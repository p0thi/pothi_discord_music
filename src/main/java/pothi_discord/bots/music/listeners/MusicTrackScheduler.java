package pothi_discord.bots.music.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import oshi.jna.platform.unix.solaris.LibKstat;
import pothi_discord.Main;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;
import net.dv8tion.jda.core.entities.VoiceChannel;
import pothi_discord.listeners.TrackScheduler;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class MusicTrackScheduler extends AudioEventAdapter implements TrackScheduler {
    private static final Logger log = MorphiaLoggerFactory.get(Main.class);

    public final Deque<AudioTrack> queue;
    public final AudioPlayer player;

    public final GuildMusicManager musicManager;

    private int skipRequestsCounter = 0;

    private String lastPlaylistOwnerId = null;
    private String lastPlaylistName = null;



    public MusicTrackScheduler(GuildMusicManager musicManager) {
        this.musicManager = musicManager;
        this.player = musicManager.player;
        this.queue = new LinkedList<>();
    }

    public void queue(AudioTrack track) throws InterruptedException {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void queueFirst(AudioTrack track) throws InterruptedException {
        if (!player.startTrack(track, true)) {
            queue.offerFirst(track);
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
    public AudioTrack nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrack result = getNextTrack();
        this.skipRequestsCounter = 0;
        player.startTrack(result, false);
        return result;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public AudioTrack getNextTrack(){
        //TODO user playlists
        AudioTrack track = null;

        int loopCounter = 0;
        while (track == null && loopCounter < 30) {
            loopCounter++;
            track = queue.poll();

            if (track == null && !(musicManager.playlist == null || musicManager.playlist.size() == 0)) {
                //the key is the identifier
                String[] tuple = musicManager.getNextIdentifier();
                lastPlaylistOwnerId = tuple[0];
                lastPlaylistName = tuple[1];
                String key = tuple[2];
                log.info("Next Track from: " + key);
                MyAudioLoadResultHandler handler = new MyAudioLoadResultHandler();

                try {
                    musicManager.bot.getDiscordBotByJDA(musicManager.guild.getJDA())
                            .getPlayerManager().loadItem(key, handler).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                track = handler.getTrack();
            }
            else {
                lastPlaylistOwnerId = musicManager.guild.getJDA().getSelfUser().getId();
                lastPlaylistName = null;
            }
        }
        return track;
    }

    public int getSkipRequestsCounter() {return this.skipRequestsCounter;}

    public synchronized void increaseSkipRequestCounter() {
        this.skipRequestsCounter++;
    }

    @Override
    public AudioPlayer getPlayer() {
        return player;
    }

    @Override
    public void queue(String identifier) {
        System.out.println("Wrong queue() called");
        MyAudioLoadResultHandler handler = new MyAudioLoadResultHandler();

        try {
            musicManager.bot.getDiscordBotByJDA(musicManager.guild.getJDA())
                    .getPlayerManager().loadItem(identifier, handler).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            queue(handler.getTrack());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public String getLastPlaylistOwnerId() {
        return lastPlaylistOwnerId;
    }

    public String getLastPlaylistName() {
        return lastPlaylistName;
    }
}
