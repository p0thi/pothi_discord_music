package pothi_discord.utils.audio;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.morphia.autoplaylists.AutoPlaylist;
import pothi_discord.utils.youtube.YoutubeAPI;
import pothi_discord.utils.youtube.YoutubePlaylist;
import pothi_discord.utils.youtube.YoutubeVideo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Pascal Pothmann on 29.01.2017.
 */
public enum YoutubeMusicGenre {
    HIP_HOP("UCUnSTiCHiHgZA9NQUG6lZkQ", "Hip Hop"),
    POP("UCE80FOXpJydkkMo-BYoJdEg", "Pop"),
    ROCK("UCRZoK7sezr5KRjk7BBjmH6w", "Rock"),
    LATIN("UCYYsyo5ekR-2Nw10s4mj3pQ", "Latin"),
    ELECTRONIC("UCCIPrrom6DIftcrInjeMvsQ", "Electronic"),
    COUNTRY("UClYMFaf6IdjQnZmsnw9N1hQ", "Country"),
    ALTERNATIVE_ROCK("UCHtUkBSmt4d92XP8q17JC3w", "Alternative Rock"),
    RNB("UC6mtDVEELAf8uYcEHvQFOWg", "Modern R & B"),
    TRAP("UC5T5hCaYVgsjc4c171koQMQ", "Trap"),
    CHRISTIAN("UCnl8lkoNIxpKL9aO0wqHYfA", "Christian"),
    HEAVY_METAL("UCSkJDgBGvNOEXSQl4YNjDtQ", "Heavy Metal"),
    SOUL("UCsFaF_3y_L__y8kWAIEhv1w", "Soul"),
    MEXICAN("UCHZ_tWPw3kldbVFZSwSaC-Q", "Mexican"),
    HOUSE("UCBg69z2WJGVY2TbhJ1xG4AA", "House"),
    ASIAN("UCDQ_5Wcc54n1_GrAzf05uWQ", "Asian"),
    REGGAE("UCEdvzYtzTH_FFpB3VRjFV6Q", "Reggae"),
    POP_ROCK("UCcu0YYUpyosw5_sLnK4wK4A", "Pop-Rock"),
    RYTHM_AND_BLUES("UCvwDeZSN2oUHlLWYRLvKceA", "Rythm and Blues"),
    CLASSICAL("UCLwMU2tKAlCoMSbGQDuiMSg", "Classic"),
    HARD_ROCK("UCsZHtkfJrxwNPR5DjLTyHlA", "Hard Rock"),
    MEDITATION("UCxtslYuuen3BRMnJZ9Z3PMw", "Meditation"),
    EDM("UCeAIo5P3sKEiuhGn-rExx7Q", "Electronic Dance"),
    INDIE("UCm-O8i4MEqBWq2wB03YGtfg", "Indie"),
    NEW_AGE("UCfqBDMEJrevX2_2XBUSxAqg", "New Age"),
    DANCEHALL("UC0JcPCLZ_orBB5nmy7IFLmQ", "Dancehall"),
    K_POP("UCsEonk9fs_9jmtw9PwER9yg", "K-Pop"),
    JAZZ("UC7KZmdQxhcajZSEFLJr3gCg", "Jazz"),
    BANDA("UCmcvZobIuROuVh0T_nVqWyw", "Banda"),
    WEST_COAST_HIP_HOP("UCv9JclKheZl9zLUCCSEVlyw", "Westcoast-Hip-Hop"),
    GOSPEL("UCTaFu5zwa9ySHWFlBo3aDPQ", "Gospel"),
    FREESTYLE_RAP("UCtHb6jVCl1UwKYFohsQGoLw", "Freestyle Rap"),
    CUMBIA("UC1n7F2eniGjfTjCIA7_8Zkg", "Cumbia"),
    PUNK_ROCK("UCYdGdf5Yf9m864XvwVBBDOQ", "Punk Rock"),
    BATTLE_RAP("UCfq1r2yndkHTLZ-pHlwvVAA", "Battle-Rap"),
    CONTEMPORARY_CHRISTIAN("UCVvRF2O1DMiQqaXALhU_afA", "Contemporary Cristian"),
    REGGAETON("UCh3PEQmV2_1D69MCcx-PArg", "Reggaeton"),
    COUNTRY_POP("UCWCPYzztTrW7CVxtuna7U2A", "Country Pop"),
    BACHATA("UCH5hwd07CSPSjgcVnKYeIyw", "Bachata"),
    INDIE_ROCK("UCqPTgFxqw_46kJGmEznQqqQ", "Indie Rock"),
    FUNK("UCxk1wRJGOTmzJAbvbQ8VicQ", "Funk"),
    SYNTHPOP("UCINv-NTCaQ2WqQDcB9quY_A", "Synthpop"),
    ELECTRO_POP("UCwqLVR1tA5E9bOZqvIaCbXA", "Elektro Pop"),
    INDIAN("UC4K4LBy_IQGmQrAQVIa1JlA", "Indian"),
    LATIN_POP("UC_qo8SpCCQ8m5zxoJDaqPbw", "Latin Pop"),
    SOFT_ROCK("UCFGhkqw3_rCSBTb2_i0P0Zg", "Soft Rock"),
    CORRIDO("UCu3cAhEkeCk6IDef_oQEucw", "Corrido"),
    THEME("UC4lqpExI7a8Ui1Bsj2KL0lg", "Theme"),
    DUBSTEP("UCjkMJPjcas2MdbzUsazzJ5g", "Dubstep"),
    SOUTHERN_HIP_HOP("UCvnjBxmJR0EfRviB7XjwIYQ", "Southern Hip Hop"),
    CONTEMPORARY_WORSHIP("UCzPmJeIkU-4ZUihOgBKgSMg", "Church"),
    FOLK_ROCK("UCW4dEMmkxH894nEU5b_gTrA", "Folk Rock"),
    ALTERNATIVE_COUNTRY("UCeD0g2R71LE_ldUlJC3fReA", "Alternative Country"),
    GANGSTA_RAP("UC1G24wIiYJFN93PGhS70QCw", "Gangsta Rap"),
    ELECTRO_HOUSE("UCUuIu6i8sx5HEDXb-quSFSg", "Electro House"),
    COUNTRY_ROCK("UCrd-uCqEUYWiXYyZf-uEyYA", "Country Rock"),
    INDIE_POP("UCyks5zRLF6A7ayt57piPvLg", "Indie Pop"),
    NORTENO("UC7dx_5aDXRGYWYqsoBHzdmg", "Norteño"),
    CHILL_OUT("UCDopqrP22sO63Bx45FYXzbA", "Chillout"),
    HINDU("UCoI1CEA6V4M_YqM22mH7Z9A", "Hindu"),
    BHAJAN("UCESyeo_eH1_7dIEudax6j8g", "Bhajan"),
    ALTERNATIVE_METAL("UCnIu2rbUSqqeZiyUdyUiw7w", "Alternative Metal"),
    RAP_ROCK("UCDKGVyxAKJjhthsc38_hxeg", "Rap Rock"),
    AMBIENT("UC9c4-wV9SKdxeRSmOqpO9Tg", "Ambient"),
    DEEP_HOUSE("UCg2AT1OrMk9L4Q-6bZK3jzQ", "Deep-House"),
    POP_RAP("UChLiECJby_tFgHf24nKCruw", "Pop Rap"),
    VOCAL("UCrrrTqJSxijC3hIJ-2oL8mw", "Vocal"),
    POST_GRUNGE("UCJrXiNxm3CjfQTjjZkxJucw", "Post-Grunge"),
    ACOUSTIC("UCJZfacqCsoD-GQiEJRbh6tg", "Acoustic"),
    DISCO("UCNGkvx5UwHzqlo6zDgRDYsQ", "Disco"),
    SALSA("UCtgHNT0ymW_SLvFdWsMmU0w", "Salsa"),
    ELECTRO("UCC-qfySwVlbmc1adWqaEVNg", "Electro"),
    EAST_COST_HIP_HOP("UC1iTbMrUhpemrk4vfQAFCig", "Eastcost-Hip-Hop"),
    NU_METAL("UCat2Bu_i7TPzFkw2G6gUhDg", "Nu Metal"),
    NEW_WAVE("UCzEHtuQ64RMEeVd6kvSH37g", "New Wave"),
    PROGRESSIVE_ROCK("UC7CJtubf9lnlGDzXHblmgwQ", "Progressive Rock"),
    TRANCE("UC5d4piMBQlBQRFpS9m_8UZQ", "Trance"),
    ALTERNATIVE_HIP_HOP("UCb3wpWWoBn3JKXAlHxOn-8A", "Alternative Hip Hop"),
    BACKGROUND("UCH3_YJAZ4FkvjkHPtZUaqeQ", "Hintergrund"),
    POWER_POP("UC7tj66CWQHLaCf6ZDIaAhcw", "Power Pop"),
    BLUES("UCYlU_M1PLtYZ6qTfKIUlxLQ", "Blues"),
    AFRICAN("UCadO807x4w5SAo-KKnQTMcA", "Afrikanisch"),
    FOLK("UC9GxgUzRt2qUIII3tSSRjwQ", "Folk"),
    THRASH_METAL("UCee8g57FktUP68exwmlvsdA", "Thrash Metal"),
    POP_PUNK("UCAOx9Hx0650MlmKTCekhV-Q", "Pop Punk"),
    BLUE_EYED_SOUL("UCQOq9ZdP7FtQK5er6ky45Jg", "Blue Eyed Soul"),
    VOCALOID("UCqabpfl-BD973ckVietzyLw", "Vocaloid"),
    FILMI("UCLF0KVv3DPBCjatHgwgkpsQ", "Filmi"),
    OPERA("UCM28Vg3P22LA3_pnoLo8eFw", "Opera"),
    PROGRESSIVE_HOUSE("UCrg8rOJ4eROCmqsmuaPQwsw", "Progressive House"),
    BALLAD("UCotL-7P2genUhs6h5O_gwWQ", "Ballad"),
    GRUNGE("UCoK3K7eGVzC9c8xOQjFAENw", "Grunge"),
    ELECTRONIC_ROCK("UCRNpymqC9wsHWJA7kkPFz_g", "Electronic Rock"),
    BRAZILIAN("UC1US41DuORGSh5cu8_vF5Tg", "Brazilian"),
    HYMN("UCQj3M6iv8Xo_kY_292gnvRQ", "Hymn"),
    SMOOTH_JAZZ("UCFD6rZx3YhGfR_pLonh8G3g", "Smooth Jazz"),
    SOUTHERN_ROCK("UCtj4KiSgBH0Xm0pcxzulSVQ", "Southern Rock"),
    DOWNTEMPO("UCoxecmnmlh4gds1Wpx7y64Q", "Downtempo"),
    ROMANTIC_MUSIC("UCXul-yaLLKSMS6y3QZtZqoQ", "Romantic"),
    PSYCHEDELIC_ROCK("UC96BpCnF38tLoGw7xbchWGg", "Psychedelic Rock"),
    RIDDIM("UCDVP78v2uLkhwGr7BHl6Pag", "Riddim");

