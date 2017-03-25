package pothi_discord_music.handlers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pothi_discord_music.listeners.TrackScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class AudioResultHandler implements AudioLoadResultHandler {

    private TrackScheduler scheduler;

    public AudioResultHandler(TrackScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        try {
            scheduler.queue(track);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        for(AudioTrack track  : playlist.getTracks()) {
            try {
                scheduler.queue(track);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void noMatches() {
    }

    @Override
    public void loadFailed(FriendlyException exception) {
    }
}
