package pothi_discord_music.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;
import pothi_discord_music.Main;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class TrackScheduler extends AudioEventAdapter {
    private static final Logger log = LoggerFactory.getLogger(TrackScheduler.class);

    public final BlockingQueue<AudioTrack> queue;
    public final AudioPlayer player;

    public final GuildMusicManager musicManager;

    private int skipRequestsCounter = 0;

    public TrackScheduler(GuildMusicManager musicManager) {
        this.musicManager = musicManager;
        this.player = musicManager.player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) throws InterruptedException {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
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
        Guild guild = musicManager.guild;
        VoiceChannel vc = guild.getMember(guild.getJDA().getSelfUser()).getVoiceState().getChannel();
        if(vc == null) {
            return;
        }
        musicManager.checkIfShouldPause(vc, this);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        nextTrack();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        nextTrack();
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.

        this.skipRequestsCounter = 0;
        player.startTrack(getNextTrack(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public AudioTrack getNextTrack(){
        AudioTrack track = null;

        int loopCounter = 0;
        while (track == null && loopCounter < 30) {
            loopCounter++;
            track = queue.poll();
            if (track == null && !(musicManager.playlist == null || musicManager.playlist.size() == 0)) {
                String key = musicManager.playlist.getRandomElement();
                log.info("Next Track from: " + key);
                MyAudioLoadResultHandler handler = new MyAudioLoadResultHandler();

                try {
                    Main.getDiscordBotByJDA(musicManager.guild.getJDA())
                            .getPlayerManager().loadItem(key, handler).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                track = handler.getTrack();
            }
        }
        return track;
    }

    public int getSkipRequestsCounter() {return this.skipRequestsCounter;}

    public synchronized void increaseSkipRequestCounter() {
        this.skipRequestsCounter++;
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