    private static final Logger log = MorphiaLoggerFactory.get(YoutubeMusicGenre.class);
    private static final String DATABASE_NAME = "autoplaylists";
    private final String channelId;
    private final String readableName;

    YoutubeMusicGenre(String channelId, String readableName) {
        this.channelId = channelId;
        this.readableName = readableName;
    }

    public String getId() {
        return  this.channelId;
    }

    public String getLink() {
        return "https://www.youtube.com/channel/" + this.channelId;
    }

    public String getReadableName() {
        return this.readableName;
    }

    public ArrayList<YoutubePlaylist> getPlaylists() {
        return YoutubeAPI.getPlaylistsFromChannel(this.channelId);
    }

    public ArrayList<YoutubeVideo> getVideos() {
        return getVideos(Integer.MAX_VALUE);
    }

    public ArrayList<YoutubeVideo> getVideos(int max) {
        ArrayList<YoutubeVideo> result = new ArrayList<>();

        ArrayList<YoutubePlaylist> list = getPlaylists();
        Collections.shuffle(getPlaylists());

        for(YoutubePlaylist playlist : list) {
            result.addAll(YoutubeAPI.getVideosFromPlaylist(playlist, max));
            if(result.size() > max) {
                break;
            }
        }

        log.info("All videos for " + this.name() + " received.");
        return result;
    }

