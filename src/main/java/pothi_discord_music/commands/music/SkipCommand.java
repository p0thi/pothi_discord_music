package pothi_discord_music.commands.music;

import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord_music.Main;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.listeners.TrackScheduler;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.utils.GuildData;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.database.guilddata.MongoGuilddata;
import pothi_discord_music.utils.database.guilddata.permissions.GuildPermissionDBObject;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class SkipCommand extends GuildCommand {
    private static final Logger log = LoggerFactory.getLogger(SkipCommand.class);

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        User user = event.getAuthor();

        GuildMusicManager manager = Main.getGuildAudioPlayer(guild);
        TextChannel channel = event.getChannel();
        VoiceChannel membersVoiceChannel = guild.getMember(user).getVoiceState().getChannel();
        VoiceChannel voiceChannel = guild.getAudioManager().getConnectedChannel();

        if(membersVoiceChannel == null ||
                !voiceChannel.getId().equals(membersVoiceChannel.getId())) {
            channel.sendMessage("Du musst dich im selben Channel wie der Bot befinden.").queue(new MessageDeleter());
            return;
        }

        if(!checkPermission(guild, user) && !checkSkipCount(manager, guild, channel)){
            return;
        }

        GuildMusicManager musicManager = Main.getGuildAudioPlayer(guild);

        String content;

        if (musicManager.player.getPlayingTrack() != null) {
            content  = "Lied wird übersprungen: **" + musicManager.player.getPlayingTrack().getInfo().title + "**";
        }
        else {
            content = "Neues Lied wird gestartet.";
        }

        musicManager.scheduler.nextTrack();

        log.info(this.getClass().getSimpleName() + ": " + content);
        channel.sendMessage(content).queue(new MessageDeleter());
    }

    @Override
    public String helpString() {
        return "Befehl zum Überspringen des Liedes, das aktuell gespielt wird.";
    }

    private boolean checkSkipCount(GuildMusicManager manager, Guild guild, TextChannel textChannel) {
        TrackScheduler scheduler = manager.scheduler;
        VoiceChannel channel = guild.getAudioManager().getConnectedChannel();

        if (channel == null) {
            return false;
        }

        scheduler.increaseSkipRequestCounter();

        int skipPercent = GuildData.ALL_GUILD_DATAS.get(guild.getId()).getGuildDBObject().getSongSkipPercent();
        int skipCount = scheduler.getSkipRequestsCounter();
        double memberCount = (double)channel.getMembers().size() - 1;
        if(skipCount >= memberCount * ((double)skipPercent / 100.0)) {
            log.info("Skipp accepted: " + skipCount + " >= " + memberCount * ((double)skipPercent / 100.0));
            return true;
        }
        else {
            textChannel.sendMessage(
                    String.format(("Mindestens %d%% aller aktuellen Zuhörer müssen dem Überspringen mit '%sskip' zustimmen." +
                            "\nAktuell: **%d/%d**."),
                            skipPercent, Param.PREFIX(), skipCount, (int)memberCount))
                    .queue(new MessageDeleter());
            return false;
        }

    }

    @Override
    public boolean checkPermission(Guild guild, User user) {
        boolean result = Param.isDeveloper(user.getId());

        GuildData guildData = GuildData.ALL_GUILD_DATAS.get(guild.getId());
        MongoGuilddata mongoGuilddata = guildData.getGuildDBObject();
        GuildPermissionDBObject permissions = mongoGuilddata.getPermissions();

        result = result || permissions.canUserInstaskip(guild, user.getId());
        result = result || permissions.hasUserPermissionForCommand(guild, user.getId(), getName());

        return result;
    }

}
