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
import pothi_discord.utils.database.morphia.autoplaylists.AutoPlaylist;
import pothi_discord.utils.database.morphia.guilddatas.Permissions;
import pothi_discord.utils.database.morphia.MongoAudioTrack;
import pothi_discord.utils.database.morphia.userdata.UserPlaylist;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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

    public String[] getNextIdentifier() {
        String[] autoPlaylistElement = new String[] {guild.getJDA().getSelfUser().getId(), null, playlist.getRandomElement().getIdentifier()};
        Random rand = new Random();
        VoiceChannel vc = guild.getAudioManager().getConnectedChannel();

        GuildData guildData = GuildData.getGuildDataByGuildId(guild.getId());

        if (vc == null) {
            return autoPlaylistElement;
        }

        ArrayList<String[]> activePlaylists = new ArrayList<>();

        for (Member member : vc.getMembers()) {
            Userdata userdata = Userdata.getUserdata(member.getUser().getId());
            UserPlaylist activePlaylist = userdata.getActivePlaylist();

            if (activePlaylist != null && activePlaylist.getTracks().size() > 0) {
                long maxTrackLength = guildData.getPermissions()
                        .getMaxSongLengthOfUser(guild, member.getUser().getId());
                for (MongoAudioTrack track : activePlaylist.getTracks()) {
                    if (track.getLength() <= maxTrackLength) {
                        String[] tuple = new String[3];
                        tuple[0] = member.getUser().getId();
                        tuple[1] = activePlaylist.getName();
                        tuple[2] = track.getIdentifier();
                        activePlaylists.add(tuple);
                    }
                }
            }
        }
        int userListSize = Math.min(activePlaylists.size(), 100);

        if (userListSize <= 0) {
            return autoPlaylistElement;
        }

        int autoListSize = Math.min(playlist.getContent().size(), 100);

        int combined = userListSize + autoListSize;
        int randInt = rand.nextInt(combined + 1);

        if (randInt <= autoListSize) {
            return autoPlaylistElement;
        }
        else {
            return activePlaylists.get(rand.nextInt(activePlaylists.size()));
        }
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
        Permissions gpo = GuildData.getGuildDataByGuildId(guild.getId()).getPermissions();
        bot.getDiscordBotByJDA(channel.getJDA())
                .getPlayerManager().loadItemOrdered(this, value, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (gpo.getMaxSongLengthOfUser(guild, userId) >= track.getDuration()) {
                    if (queueTrack(track)) {
                        sendMessage("Zur Warteschlange hinzugefügt: **" + track.getInfo().title
                                + "**", channel, skipMessages);
                    }
                }
                else {
                    sendMessage(String.format("Das Lied ist zu lang. " +
                            "Du darft nur Tracks mit maximal **%d Minuten** hinzufügen",
                            (gpo.getMaxSongLengthOfUser(guild, userId) / 60000)), channel, skipMessages);
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
    public ArrayList<Member> getMembersInChannelWithoutBots() {
        return getMembersInChannelWithoutBots(guild.getMember(guild.getJDA().getSelfUser()).getVoiceState().getChannel());
    }

    public ArrayList<Member> getMembersInChannelWithoutBots(VoiceChannel voiceChannel) {
        JDA jda = guild.getJDA();

        ArrayList<Member> membersWithoutBot = new ArrayList<>();

        if (voiceChannel == null) {
            return membersWithoutBot;
        }

        for(Member member : voiceChannel.getMembers()) {
            if(!(member.getUser().isBot() || member.getUser().getId().equals(jda.getSelfUser().getId()))) {
                membersWithoutBot.add(member);
            }
        }
        return membersWithoutBot;
    }

    public void checkIfShouldPause(VoiceChannel channel, Object caller) {
        // FIXME: pauses when not expected
        if(channel == null) {
            return;
        }

        ArrayList<Member> membersWithoutBot = getMembersInChannelWithoutBots();

        int memberCount = membersWithoutBot.size();

        log.info("From: " + caller.getClass().getSimpleName() + " -> Checking for autopause in " + guild.getName() + ". (Members in Channel without bot: " + memberCount + ")");
        boolean wasPaused = player.isPaused();

        boolean shouldPause = memberCount <= 0;
        log.info("Pausing in " + guild.getName() + ": " + shouldPause);

//        GuildAudioManager manager = this.bot.getGuildAudioPlayer(channel.getGuild());
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
        this.playlist = AutoPlaylist.getAutoPlaylistByName(genre.name());
    }

    public void loadDefaultPlaylist(){
        this.playlist = GuildData.getGuildDataByGuildId(guild.getId()).getDefaultAutoplaylist();
    }

    public TextChannel getActiveTextChannel() {
        if (currentTC != null) {
            return currentTC;
        } else {
            log.warning("No currentTC in " + guild + "! Returning public channel...");
            return guild.getDefaultChannel();
        }
    }

    public void setCurrentTC(TextChannel currentTC) {
        this.currentTC = currentTC;
    }

    public TextChannel getCurrentTC() {
        return currentTC;
    }
}