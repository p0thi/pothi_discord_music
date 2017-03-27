package pothi_discord.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import pothi_discord.handlers.AudioPlayerSendHandler;
import pothi_discord.listeners.TrackScheduler;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
public interface GuildAudioManager {

    AudioPlayer getPlayer();
    AudioPlayerSendHandler getSendHandler();
    TrackScheduler getScheduler();

}
