package pothi_discord.bots.music.managers.audio;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.bots.Bot;
import pothi_discord.Main;
import pothi_discord.bots.music.listeners.MusicTrackScheduler;
import pothi_discord.handlers.AudioPlayerSendHandler;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.listeners.TrackScheduler;
import pothi_discord.managers.GuildAudioManager;
import pothi_discord.utils.audio.VideoSelection;
import pothi_discord.utils.audio.YoutubeMusicGenre;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.Permissions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager implements GuildAudioManager{
    private static final Logger log = MorphiaLoggerFactory.get(Main.class);

    /**
     * Audio player for the guild.
     */
    public final AudioPlayer player;
    /**
     * Track scheduler for the player.
     */
    public final MusicTrackScheduler scheduler;
    public AutoPlaylist playlist;
    public Bot bot;

    public final Guild guild;

    public final HashMap<String, VideoSelection> selections = new HashMap<>();

    private TextChannel currentTC;

    /**
     * Creates a player and a track scheduler.
     * @param manager Audio player manager to use for creating the player.
     */
    public GuildMusicManager(Guild guild, AudioPlayerManager manager, Bot bot) {
        this.bot = bot;
        player = manager.createPlayer();
        scheduler = new MusicTrackScheduler(this);
        this.guild = guild;
        player.addListener(scheduler);
        loadDefaultPlaylist();
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


    public void playRequestByKey(Guild guild, String userId, String value, TextChannel channel, boolean skipMessages) {
        Permissions gpo = GuildData.getGuildDataById(guild.getId()).getPermissions();
        bot.getDiscordBotByJDA(channel.getJDA())
                .getPlayerManager().loadItemOrdered(this, value, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if(queueTrack(track)) {
                    sendMessage("Zur Warteschlange hinzugefügt: **" + track.getInfo().title
                            + "**", channel, skipMessages);
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                int addedCounter = 0;
                if(playlist.getTracks().size() > gpo.getMaxPlaylistSizeOfUser(guild, userId)) {
                    sendMessage("Die Playlist ist zu lang. Maximal "
                            + gpo.getMaxPlaylistSizeOfUser(guild, userId) + " Titel", channel, skipMessages);
                    return;
                }
                for(AudioTrack track : playlist.getTracks()) {
                    if (queueTrack(track)) {
                        addedCounter++;
                    }
                }
                sendMessage(addedCounter + " der " + playlist.getTracks().size()
                        + " verfügbaren Titel aus der Playlist **\"" + playlist.getName() + "\"** hinzugefügt.", channel, skipMessages);
            }

            @Override
            public void noMatches() {
                sendMessage("Ich habe nichts gefunden zu **" + value + "**.", channel, skipMessages);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                sendMessage("Ich konnte **" + exception.getMessage() + "** nicht hinzufügen.", channel, skipMessages);
            }
        });
    }


    private boolean queueTrack(AudioTrack track) {
        try {
            if(guild.getAudioManager().isConnected()){
                scheduler.queue(track);
            }
            else {
                connectAndPlay(track);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void connectAndPlay() throws InterruptedException {
        connectAndPlay(this.scheduler.getNextTrack());
    }

    public void connectAndPlay(AudioTrack track) throws InterruptedException {
        bot.connectToVoiceChannel(guild.getAudioManager());
        if(track != null) {
            this.scheduler.queue(track);
        }
    }

    private void sendMessage(String msg, TextChannel channel, boolean skip){
        if(!skip) {
            channel.sendMessage(msg).queue(new MessageDeleter());
        }
    }

    public void checkIfShouldPause(VoiceChannel channel, Object caller) {
        // FIXME: pauses when not expected
        if(channel == null) {
            return;
        }

        JDA jda = channel.getJDA();
        ArrayList<Member> membersWithoutBot = new ArrayList<>();

        Member bot = null;

        for(Member member : channel.getMembers()) {
            if(!member.getUser().getId().equals(jda.getSelfUser().getId())) {
                membersWithoutBot.add(member);
            }
        }

        int memberCount = membersWithoutBot.size();

        log.info("From: " + caller.getClass().getSimpleName() + " -> Checking for autopause in " + guild.getName() + ". (Members in Channel without bot: " + memberCount + ")");
        boolean wasPaused = player.isPaused();

        boolean shouldPause = memberCount < 1;

        GuildAudioManager manager = this.bot.getGuildAudioPlayer(channel.getGuild());
        if (shouldPause) {
            loadDefaultPlaylist();
        }
        else {
            AudioTrack at = player.getPlayingTrack();
            if (at == null) {
                scheduler.nextTrack();
            }
        }

        player.setPaused(shouldPause);

        if (wasPaused) {
            if (!shouldPause) {
                log.info("Resuming player in " + guild.getName() + " (Member joined empty Channel).");
            }
        }
        else {
            if (shouldPause) {
                log.info("Pausing player in " + guild.getName() + " (Empty Channel).");
            }
        }
    }

    public void setGenrePlaylist(YoutubeMusicGenre genre) {
        this.playlist = new AutoPlaylist(genre);
    }

    public void loadDefaultPlaylist(){
        this.playlist = GuildData.getGuildDataById(guild.getId()).getDefaultAutoplaylist();
    }

    public TextChannel getActiveTextChannel() {
        if (currentTC != null) {
            return currentTC;
        } else {
            log.warning("No currentTC in " + guild + "! Returning public channel...");
            return guild.getPublicChannel();
        }
    }

    public void setCurrentTC(TextChannel currentTC) {
        this.currentTC = currentTC;
    }

    public TextChannel getCurrentTC() {
        return currentTC;
    }
}