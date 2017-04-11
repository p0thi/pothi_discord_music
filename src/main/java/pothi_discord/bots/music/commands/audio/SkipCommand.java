package pothi_discord.bots.music.commands.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.bots.music.listeners.MusicTrackScheduler;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.listeners.TrackScheduler;
import pothi_discord.managers.GuildAudioManager;
import pothi_discord.permissions.PermissionManager;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class SkipCommand extends GuildCommand {

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        Guild guild = event.getGuild();
        User user = event.getAuthor();

        GuildAudioManager manager = shard.getMyBot().getGuildAudioPlayer(guild);
        TextChannel channel = event.getChannel();
        VoiceChannel membersVoiceChannel = guild.getMember(user).getVoiceState().getChannel();
        VoiceChannel voiceChannel = guild.getAudioManager().getConnectedChannel();

        if(membersVoiceChannel == null ||
                voiceChannel == null ||
                !voiceChannel.getId().equals(membersVoiceChannel.getId())) {
            channel.sendMessage("Du musst dich im selben Channel wie der Bot befinden.").queue(new MessageDeleter());
            return;
        }

        if(!checkPermission(event)){
            return;
        }

        if (! (PermissionManager.checkUserPermission(guild, user, "can-instant-skip") || checkSkipCount(manager, guild, channel))) {
            return;
        }

        channel.sendTyping().queue();

        GuildAudioManager musicManager = shard.getMyBot().getGuildAudioPlayer(guild);

        String content;
        AudioTrack oldTrack = musicManager.getPlayer().getPlayingTrack();
        AudioTrack newTrack = ((MusicTrackScheduler)musicManager.getScheduler()).nextTrack();
        if (musicManager.getPlayer().getPlayingTrack() != null) {
            content = "Lied wird übersprungen: **" + oldTrack.getInfo().title + "**" +
                    "\n\n" +
                    "Neues Lied: **" + newTrack.getInfo().title + "**";
        }
        else {
            content = "Neues Lied wird gestartet.";
        }


        log.info(this.getClass().getSimpleName() + ": " + content);
        channel.sendMessage(content).queue(new MessageDeleter());
    }

    @Override
    public String helpString() {
        return "Befehl zum Überspringen des Liedes, das aktuell gespielt wird.";
    }

    private boolean checkSkipCount(GuildAudioManager manager, Guild guild, TextChannel textChannel) {
        TrackScheduler scheduler = manager.getScheduler();
        VoiceChannel channel = guild.getAudioManager().getConnectedChannel();

        if (channel == null) {
            return false;
        }

        ((MusicTrackScheduler)scheduler).increaseSkipRequestCounter();

        int skipPercent = GuildData.getGuildDataByGuildId(guild.getId()).getSongSkipPercent();
        int skipCount = ((MusicTrackScheduler)scheduler).getSkipRequestsCounter();
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

}
