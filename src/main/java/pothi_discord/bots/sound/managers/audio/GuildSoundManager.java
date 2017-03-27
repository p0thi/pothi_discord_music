package pothi_discord.bots.sound.managers.audio;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord.bots.Bot;
import pothi_discord.bots.sound.listeners.SoundTrackScheduler;
import pothi_discord.handlers.AudioPlayerSendHandler;
import pothi_discord.listeners.TrackScheduler;
import pothi_discord.managers.GuildAudioManager;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildSoundManager implements GuildAudioManager {
    private static final Logger log = LoggerFactory.getLogger(GuildSoundManager.class);

    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final SoundTrackScheduler scheduler;

    public final Guild guild;

    public Bot bot;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildSoundManager(Guild guild, AudioPlayerManager manager, Bot bot) {
        this.bot = bot;
        player = manager.createPlayer();
        this.guild = guild;
        scheduler = new SoundTrackScheduler(this);
        player.addListener(scheduler);
    }

    @Override
    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    @Override
    public TrackScheduler getScheduler() {
        return scheduler;
    }


}