package pothi_discord.bots.sound.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord.bots.sound.managers.audio.GuildSoundManager;
import pothi_discord.listeners.TrackScheduler;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

import java.io.File;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class SoundTrackScheduler extends AudioEventAdapter implements TrackScheduler {
    private static final Logger log = LoggerFactory.getLogger(SoundTrackScheduler.class);

    public final AudioPlayer player;

    public final GuildSoundManager soundManager;
    public VoiceChannel vc = null;

    public SoundTrackScheduler(GuildSoundManager musicManager) {
        this.soundManager = musicManager;
        this.player = musicManager.player;

    }

    @Override
    public void queue(String identifier) {
        //connectedCommands.incrementAndGet();
        soundManager.bot.getDiscordBotByJDA(soundManager.guild.getJDA())
                .getPlayerManager().loadItem(identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                new Thread(() -> {
                    GuildData guildData = GuildData.getGuildDataById(soundManager.guild.getId());
                    player.setVolume(guildData.getAudioCommandsStartVolume());
                }).start();
                player.playTrack(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                log.info("######################################### Could not load Track 1");
            }

            @Override
            public void noMatches() {
                log.info("######################################### Could not load Track 2");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                log.info("######################################### Could not load Track 3");
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        super.onPlayerPause(player);
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        super.onPlayerResume(player);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        super.onTrackStart(player, track);
        Guild guild = soundManager.guild;
        VoiceChannel vc = guild.getMember(guild.getJDA().getSelfUser()).getVoiceState().getChannel();
        if(vc == null) {
            return;
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {

    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {

    }


    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            soundManager.guild.getAudioManager().closeAudioConnection();
            vc = null;
        }
        try {
            File myFile = new File(track.getInfo().uri);
            myFile.delete();
        } catch (Exception e) {
            log.error("Could not delete File ", e);
        }
    }

    @Override
    public AudioPlayer getPlayer() {
        return player;
    }


    private class MyAudioLoadResultHandler implements AudioLoadResultHandler {
        private AudioTrack track;
        MyAudioLoadResultHandler() {
            this.track = null;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            this.track = track;
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            this.track = playlist.getTracks().get(new Random().nextInt(playlist.getTracks().size()));
        }

        @Override
        public void noMatches() {}

        @Override
        public void loadFailed(FriendlyException exception) {
            log.error("FriendlyException while loading. ", exception);
        }

        public AudioTrack getTrack() {
            return this.track;
        }
    }
}
