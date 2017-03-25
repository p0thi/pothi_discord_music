package pothi_discord_music.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord_music.Main;
import pothi_discord_music.handlers.ExceptionHandler;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.utils.Param;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class MessageListener extends AbstractEventListener {
    private static final Logger log = MorphiaLoggerFactory.get(Main.class);

    private static final Pattern PATTERN = Pattern.compile("([\\p{L}\\p{Digit}_<>|])+");
    public static final AtomicLong TOTAL_MESSAGES = new AtomicLong();
    public static final AtomicLong TOTAL_COMMANDS = new AtomicLong();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String msg = event.getMessage().getContent();

        log.info("Message received. (" + msg + ")");

        // if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
        if (event.getAuthor().isBot()) {
            log.info(event.getGuild().getName() + " \t " + event.getAuthor().getName() + " \t " + event.getMessage().getRawContent());
            return;
        }

        TOTAL_MESSAGES.incrementAndGet();

        if (msg.length() <= Param.PREFIX().length()) {
            return;
        }

        if (msg.startsWith(Param.PREFIX())) {
            String[] words = msg.split("[ \r\n]");
            String content = event.getMessage().getContent().replaceFirst(Param.PREFIX(), "");


            Matcher matcher = PATTERN.matcher(content);

            if (matcher.find()) {
                MessageDeleter.deleteMessage(event.getMessage(), 10000);
                String command = matcher.group();
                TOTAL_COMMANDS.incrementAndGet();
                log.info("Command detected: " + command);

                if (Main.getGuildCommandManagers().get(event.getGuild().getId()).hasCommand(command)) {
                    try {
                        Main.getGuildCommandManagers().get(event.getGuild().getId()).getCommand(command).action(event, words);
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

        GuildMusicManager musicManager = Main.getGuildAudioPlayer(guild);
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
        GuildMusicManager manager = Main.getGuildAudioPlayer(guild);

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
        GuildMusicManager manager = Main.getGuildAudioPlayer(guild);

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
        GuildMusicManager manager = Main.getGuildAudioPlayer(guild);

        if (!member.getUser().getId().equals(event.getJDA().getSelfUser().getId())
                && !(channelJoined.getId().equals(botChannel.getId()) || channelLeft.getId().equals(botChannel.getId()))) {
            return;
        }

        manager.checkIfShouldPause(botChannel, this);

    }


    @Override
    public void onReady(ReadyEvent event) {
        super.onReady(event);
        event.getJDA().getPresence().setGame(Game.of("[" + Main.getInstance(event.getJDA()).getShardInfo().getShardId() + "] Say " + Param.PREFIX() + "help"));

        User owner = event.getJDA().getUserById(Param.OWNER_ID());
        ExceptionHandler.setOwner(owner);
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        event.getJDA().getPresence().setGame(Game.of("[" + Main.getInstance(event.getJDA()).getShardInfo().getShardId() + "] Say " + Param.PREFIX() + "help"));
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        // PlayerRegistry.destroyPlayer(event.getGuild()); TODO

        for(Guild guild : event.getJDA().getGuilds()) {
            GuildMusicManager manager = Main.getGuildAudioPlayer(guild);
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
}