    public AutoPlaylist getMongoPlaylist(){
        AutoPlaylist result = AutoPlaylist.getAutoPlaylistByName(this.name());
        return result;
    }

    public void saveVideoIds() {
        log.info("Start saving IDs for " + this.name() + ".");
        ArrayList<String> ids = new ArrayList<>();
        for (YoutubeVideo video : this.getVideos()) {
            ids.add(video.getId());
        }
        /* FIXME: TODO: Store Data to database
        try {
            HashSet<String> hs = new HashSet<>(ids);
            ids.clear();
            ids.addAll(hs);

            TextUtils.writeTxt(
                    ids,
                    this.getFilename(),
                    "./pothi_discord_music/genres/"
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        */
    }

    public static void saveVideosForAllGenres() {
        long startTime = System.currentTimeMillis();
        int count = 1;

        for (YoutubeMusicGenre genre : YoutubeMusicGenre.values()) {
            genre.saveVideoIds();

            log.info("Genre " + count + "/" + YoutubeMusicGenre.values().length + " finished.");
            count++;
        }
        log.info("All genres saved. Time elapsed: "
                + TextUtils.formatMillis(System.currentTimeMillis()-startTime));
    }

    public static ArrayList<YoutubeMusicGenre> getGenresBySearch(String search) {
        ArrayList<String> names = new ArrayList<>();
        for (YoutubeMusicGenre genre : YoutubeMusicGenre.values()) {
            names.add(genre.name());
        }

        List<ExtractedResult> tmpResults =  FuzzySearch.extractSorted(search, names, 70);

        ArrayList<YoutubeMusicGenre> result = new ArrayList<>();

        for (ExtractedResult res : tmpResults) {
            log.info(res.getString() + ": " + res.getScore());
            result.add(YoutubeMusicGenre.valueOf(res.getString()));
        }

        if (search.toLowerCase().equals("all")) {
            result.clear();
            for (YoutubeMusicGenre genre : YoutubeMusicGenre.values()) {
                result.add(genre);
            }
        }
        log.info(result.toString());
        return result;
    }

}
