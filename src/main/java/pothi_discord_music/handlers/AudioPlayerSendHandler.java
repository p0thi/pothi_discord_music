package pothi_discord_music.handlers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer player;
    private AudioFrame frame;

    public AudioPlayerSendHandler(AudioPlayer player) {
        this.player = player;
    }

    public boolean canProvide() {
        frame = player.provide();
        return frame != null;
    }

    public byte[] provide20MsAudio() {
        return frame.data;
    }

    public boolean isOpus() {
        return true;
    }


}
