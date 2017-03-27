package pothi_discord.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
public interface TrackScheduler {
    AudioPlayer getPlayer();
    void queue(String identifier);
}
