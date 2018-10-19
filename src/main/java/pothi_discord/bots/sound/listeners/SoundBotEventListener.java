package pothi_discord.bots.sound.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.Main;
import pothi_discord.bots.BotShard;
import pothi_discord.bots.sound.DiscordSoundBotShard;
import pothi_discord.bots.sound.commands.audio.PlayerCommand;
import pothi_discord.bots.sound.managers.audio.GuildSoundManager;
import pothi_discord.commands.Command;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.listeners.AbstractEventListener;
import pothi_discord.listeners.TrackScheduler;
import pothi_discord.managers.GuildAudioManager;
import pothi_discord.managers.GuildCommandManager;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommandEntry;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class SoundBotEventListener extends AbstractEventListener {
    private static final Logger log = MorphiaLoggerFactory.get(SoundBotEventListener.class);



    private DiscordSoundBotShard shard;

    public SoundBotEventListener(DiscordSoundBotShard shard) {
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

        if (msg.length() <= Param.PREFIX().length()) {
            return;
        }

        if (msg.startsWith(Param.PREFIX())) {
            String[] words = msg.split("[ \r\n]");
            String content = event.getMessage().getContentDisplay().replaceFirst(Param.PREFIX(), "");


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
                        log.warning("Could not executeAllTasks commsnd: " + command);
                        e.printStackTrace();
                    }
                } else {
                    GuildData guildData = GuildData.getGuildDataByGuildId(event.getGuild().getId());
                    boolean isAudioCommand = false;
                    String fileId = null;

                    if (command.toLowerCase().equals("random")) {
                        isAudioCommand = true;
                        List<SoundCommandEntry> allCommands = guildData.getSoundCommands().getSoundCommandEntries();
                        fileId = allCommands.get(new Random().nextInt(allCommands.size())).getFileId();
                    }
                    else {
                        for (SoundCommandEntry soundCommand : guildData.getSoundCommands().getSoundCommandEntries()) {
                            if (soundCommand.getCommand().toLowerCase().equals(command.toLowerCase())) {
                                isAudioCommand = true;
                                fileId = soundCommand.getFileId();
                                break;
                            }
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
        log.info("Joined Guild: " + guild.getName());

        shard.getMyBot().getGuildAudioPlayer(guild);
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        Guild guild = event.getGuild();

        VoiceChannel voiceChannel = event.getChannelJoined();

        Member member = event.getMember();
        Userdata userdata = Userdata.getUserdata(member.getUser().getId());
        String joinCommand = userdata.getJoinCommand();

        if (joinCommand == null) {
            return;
        }
        GuildData guildData = GuildData.getGuildDataByGuildId(guild.getId());

        String fileId = null;
        if (joinCommand.equals("random")) {
            List<SoundCommandEntry> allCommands = guildData.getSoundCommands().getSoundCommandEntries();
            fileId = allCommands.get(new Random().nextInt(allCommands.size())).getFileId();
        }
        else {
            for (SoundCommandEntry soundCommand : guildData.getSoundCommands().getSoundCommandEntries()) {
                if (soundCommand.getCommand().toLowerCase().equals(joinCommand.toLowerCase())) {
                    fileId = soundCommand.getFileId();
                    break;
                }
            }
        }

        if (fileId == null) {
            return;
        }

        TrackScheduler scheduler = Main.soundBot.getGuildAudioPlayer(guild).getScheduler();

        new PlayerCommand(fileId).action(guild, member.getUser(), voiceChannel, scheduler);

    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        SoundTrackScheduler scheduler = (SoundTrackScheduler) shard.getMyBot().getGuildAudioPlayer(event.getGuild()).getScheduler();
        if (event.getMember().getUser().getId().equals(event.getJDA().getSelfUser().getId())
                && event.getChannelLeft().equals(scheduler.vc)) {
            try {
                event.getGuild().getAudioManager().openAudioConnection(event.getChannelLeft());
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        super.onReady(event);
        event.getJDA().getPresence().setGame(Game.of(Game.GameType.DEFAULT,"[" + shard.getMyBot().getInstance(event.getJDA()).getShardInfo().getShardId() + "] Say " + Param.PREFIX() + "help"));
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
        event.getJDA().getPresence().setGame(Game.of(Game.GameType.DEFAULT, "[" + shard.getMyBot().getInstance(event.getJDA()).getShardInfo().getShardId() + "] Say " + Param.PREFIX() + "help"));
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
