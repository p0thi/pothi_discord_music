/*
 *      quantize_pvt source file
 *
 *      Copyright (c) 1999-2002 Takehiro Tominaga
 *      Copyright (c) 2000-2002 Robert Hegemann
 *      Copyright (c) 2001 Naoki Shibata
 *      Copyright (c) 2002-2005 Gabriel Bouvigne
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/* $Id: QuantizePVT.java,v 1.25 2012/03/12 15:58:57 kenchis Exp $ */
package pothi_discord.utils.lame.mp3;


public class QuantizePVT {

  /**
   * ix always <= 8191+15. see count_bits()
   */
  public static final int IXMAX_VAL = 8206;
  private static final int PRECALC_SIZE = (IXMAX_VAL + 2);
  public float pow43[] = new float[PRECALC_SIZE];
  public float adj43[] = new float[PRECALC_SIZE];
  /**
   * <CODE>
   * minimum possible number of
   * -cod_info.global_gain + ((scalefac[] + (cod_info.preflag ? pretab[sfb] : 0))
   * << (cod_info.scalefac_scale + 1)) + cod_info.subblock_gain[cod_info.window[sfb]] * 8;
   * <p/>
   * for long block, 0+((15+3)<<2) = 18*4 = 72
   * for short block, 0+(15<<2)+7*8 = 15*4+56 = 116
   * </CODE>
   */
  public static final int Q_MAX2 = 116;
  public static final int LARGE_BITS = 100000;
  /**
   * smallest such that 1.0+DBL_EPSILON != 1.0
   */
  private static final float DBL_EPSILON = 2.2204460492503131e-016f;
  private static final int Q_MAX = (256 + 1);
  public float pow20[] = new float[Q_MAX + Q_MAX2 + 1];
  public float ipow20[] = new float[Q_MAX];
  /**
   * Assuming dynamic range=96dB, this value should be 92
   */
  private static final int NSATHSCALE = 100;
  /**
   * The following table is used to implement the scalefactor partitioning for
   * MPEG2 as described in section 2.4.3.2 of the IS. The indexing corresponds
   * to the way the tables are presented in the IS:
   * <p/>
   * [table_number][row_in_table][column of nr_of_sfb]
   */
  public final int nr_of_sfb_block[][][] = new int[][][]{
      {{6, 5, 5, 5}, {9, 9, 9, 9}, {6, 9, 9, 9}},
      {{6, 5, 7, 3}, {9, 9, 12, 6}, {6, 9, 12, 6}},
      {{11, 10, 0, 0}, {18, 18, 0, 0}, {15, 18, 0, 0}},
      {{7, 7, 7, 0}, {12, 12, 12, 0}, {6, 15, 12, 0}},
      {{6, 6, 6, 3}, {12, 9, 9, 6}, {6, 12, 9, 6}},
      {{8, 8, 5, 0}, {15, 12, 9, 0}, {6, 18, 9, 0}}};
  /**
   * Table B.6: layer3 preemphasis
   */
  public final int pretab[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1,
      2, 2, 3, 3, 3, 2, 0};
  /**
   * Here are MPEG1 Table B.8 and MPEG2 Table B.1 -- Layer III scalefactor
   * bands. <BR>
   * Index into this using a method such as:<BR>
   * idx = fr_ps.header.sampling_frequency + (fr_ps.header.version * 3)
   */
  public final ScaleFac sfBandIndex[] = {
      // Table B.2.b: 22.05 kHz
      new ScaleFac(new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464,
          522, 576},
          new int[]{0, 4, 8, 12, 18, 24, 32, 42, 56, 74, 100, 132, 174, 192}
          , new int[]{0, 0, 0, 0, 0, 0, 0} //  sfb21 pseudo sub bands
          , new int[]{0, 0, 0, 0, 0, 0, 0} //  sfb12 pseudo sub bands
      ),
                         /* Table B.2.c: 24 kHz */ /* docs: 332. mpg123(broken): 330 */
      new ScaleFac(new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 114, 136, 162, 194, 232, 278, 332, 394, 464,
          540, 576},
          new int[]{0, 4, 8, 12, 18, 26, 36, 48, 62, 80, 104, 136, 180, 192}
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb21 pseudo sub bands */
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
      ),
                         /* Table B.2.a: 16 kHz */
      new ScaleFac(new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464,
          522, 576},
          new int[]{0, 4, 8, 12, 18, 26, 36, 48, 62, 80, 104, 134, 174, 192}
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb21 pseudo sub bands */
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
      ),
                         /* Table B.8.b: 44.1 kHz */
      new ScaleFac(new int[]{0, 4, 8, 12, 16, 20, 24, 30, 36, 44, 52, 62, 74, 90, 110, 134, 162, 196, 238, 288, 342, 418,
          576},
          new int[]{0, 4, 8, 12, 16, 22, 30, 40, 52, 66, 84, 106, 136, 192}
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb21 pseudo sub bands */
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
      ),
                         /* Table B.8.c: 48 kHz */
      new ScaleFac(new int[]{0, 4, 8, 12, 16, 20, 24, 30, 36, 42, 50, 60, 72, 88, 106, 128, 156, 190, 230, 276, 330, 384,
          576},
          new int[]{0, 4, 8, 12, 16, 22, 28, 38, 50, 64, 80, 100, 126, 192}
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb21 pseudo sub bands */
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
      ),
                         /* Table B.8.a: 32 kHz */
      new ScaleFac(new int[]{0, 4, 8, 12, 16, 20, 24, 30, 36, 44, 54, 66, 82, 102, 126, 156, 194, 240, 296, 364, 448, 550,
          576},
          new int[]{0, 4, 8, 12, 16, 22, 30, 42, 58, 78, 104, 138, 180, 192}
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb21 pseudo sub bands */
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
      ),
                         /* MPEG-2.5 11.025 kHz */
      new ScaleFac(new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464,
          522, 576},
          new int[]{0 / 3, 12 / 3, 24 / 3, 36 / 3, 54 / 3, 78 / 3, 108 / 3, 144 / 3, 186 / 3, 240 / 3, 312 / 3,
              402 / 3, 522 / 3, 576 / 3}
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb21 pseudo sub bands */
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
      ),
                         /* MPEG-2.5 12 kHz */
      new ScaleFac(new int[]{0, 6, 12, 18, 24, 30, 36, 44, 54, 66, 80, 96, 116, 140, 168, 200, 238, 284, 336, 396, 464,
          522, 576},
          new int[]{0 / 3, 12 / 3, 24 / 3, 36 / 3, 54 / 3, 78 / 3, 108 / 3, 144 / 3, 186 / 3, 240 / 3, 312 / 3,
              402 / 3, 522 / 3, 576 / 3}
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb21 pseudo sub bands */
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
      ),
                         /* MPEG-2.5 8 kHz */
      new ScaleFac(new int[]{0, 12, 24, 36, 48, 60, 72, 88, 108, 132, 160, 192, 232, 280, 336, 400, 476, 566, 568, 570,
          572, 574, 576},
          new int[]{0 / 3, 24 / 3, 48 / 3, 72 / 3, 108 / 3, 156 / 3, 216 / 3, 288 / 3, 372 / 3, 480 / 3, 486 / 3,
              492 / 3, 498 / 3, 576 / 3}
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb21 pseudo sub bands */
          , new int[]{0, 0, 0, 0, 0, 0, 0} /*  sfb12 pseudo sub bands */
      )
  };
  Takehiro tak;
  Reservoir rv;
  PsyModel psy;

  public final void setModules(Takehiro tk, Reservoir rv, PsyModel psy) {
    this.tak = tk;
    this.rv = rv;
    this.psy = psy;
  }

  public final float POW20(final int x) {
    assert (0 <= (x + QuantizePVT.Q_MAX2) && x < QuantizePVT.Q_MAX);
    return pow20[x + QuantizePVT.Q_MAX2];
  }

  public final float IPOW20(final int x) {
    assert (0 <= x && x < QuantizePVT.Q_MAX);
    return ipow20[x];
  }

  /**
   * <PRE>
   * compute the ATH for each scalefactor band cd range: 0..96db
   * <p/>
   * Input: 3.3kHz signal 32767 amplitude (3.3kHz is where ATH is smallest =
   * -5db) longblocks: sfb=12 en0/bw=-11db max_en0 = 1.3db shortblocks: sfb=5
   * -9db 0db
   * <p/>
   * Input: 1 1 1 1 1 1 1 -1 -1 -1 -1 -1 -1 -1 (repeated) longblocks: amp=1
   * sfb=12 en0/bw=-103 db max_en0 = -92db amp=32767 sfb=12 -12 db -1.4db
   * <p/>
   * Input: 1 1 1 1 1 1 1 -1 -1 -1 -1 -1 -1 -1 (repeated) shortblocks: amp=1
   * sfb=5 en0/bw= -99 -86 amp=32767 sfb=5 -9 db 4db
   * <p/>
   * <p/>
   * MAX energy of largest wave at 3.3kHz = 1db AVE energy of largest wave at
   * 3.3kHz = -11db Let's take AVE: -11db = maximum signal in sfb=12. Dynamic
   * range of CD: 96db. Therefor energy of smallest audible wave in sfb=12 =
   * -11 - 96 = -107db = ATH at 3.3kHz.
   * <p/>
   * ATH formula for this wave: -5db. To adjust to LAME scaling, we need ATH =
   * ATH_formula - 103 (db) ATH = ATH * 2.5e-10 (ener)
   * </PRE>
   */
  private float ATHmdct(final LameGlobalFlags gfp, final float f) {
    float ath = psy.ATHformula(f, gfp);

    ath -= NSATHSCALE;

		/* modify the MDCT scaling for the ATH and convert to energy */
    ath = (float) Math.pow(10.0, ath / 10.0 + gfp.ATHlower);
    return ath;
  }

  private void compute_ath(final LameGlobalFlags gfp) {
    final float[] ATH_l = gfp.internal_flags.ATH.l;
    final float[] ATH_psfb21 = gfp.internal_flags.ATH.psfb21;
    final float[] ATH_s = gfp.internal_flags.ATH.s;
    final float[] ATH_psfb12 = gfp.internal_flags.ATH.psfb12;
    final LameInternalFlags gfc = gfp.internal_flags;
    final float samp_freq = gfp.getOutSampleRate();

    for (int sfb = 0; sfb < Encoder.SBMAX_l; sfb++) {
      int start = gfc.scalefac_band.l[sfb];
      int end = gfc.scalefac_band.l[sfb + 1];
      ATH_l[sfb] = Float.MAX_VALUE;
      for (int i = start; i < end; i++) {
        final float freq = i * samp_freq / (2 * 576);
        float ATH_f = ATHmdct(gfp, freq); /* freq in kHz */
        ATH_l[sfb] = Math.min(ATH_l[sfb], ATH_f);
      }
    }

    for (int sfb = 0; sfb < Encoder.PSFB21; sfb++) {
      int start = gfc.scalefac_band.psfb21[sfb];
      int end = gfc.scalefac_band.psfb21[sfb + 1];
      ATH_psfb21[sfb] = Float.MAX_VALUE;
      for (int i = start; i < end; i++) {
        final float freq = i * samp_freq / (2 * 576);
        float ATH_f = ATHmdct(gfp, freq); /* freq in kHz */
        ATH_psfb21[sfb] = Math.min(ATH_psfb21[sfb], ATH_f);
      }
    }

    for (int sfb = 0; sfb < Encoder.SBMAX_s; sfb++) {
      int start = gfc.scalefac_band.s[sfb];
      int end = gfc.scalefac_band.s[sfb + 1];
      ATH_s[sfb] = Float.MAX_VALUE;
      for (int i = start; i < end; i++) {
        final float freq = i * samp_freq / (2 * 192);
        float ATH_f = ATHmdct(gfp, freq); /* freq in kHz */
        ATH_s[sfb] = Math.min(ATH_s[sfb], ATH_f);
      }
      ATH_s[sfb] *= (gfc.scalefac_band.s[sfb + 1] - gfc.scalefac_band.s[sfb]);
    }

    for (int sfb = 0; sfb < Encoder.PSFB12; sfb++) {
      int start = gfc.scalefac_band.psfb12[sfb];
      int end = gfc.scalefac_band.psfb12[sfb + 1];
      ATH_psfb12[sfb] = Float.MAX_VALUE;
      for (int i = start; i < end; i++) {
        final float freq = i * samp_freq / (2 * 192);
        float ATH_f = ATHmdct(gfp, freq); /* freq in kHz */
        ATH_psfb12[sfb] = Math.min(ATH_psfb12[sfb], ATH_f);
      }
      /* not sure about the following */
      ATH_psfb12[sfb] *= (gfc.scalefac_band.s[13] - gfc.scalefac_band.s[12]);
    }

		/*
		 * no-ATH mode: reduce ATH to -200 dB
		 */
    if (gfp.noATH) {
      for (int sfb = 0; sfb < Encoder.SBMAX_l; sfb++) {
        ATH_l[sfb] = 1E-20f;
      }
      for (int sfb = 0; sfb < Encoder.PSFB21; sfb++) {
        ATH_psfb21[sfb] = 1E-20f;
      }
      for (int sfb = 0; sfb < Encoder.SBMAX_s; sfb++) {
        ATH_s[sfb] = 1E-20f;
      }
      for (int sfb = 0; sfb < Encoder.PSFB12; sfb++) {
        ATH_psfb12[sfb] = 1E-20f;
      }
    }

		/*
		 * work in progress, don't rely on it too much
		 */
    gfc.ATH.floor = 10.f * (float) Math.log10(ATHmdct(gfp, -1.f));
  }

  /**
   * initialization for iteration_loop
   */
  public final void iteration_init(final LameGlobalFlags gfp) {
    final LameInternalFlags gfc = gfp.internal_flags;
    final IIISideInfo l3_side = gfc.l3_side;
    int i;

    if (gfc.iteration_init_init == 0) {
      gfc.iteration_init_init = 1;

      l3_side.main_data_begin = 0;
      compute_ath(gfp);

      pow43[0] = 0.0f;
      for (i = 1; i < PRECALC_SIZE; i++)
        pow43[i] = (float) Math.pow((float) i, 4.0 / 3.0);

      for (i = 0; i < PRECALC_SIZE - 1; i++)
        adj43[i] = (float) ((i + 1) - Math.pow(
            0.5 * (pow43[i] + pow43[i + 1]), 0.75));
      adj43[i] = 0.5f;

      for (i = 0; i < Q_MAX; i++)
        ipow20[i] = (float) Math.pow(2.0, (i - 210) * -0.1875);
      for (i = 0; i <= Q_MAX + Q_MAX2; i++)
        pow20[i] = (float) Math.pow(2.0, (i - 210 - Q_MAX2) * 0.25);

      tak.huffman_init(gfc);

      {
        float bass, alto, treble, sfb21;

        i = (gfp.exp_nspsytune >> 2) & 63;
        if (i >= 32)
          i -= 64;
        bass = (float) Math.pow(10, i / 4.0 / 10.0);

        i = (gfp.exp_nspsytune >> 8) & 63;
        if (i >= 32)
          i -= 64;
        alto = (float) Math.pow(10, i / 4.0 / 10.0);

        i = (gfp.exp_nspsytune >> 14) & 63;
        if (i >= 32)
          i -= 64;
        treble = (float) Math.pow(10, i / 4.0 / 10.0);

				/*
				 * to be compatible with Naoki's original code, the next 6 bits
				 * define only the amount of changing treble for sfb21
				 */
        i = (gfp.exp_nspsytune >> 20) & 63;
        if (i >= 32)
          i -= 64;
        sfb21 = treble * (float) Math.pow(10, i / 4.0 / 10.0);
        for (i = 0; i < Encoder.SBMAX_l; i++) {
          float f;
          if (i <= 6)
            f = bass;
          else if (i <= 13)
            f = alto;
          else if (i <= 20)
            f = treble;
          else
            f = sfb21;

          gfc.nsPsy.longfact[i] = f;
        }
        for (i = 0; i < Encoder.SBMAX_s; i++) {
          float f;
          if (i <= 5)
            f = bass;
          else if (i <= 10)
            f = alto;
          else if (i <= 11)
            f = treble;
          else
            f = sfb21;

          gfc.nsPsy.shortfact[i] = f;
        }
      }
    }
  }

  /**
   * allocate bits among 2 channels based on PE<BR>
   * mt 6/99<BR>
   * bugfixes rh 8/01: often allocated more than the allowed 4095 bits
   */
  public final int on_pe(final LameGlobalFlags gfp, float pe[][],
                         int targ_bits[], int mean_bits, int gr, int cbr) {
    final LameInternalFlags gfc = gfp.internal_flags;
    int tbits = 0, bits;
    int add_bits[] = new int[2];
    int ch;

		/* allocate targ_bits for granule */
    MeanBits mb = new MeanBits(tbits);
    int extra_bits = rv.ResvMaxBits(gfp, mean_bits, mb, cbr);
    tbits = mb.bits;
		/* maximum allowed bits for this granule */
    int max_bits = tbits + extra_bits;
    if (max_bits > LameInternalFlags.MAX_BITS_PER_GRANULE) {
      // hard limit per granule
      max_bits = LameInternalFlags.MAX_BITS_PER_GRANULE;
    }
    for (bits = 0, ch = 0; ch < gfc.channels_out; ++ch) {
      /******************************************************************
       * allocate bits for each channel
       ******************************************************************/
      targ_bits[ch] = Math.min(LameInternalFlags.MAX_BITS_PER_CHANNEL,
          tbits / gfc.channels_out);

      add_bits[ch] = (int) (targ_bits[ch] * pe[gr][ch] / 700.0 - targ_bits[ch]);

			/* at most increase bits by 1.5*average */
      if (add_bits[ch] > mean_bits * 3 / 4)
        add_bits[ch] = mean_bits * 3 / 4;
      if (add_bits[ch] < 0)
        add_bits[ch] = 0;

      if (add_bits[ch] + targ_bits[ch] > LameInternalFlags.MAX_BITS_PER_CHANNEL)
        add_bits[ch] = Math.max(0,
            LameInternalFlags.MAX_BITS_PER_CHANNEL - targ_bits[ch]);

      bits += add_bits[ch];
    }
    if (bits > extra_bits) {
      for (ch = 0; ch < gfc.channels_out; ++ch) {
        add_bits[ch] = extra_bits * add_bits[ch] / bits;
      }
    }

    for (ch = 0; ch < gfc.channels_out; ++ch) {
      targ_bits[ch] += add_bits[ch];
      extra_bits -= add_bits[ch];
    }

    for (bits = 0, ch = 0; ch < gfc.channels_out; ++ch) {
      bits += targ_bits[ch];
    }
    if (bits > LameInternalFlags.MAX_BITS_PER_GRANULE) {
      int sum = 0;
      for (ch = 0; ch < gfc.channels_out; ++ch) {
        targ_bits[ch] *= LameInternalFlags.MAX_BITS_PER_GRANULE;
        targ_bits[ch] /= bits;
        sum += targ_bits[ch];
      }
      assert (sum <= LameInternalFlags.MAX_BITS_PER_GRANULE);
    }

    return max_bits;
  }

  public final void reduce_side(final int targ_bits[],
                                final float ms_ener_ratio, final int mean_bits, final int max_bits) {
    assert (max_bits <= LameInternalFlags.MAX_BITS_PER_GRANULE);
    assert (targ_bits[0] + targ_bits[1] <= LameInternalFlags.MAX_BITS_PER_GRANULE);

		/*
		 * ms_ener_ratio = 0: allocate 66/33 mid/side fac=.33 ms_ener_ratio =.5:
		 * allocate 50/50 mid/side fac= 0
		 */
		/* 75/25 split is fac=.5 */
    float fac = .33f * (.5f - ms_ener_ratio) / .5f;
    if (fac < 0)
      fac = 0;
    if (fac > .5)
      fac = .5f;

		/* number of bits to move from side channel to mid channel */
		/* move_bits = fac*targ_bits[1]; */
    int move_bits = (int) (fac * .5 * (targ_bits[0] + targ_bits[1]));

    if (move_bits > LameInternalFlags.MAX_BITS_PER_CHANNEL - targ_bits[0]) {
      move_bits = LameInternalFlags.MAX_BITS_PER_CHANNEL - targ_bits[0];
    }
    if (move_bits < 0)
      move_bits = 0;

    if (targ_bits[1] >= 125) {
			/* dont reduce side channel below 125 bits */
      if (targ_bits[1] - move_bits > 125) {

				/* if mid channel already has 2x more than average, dont bother */
				/* mean_bits = bits per granule (for both channels) */
        if (targ_bits[0] < mean_bits)
          targ_bits[0] += move_bits;
        targ_bits[1] -= move_bits;
      } else {
        targ_bits[0] += targ_bits[1] - 125;
        targ_bits[1] = 125;
      }
    }

    move_bits = targ_bits[0] + targ_bits[1];
    if (move_bits > max_bits) {
      targ_bits[0] = (max_bits * targ_bits[0]) / move_bits;
      targ_bits[1] = (max_bits * targ_bits[1]) / move_bits;
    }
    assert (targ_bits[0] <= LameInternalFlags.MAX_BITS_PER_CHANNEL);
    assert (targ_bits[1] <= LameInternalFlags.MAX_BITS_PER_CHANNEL);
    assert (targ_bits[0] + targ_bits[1] <= LameInternalFlags.MAX_BITS_PER_GRANULE);
  }

  /**
   * Robert Hegemann 2001-04-27:
   * this adjusts the ATH, keeping the original noise floor
   * affects the higher frequencies more than the lower ones
   */
  public final float athAdjust(final float a, final float x,
                               final float athFloor) {
		/*
		 * work in progress
		 */
    final float o = 90.30873362f;
    final float p = 94.82444863f;
    float u = Util.FAST_LOG10_X(x, 10.0f);
    final float v = a * a;
    float w = 0.0f;
    u -= athFloor; /* undo scaling */
    if (v > 1E-20)
      w = 1.f + Util.FAST_LOG10_X(v, 10.0f / o);
    if (w < 0)
      w = 0.f;
    u *= w;
    u += athFloor + o - p; /* redo scaling */

    return (float) Math.pow(10., 0.1 * u);
  }

  /**
   * Calculate the allowed distortion for each scalefactor band, as determined
   * by the psychoacoustic model. xmin(sb) = ratio(sb) * en(sb) / bw(sb)
   * <p/>
   * returns number of sfb's with energy > ATH
   */
  public final int calc_xmin(final LameGlobalFlags gfp,
                             final III_psy_ratio ratio, final GrInfo cod_info,
                             final float[] pxmin) {
    int pxminPos = 0;
    final LameInternalFlags gfc = gfp.internal_flags;
    int gsfb, j = 0, ath_over = 0;
    final ATH ATH = gfc.ATH;
    final float[] xr = cod_info.xr;
    final int enable_athaa_fix = (gfp.getVBR() == VbrMode.vbr_mtrh) ? 1 : 0;
    float masking_lower = gfc.masking_lower;

    if (gfp.getVBR() == VbrMode.vbr_mtrh || gfp.getVBR() == VbrMode.vbr_mt) {
			/* was already done in PSY-Model */
      masking_lower = 1.0f;
    }

    for (gsfb = 0; gsfb < cod_info.psy_lmax; gsfb++) {
      float en0, xmin;
      float rh1, rh2;
      int width, l;

      if (gfp.getVBR() == VbrMode.vbr_rh || gfp.getVBR() == VbrMode.vbr_mtrh)
        xmin = athAdjust(ATH.adjust, ATH.l[gsfb], ATH.floor);
      else
        xmin = ATH.adjust * ATH.l[gsfb];

      width = cod_info.width[gsfb];
      rh1 = xmin / width;
      rh2 = DBL_EPSILON;
      l = width >> 1;
      en0 = 0.0f;
      do {
        float xa, xb;
        xa = xr[j] * xr[j];
        en0 += xa;
        rh2 += (xa < rh1) ? xa : rh1;
        j++;
        xb = xr[j] * xr[j];
        en0 += xb;
        rh2 += (xb < rh1) ? xb : rh1;
        j++;
      } while (--l > 0);
      if (en0 > xmin)
        ath_over++;

      if (gsfb == Encoder.SBPSY_l) {
        float x = xmin * gfc.nsPsy.longfact[gsfb];
        if (rh2 < x) {
          rh2 = x;
        }
      }
      if (enable_athaa_fix != 0) {
        xmin = rh2;
      }
      if (!gfp.ATHonly) {
        final float e = ratio.en.l[gsfb];
        if (e > 0.0f) {
          float x;
          x = en0 * ratio.thm.l[gsfb] * masking_lower / e;
          if (enable_athaa_fix != 0)
            x *= gfc.nsPsy.longfact[gsfb];
          if (xmin < x)
            xmin = x;
        }
      }
      if (enable_athaa_fix != 0)
        pxmin[pxminPos++] = xmin;
      else
        pxmin[pxminPos++] = xmin * gfc.nsPsy.longfact[gsfb];
    } /* end of long block loop */

		/* use this function to determine the highest non-zero coeff */
    int max_nonzero = 575;
    if (cod_info.block_type != Encoder.SHORT_TYPE) {
      // NORM, START or STOP type, but not SHORT
      int k = 576;
      while (k-- != 0 && BitStream.EQ(xr[k], 0)) {
        max_nonzero = k;
      }
    }
    cod_info.max_nonzero_coeff = max_nonzero;

    for (int sfb = cod_info.sfb_smin; gsfb < cod_info.psymax; sfb++, gsfb += 3) {
      int width, b;
      float tmpATH;
      if (gfp.getVBR() == VbrMode.vbr_rh || gfp.getVBR() == VbrMode.vbr_mtrh)
        tmpATH = athAdjust(ATH.adjust, ATH.s[sfb], ATH.floor);
      else
        tmpATH = ATH.adjust * ATH.s[sfb];

      width = cod_info.width[gsfb];
      for (b = 0; b < 3; b++) {
        float en0 = 0.0f, xmin;
        float rh1, rh2;
        int l = width >> 1;

        rh1 = tmpATH / width;
        rh2 = DBL_EPSILON;
        do {
          float xa, xb;
          xa = xr[j] * xr[j];
          en0 += xa;
          rh2 += (xa < rh1) ? xa : rh1;
          j++;
          xb = xr[j] * xr[j];
          en0 += xb;
          rh2 += (xb < rh1) ? xb : rh1;
          j++;
        } while (--l > 0);
        if (en0 > tmpATH)
          ath_over++;
        if (sfb == Encoder.SBPSY_s) {
          float x = tmpATH * gfc.nsPsy.shortfact[sfb];
          if (rh2 < x) {
            rh2 = x;
          }
        }
        if (enable_athaa_fix != 0)
          xmin = rh2;
        else
          xmin = tmpATH;

        if (!gfp.ATHonly && !gfp.ATHshort) {
          final float e = ratio.en.s[sfb][b];
          if (e > 0.0f) {
            float x;
            x = en0 * ratio.thm.s[sfb][b] * masking_lower / e;
            if (enable_athaa_fix != 0)
              x *= gfc.nsPsy.shortfact[sfb];
            if (xmin < x)
              xmin = x;
          }
        }
        if (enable_athaa_fix != 0)
          pxmin[pxminPos++] = xmin;
        else
          pxmin[pxminPos++] = xmin * gfc.nsPsy.shortfact[sfb];
      } /* b */
      if (gfp.useTemporal) {
        if (pxmin[pxminPos - 3] > pxmin[pxminPos - 3 + 1])
          pxmin[pxminPos - 3 + 1] += (pxmin[pxminPos - 3] - pxmin[pxminPos - 3 + 1])
              * gfc.decay;
        if (pxmin[pxminPos - 3 + 1] > pxmin[pxminPos - 3 + 2])
          pxmin[pxminPos - 3 + 2] += (pxmin[pxminPos - 3 + 1] - pxmin[pxminPos - 3 + 2])
              * gfc.decay;
      }
    } /* end of short block sfb loop */

    return ath_over;
  }

  private float calc_noise_core(final GrInfo cod_info,
                                final StartLine startline, int l, final float step) {
    float noise = 0;
    int j = startline.s;
    final int[] ix = cod_info.l3_enc;

    if (j > cod_info.count1) {
      while ((l--) != 0) {
        float temp;
        temp = cod_info.xr[j];
        j++;
        noise += temp * temp;
        temp = cod_info.xr[j];
        j++;
        noise += temp * temp;
      }
    } else if (j > cod_info.big_values) {
      float ix01[] = new float[2];
      ix01[0] = 0;
      ix01[1] = step;
      while ((l--) != 0) {
        float temp;
        temp = Math.abs(cod_info.xr[j]) - ix01[ix[j]];
        j++;
        noise += temp * temp;
        temp = Math.abs(cod_info.xr[j]) - ix01[ix[j]];
        j++;
        noise += temp * temp;
      }
    } else {
      while ((l--) != 0) {
        float temp;
        temp = Math.abs(cod_info.xr[j]) - pow43[ix[j]] * step;
        j++;
        noise += temp * temp;
        temp = Math.abs(cod_info.xr[j]) - pow43[ix[j]] * step;
        j++;
        noise += temp * temp;
      }
    }

    startline.s = j;
    return noise;
  }

  /**
   * <PRE>
   * -oo dB  =>  -1.00
   * - 6 dB  =>  -0.97
   * - 3 dB  =>  -0.80
   * - 2 dB  =>  -0.64
   * - 1 dB  =>  -0.38
   * 0 dB  =>   0.00
   * + 1 dB  =>  +0.49
   * + 2 dB  =>  +1.06
   * + 3 dB  =>  +1.68
   * + 6 dB  =>  +3.69
   * +10 dB  =>  +6.45
   * </PRE>
   */
  public final int calc_noise(final GrInfo cod_info,
                              final float[] l3_xmin, final float[] distort,
                              final CalcNoiseResult res, final CalcNoiseData prev_noise) {
    int distortPos = 0;
    int l3_xminPos = 0;
    int sfb, l, over = 0;
    float over_noise_db = 0;
		/* 0 dB relative to masking */
    float tot_noise_db = 0;
		/* -200 dB relative to masking */
    float max_noise = -20.0f;
    int j = 0;
    final int[] scalefac = cod_info.scalefac;
    int scalefacPos = 0;

    res.over_SSD = 0;

    for (sfb = 0; sfb < cod_info.psymax; sfb++) {
      final int s = cod_info.global_gain
          - (((scalefac[scalefacPos++]) + (cod_info.preflag != 0 ? pretab[sfb]
          : 0)) << (cod_info.scalefac_scale + 1))
          - cod_info.subblock_gain[cod_info.window[sfb]] * 8;
      float noise = 0.0f;

      if (prev_noise != null && (prev_noise.step[sfb] == s)) {

				/* use previously computed values */
        noise = prev_noise.noise[sfb];
        j += cod_info.width[sfb];
        distort[distortPos++] = noise / l3_xmin[l3_xminPos++];

        noise = prev_noise.noise_log[sfb];

      } else {
        final float step = POW20(s);
        l = cod_info.width[sfb] >> 1;

        if ((j + cod_info.width[sfb]) > cod_info.max_nonzero_coeff) {
          int usefullsize;
          usefullsize = cod_info.max_nonzero_coeff - j + 1;

          if (usefullsize > 0)
            l = usefullsize >> 1;
          else
            l = 0;
        }

        StartLine sl = new StartLine(j);
        noise = calc_noise_core(cod_info, sl, l, step);
        j = sl.s;

        if (prev_noise != null) {
					/* save noise values */
          prev_noise.step[sfb] = s;
          prev_noise.noise[sfb] = noise;
        }

        noise = distort[distortPos++] = noise / l3_xmin[l3_xminPos++];

				/* multiplying here is adding in dB, but can overflow */
        noise = Util.FAST_LOG10((float) Math.max(noise, 1E-20));

        if (prev_noise != null) {
					/* save noise values */
          prev_noise.noise_log[sfb] = noise;
        }
      }

      if (prev_noise != null) {
				/* save noise values */
        prev_noise.global_gain = cod_info.global_gain;
      }

      tot_noise_db += noise;

      if (noise > 0.0) {
        int tmp;

        tmp = Math.max((int) (noise * 10 + .5), 1);
        res.over_SSD += tmp * tmp;

        over++;
				/* multiplying here is adding in dB -but can overflow */
				/* over_noise *= noise; */
        over_noise_db += noise;
      }
      max_noise = Math.max(max_noise, noise);

    }

    res.over_count = over;
    res.tot_noise = tot_noise_db;
    res.over_noise = over_noise_db;
    res.max_noise = max_noise;

    return over;
  }

  /**
   * updates plotting data
   * <p/>
   * Mark Taylor 2000-??-??
   * <p/>
   * Robert Hegemann: moved noise/distortion calc into it
   */
  private void set_pinfo(final LameGlobalFlags gfp,
                         final GrInfo cod_info, final III_psy_ratio ratio,
                         final int gr, final int ch) {
    final LameInternalFlags gfc = gfp.internal_flags;
    int sfb, sfb2;
    int l;
    float en0, en1;
    float ifqstep = (cod_info.scalefac_scale == 0) ? .5f : 1.0f;
    int[] scalefac = cod_info.scalefac;

    float l3_xmin[] = new float[L3Side.SFBMAX], xfsf[] = new float[L3Side.SFBMAX];
    CalcNoiseResult noise = new CalcNoiseResult();

    calc_xmin(gfp, ratio, cod_info, l3_xmin);
    calc_noise(cod_info, l3_xmin, xfsf, noise, null);

    int j = 0;
    sfb2 = cod_info.sfb_lmax;
    if (cod_info.block_type != Encoder.SHORT_TYPE
        && 0 == cod_info.mixed_block_flag)
      sfb2 = 22;
    for (sfb = 0; sfb < sfb2; sfb++) {
      int start = gfc.scalefac_band.l[sfb];
      int end = gfc.scalefac_band.l[sfb + 1];
      int bw = end - start;
      for (en0 = 0.0f; j < end; j++)
        en0 += cod_info.xr[j] * cod_info.xr[j];
      en0 /= bw;
			/* convert to MDCT units */
			/* scaling so it shows up on FFT plot */
      en1 = 1e15f;
      gfc.pinfo.en[gr][ch][sfb] = en1 * en0;
      gfc.pinfo.xfsf[gr][ch][sfb] = en1 * l3_xmin[sfb] * xfsf[sfb] / bw;

      if (ratio.en.l[sfb] > 0 && !gfp.ATHonly)
        en0 = en0 / ratio.en.l[sfb];
      else
        en0 = 0.0f;

      gfc.pinfo.thr[gr][ch][sfb] = en1
          * Math.max(en0 * ratio.thm.l[sfb], gfc.ATH.l[sfb]);

			/* there is no scalefactor bands >= SBPSY_l */
      gfc.pinfo.LAMEsfb[gr][ch][sfb] = 0;
      if (cod_info.preflag != 0 && sfb >= 11)
        gfc.pinfo.LAMEsfb[gr][ch][sfb] = -ifqstep * pretab[sfb];

      if (sfb < Encoder.SBPSY_l) {
				/* scfsi should be decoded by caller side */
        assert (scalefac[sfb] >= 0);
        gfc.pinfo.LAMEsfb[gr][ch][sfb] -= ifqstep * scalefac[sfb];
      }
    } /* for sfb */

    if (cod_info.block_type == Encoder.SHORT_TYPE) {
      sfb2 = sfb;
      for (sfb = cod_info.sfb_smin; sfb < Encoder.SBMAX_s; sfb++) {
        int start = gfc.scalefac_band.s[sfb];
        int end = gfc.scalefac_band.s[sfb + 1];
        int bw = end - start;
        for (int i = 0; i < 3; i++) {
          for (en0 = 0.0f, l = start; l < end; l++) {
            en0 += cod_info.xr[j] * cod_info.xr[j];
            j++;
          }
          en0 = (float) Math.max(en0 / bw, 1e-20);
					/* convert to MDCT units */
					/* scaling so it shows up on FFT plot */
          en1 = 1e15f;

          gfc.pinfo.en_s[gr][ch][3 * sfb + i] = en1 * en0;
          gfc.pinfo.xfsf_s[gr][ch][3 * sfb + i] = en1 * l3_xmin[sfb2]
              * xfsf[sfb2] / bw;
          if (ratio.en.s[sfb][i] > 0)
            en0 = en0 / ratio.en.s[sfb][i];
          else
            en0 = 0.0f;
          if (gfp.ATHonly || gfp.ATHshort)
            en0 = 0;

          gfc.pinfo.thr_s[gr][ch][3 * sfb + i] = en1
              * Math.max(en0 * ratio.thm.s[sfb][i],
              gfc.ATH.s[sfb]);

					/* there is no scalefactor bands >= SBPSY_s */
          gfc.pinfo.LAMEsfb_s[gr][ch][3 * sfb + i] = -2.0
              * cod_info.subblock_gain[i];
          if (sfb < Encoder.SBPSY_s) {
            gfc.pinfo.LAMEsfb_s[gr][ch][3 * sfb + i] -= ifqstep
                * scalefac[sfb2];
          }
          sfb2++;
        }
      }
    } /* block type short */
    gfc.pinfo.LAMEqss[gr][ch] = cod_info.global_gain;
    gfc.pinfo.LAMEmainbits[gr][ch] = cod_info.part2_3_length
        + cod_info.part2_length;
    gfc.pinfo.LAMEsfbits[gr][ch] = cod_info.part2_length;

    gfc.pinfo.over[gr][ch] = noise.over_count;
    gfc.pinfo.max_noise[gr][ch] = noise.max_noise * 10.0;
    gfc.pinfo.over_noise[gr][ch] = noise.over_noise * 10.0;
    gfc.pinfo.tot_noise[gr][ch] = noise.tot_noise * 10.0;
    gfc.pinfo.over_SSD[gr][ch] = noise.over_SSD;
  }

  /**
   * updates plotting data for a whole frame
   * <p/>
   * Robert Hegemann 2000-10-21
   */
  public final void set_frame_pinfo(final LameGlobalFlags gfp,
                                    final III_psy_ratio ratio[][]) {
    final LameInternalFlags gfc = gfp.internal_flags;

    gfc.masking_lower = 1.0f;

		/*
		 * for every granule and channel patch l3_enc and set info
		 */
    for (int gr = 0; gr < gfc.mode_gr; gr++) {
      for (int ch = 0; ch < gfc.channels_out; ch++) {
        GrInfo cod_info = gfc.l3_side.tt[gr][ch];
        int scalefac_sav[] = new int[L3Side.SFBMAX];
        System.arraycopy(cod_info.scalefac, 0, scalefac_sav, 0,
            scalefac_sav.length);

				/*
				 * reconstruct the scalefactors in case SCFSI was used
				 */
        if (gr == 1) {
          int sfb;
          for (sfb = 0; sfb < cod_info.sfb_lmax; sfb++) {
            if (cod_info.scalefac[sfb] < 0) /* scfsi */
              cod_info.scalefac[sfb] = gfc.l3_side.tt[0][ch].scalefac[sfb];
          }
        }

        set_pinfo(gfp, cod_info, ratio[gr][ch], gr, ch);
        System.arraycopy(scalefac_sav, 0, cod_info.scalefac, 0,
            scalefac_sav.length);
      } /* for ch */
    } /* for gr */
  }

  private static class StartLine {
    int s;

    public StartLine(final int j) {
      s = j;
    }
  }

}