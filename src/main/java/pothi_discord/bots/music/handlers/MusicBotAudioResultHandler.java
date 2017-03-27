package pothi_discord.bots.music.handlers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pothi_discord.bots.music.listeners.MusicTrackScheduler;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class MusicBotAudioResultHandler implements AudioLoadResultHandler {

    private MusicTrackScheduler scheduler;

    public MusicBotAudioResultHandler(MusicTrackScheduler scheduler) {
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
