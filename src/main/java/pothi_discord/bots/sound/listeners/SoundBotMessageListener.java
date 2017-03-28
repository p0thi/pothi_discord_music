package pothi_discord.bots.sound.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord.bots.BotShard;
import pothi_discord.bots.sound.DiscordSoundBotShard;
import pothi_discord.bots.sound.commands.audio.PlayerCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.listeners.AbstractEventListener;
import pothi_discord.managers.GuildAudioManager;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommand;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class SoundBotMessageListener extends AbstractEventListener {
    private static final Logger log = LoggerFactory.getLogger(SoundBotMessageListener.class);



    private DiscordSoundBotShard shard;

    public SoundBotMessageListener(DiscordSoundBotShard shard) {
        this.shard = shard;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String msg = event.getMessage().getContent();

        log.info("Message received. (" + msg + ")");

        // if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
        if (event.getAuthor().isBot()) {
            log.info(event.getGuild().getName() + " \t " + event.getAuthor().getName() + " \t " + event.getMessage().getRawContent());
            return;
        }

        if (msg.length() <= Param.PREFIX().length()) {
            return;
        }

        if (msg.startsWith(Param.PREFIX())) {
            String[] words = msg.split("[ \r\n]");
            String content = event.getMessage().getContent().replaceFirst(Param.PREFIX(), "");


            Matcher matcher = PATTERN.matcher(content);

            if (matcher.find()) {
                String command = matcher.group();
                TOTAL_COMMANDS.incrementAndGet();
                log.info("Command detected: " + command);

                if (shard.getMyBot().getGuildCommandManagers().get(event.getGuild().getId()).hasCommand(command)) {
                    try {
                        MessageDeleter.deleteMessage(event.getMessage(), 10000);
                        shard.getMyBot().getGuildCommandManagers().get(event.getGuild().getId()).getCommand(command).action(event, words, shard);
                    } catch (Exception e) {
                        log.warn("Could not executeAllTasks commsnd: " + command);
                        e.printStackTrace();
                    }
                } else {
                    GuildData guildData = GuildData.getGuildDataById(event.getGuild().getId());
                    boolean isAudioCommand = false;
                    String fileId = null;

                    for(SoundCommand soundCommand : guildData.getSoundCommands()) {
                        if(soundCommand.getCommand().toLowerCase().equals(command.toLowerCase())) {
                            isAudioCommand = true;
                            fileId = soundCommand.getFileId();
                            break;
                        }
                    }

                    if (isAudioCommand) {
                        MessageDeleter.deleteMessage(event.getMessage(), 0);
                        new PlayerCommand(fileId).action(event, words, shard);
                    } else {
                        log.info("(No command): " + msg);
                    }
                }
            }
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();
        log.info("Joined Guidl: " + guild.getName());

        shard.getMyBot().getGuildAudioPlayer(guild);
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId())) {
            try {
                event.getGuild().getAudioManager().openAudioConnection(event.getChannelLeft());
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onUserGameUpdate(UserGameUpdateEvent event) {

    }

    @Override
    public void onReady(ReadyEvent event) {
        super.onReady(event);
        event.getJDA().getPresence().setGame(Game.of("[" + shard.getMyBot().getInstance(event.getJDA()).getShardInfo().getShardId() + "] Say " + Param.PREFIX() + "help"));
        /*
        for (User user : event.getJDA().getUsers()) {
            new Thread(() -> {
                Member member = user.getMutualGuilds().get(0).getMember(user);
                if(member.getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
                    return;
                }

                MongoUserdata mongoUserdata = MongoUserdata.getUserdataById(user.getId());
                mongoUserdata.setGame(member.getGame());
            }).start();
        }
        */
    }

    @Override
    public BotShard getShard() {
        return shard;
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        event.getJDA().getPresence().setGame(Game.of("[" + shard.getMyBot().getInstance(event.getJDA()).getShardInfo().getShardId() + "] Say " + Param.PREFIX() + "help"));
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        // PlayerRegistry.destroyPlayer(event.getGuild()); TODO

        for(Guild guild : event.getJDA().getGuilds()) {
            GuildAudioManager manager = shard.getMyBot().getGuildAudioPlayer(guild);
            // GuildPlayer player = PlayerRegistry.getExisting(guild);
            AudioPlayer player = manager.getPlayer();

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
