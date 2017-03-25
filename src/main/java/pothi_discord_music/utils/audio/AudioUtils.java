package pothi_discord_music.utils.audio;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import pothi_discord_music.utils.lame.lowlevel.LameEncoder;
import pothi_discord_music.utils.lame.mp3.Lame;
import pothi_discord_music.utils.lame.mp3.MPEGMode;
import org.apache.commons.io.FileUtils;

import javax.sound.sampled.AudioFormat;
import java.io.*;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public class AudioUtils {
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private int sampleRate = 48000;
    private int channels = 2;
    private String filePath;
    private AudioFormat frmt = new AudioFormat(sampleRate, 16, 2, true, true);
    private long startTime;
    public Message message;
    public boolean recording =false;
    public User user;

    public AudioUtils() {
    }

    public File saveToFile() {

        File result = new File(filePath);

        if(!result.exists()) {
            try {
                result.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] tmp = pcmToMp3Bytes(byteArrayOutputStream.toByteArray());

        try {
            FileUtils.writeByteArrayToFile(result, tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public byte[] pcmToMp3Bytes(final byte[] pcm) {
        LameEncoder encoder = new LameEncoder(frmt, 64, MPEGMode.MONO, Lame.QUALITY_MIDDLE_LOW, false);
        ByteArrayOutputStream mp3 = new ByteArrayOutputStream();

        byte[] buffer = new byte[encoder.getPCMBufferSize()];

        int bytesToTransfer = Math.min(buffer.length, pcm.length);
        int bytesWritten;
        int currentPcmPosition = 0;
        while (0 < (bytesWritten = encoder.encodeBuffer(pcm, currentPcmPosition, bytesToTransfer, buffer))) {
            currentPcmPosition += bytesToTransfer;
            bytesToTransfer = Math.min(buffer.length, pcm.length - currentPcmPosition);

            mp3.write(buffer, 0, bytesWritten);
        }
        encoder.close();
        byte[] result = mp3.toByteArray();
        return result;
    }

    public void addBytes(byte[] pcm) {
        try {
            byteArrayOutputStream.write(pcm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        byteArrayOutputStream.reset();
        user = null;
        message = null;
        startTime = 0;
        recording = false;
        filePath = null;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFrmt(AudioFormat frmt) {
        this.frmt = frmt;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
