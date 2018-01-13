package pothi_discord.bots.music.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.bots.BotShard;
import pothi_discord.Main;
import pothi_discord.handlers.ExceptionHandler;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.listeners.AbstractEventListener;
import pothi_discord.bots.music.DiscordMusicBotShard;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class MusicBotMessageListener extends AbstractEventListener {
    private static final Logger log = MorphiaLoggerFactory.get(Main.class);

    private DiscordMusicBotShard shard;

    public MusicBotMessageListener(DiscordMusicBotShard shard) {
        this.shard = shard;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();

        log.info("Message received. (" + msg + ")");

        // if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
        if (event.getAuthor().isBot()) {
            log.info(event.getGuild().getName() + " \t " + event.getAuthor().getName() + " \t " + event.getMessage().getContentRaw());
            return;
        }

        TOTAL_MESSAGES.incrementAndGet();

        if (msg.length() <= Param.PREFIX().length()) {
            return;
        }

        if (msg.startsWith(Param.PREFIX())) {
            String[] words = msg.split("[ \r\n]");
            String content = event.getMessage().getContentDisplay().replaceFirst(Param.PREFIX(), "");


            Matcher matcher = PATTERN.matcher(content);

            if (matcher.find()) {
                MessageDeleter.deleteMessage(event.getMessage(), 10000);
                String command = matcher.group();
                TOTAL_COMMANDS.incrementAndGet();
                log.info("Command detected: " + command);

                if (shard.getMyBot().getGuildCommandManagers().get(event.getGuild().getId()).hasCommand(command)) {
                    try {
                        shard.getMyBot().getGuildCommandManagers().get(event.getGuild().getId())
                                .getCommand(command).action(event, words, shard);
                    } catch (Exception e) {
                        log.warning("Could not executeAllTasks commsnd: " + command);
                        e.printStackTrace();
                        new ExceptionHandler(e);
                    }
                } else {
                    log.info("(No command): " + msg);
                }
            }
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        log.info("Joined Guidl: " + guild.getName());

        GuildMusicManager musicManager = (GuildMusicManager) shard.getMyBot().getGuildAudioPlayer(guild);
        try {
            musicManager.connectAndPlay();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Guild guild = event.getGuild();
        VoiceChannel botChannel = guild.getMember(event.getJDA().getSelfUser()).getVoiceState().getChannel();

        if (botChannel == null) {
            return;
        }

        Member member = event.getMember();
        VoiceChannel channel = event.getChannelJoined();
        GuildMusicManager manager = (GuildMusicManager) shard.getMyBot().getGuildAudioPlayer(guild);

        if (!member.getUser().getId().equals(event.getJDA().getSelfUser().getId())
                && !channel.getId().equals(botChannel.getId())) {
            return;
        }

        manager.checkIfShouldPause(botChannel, this);
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        Guild guild = event.getGuild();
        VoiceChannel botChannel = guild.getMember(event.getJDA().getSelfUser()).getVoiceState().getChannel();

        if (botChannel == null) {
            return;
        }

        Member member = event.getMember();
        VoiceChannel channel = event.getChannelLeft();
        GuildMusicManager manager = (GuildMusicManager) shard.getMyBot().getGuildAudioPlayer(guild);

        if (!member.getUser().getId().equals(event.getJDA().getSelfUser().getId())
                && !channel.getId().equals(botChannel.getId())) {
            return;
        }

        manager.checkIfShouldPause(botChannel, this);
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        Guild guild = event.getGuild();
        VoiceChannel botChannel = guild.getMember(event.getJDA().getSelfUser()).getVoiceState().getChannel();

        if (botChannel == null) {
            return;
        }

        VoiceChannel channelLeft = event.getChannelLeft();
        VoiceChannel channelJoined = event.getChannelJoined();
        Member member = event.getMember();
        GuildMusicManager manager = (GuildMusicManager) shard.getMyBot().getGuildAudioPlayer(guild);

        if (!member.getUser().getId().equals(event.getJDA().getSelfUser().getId())
                && !(channelJoined.getId().equals(botChannel.getId()) || channelLeft.getId()
                .equals(botChannel.getId()))) {
            return;
        }

        manager.checkIfShouldPause(botChannel, this);

    }


    @Override
    public void onReady(ReadyEvent event) {
        super.onReady(event);
        event.getJDA().getPresence().setGame(Game.of(Game.GameType.DEFAULT, "[" +
                shard.getMyBot().getInstance(event.getJDA()).getShardInfo().getShardId() + "] Say "
                + Param.PREFIX() + "help"));

        User owner = event.getJDA().getUserById(Param.OWNER_ID());
        ExceptionHandler.setOwner(owner);
    }

    @Override
    public BotShard getShard() {
        return this.shard;
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        event.getJDA().getPresence().setGame(Game.of(Game.GameType.DEFAULT, "[" + shard.getMyBot().getInstance(event.getJDA())
                .getShardInfo().getShardId() + "] Say " + Param.PREFIX() + "help"));
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        // PlayerRegistry.destroyPlayer(event.getGuild()); TODO

        for(Guild guild : event.getJDA().getGuilds()) {
            GuildMusicManager manager = (GuildMusicManager) shard.getMyBot().getGuildAudioPlayer(guild);
            // GuildPlayer player = PlayerRegistry.getExisting(guild);
            AudioPlayer player = manager.player;

            if (player == null) {
                continue;
            }

            if (player.getPlayingTrack() != null && !player.isPaused()) {
                System.err.println("Resetting player in " + guild.getName());
                player.setPaused(true);
                player.setPaused(false);
            }
        }
    }

    @Override
    public void onUserGameUpdate(UserGameUpdateEvent event) {
        User user = event.getUser();
        if (user.isBot()) {
            return;
        }
        Userdata userdata = Userdata.getUserdata(user.getId());

        Game newGame;
        try {
            newGame = event.getGuild().getMember(event.getUser()).getGame();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return;
        }

        try {
            userdata.storeGame(newGame);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
            return;
        }
    }

    @Override
    public void onGenericEvent(Event event) {
        log.debug("Event received: " + event.getClass().getSimpleName());
    }
}
