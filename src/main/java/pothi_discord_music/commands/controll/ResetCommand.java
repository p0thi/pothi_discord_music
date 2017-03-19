package pothi_discord_music.commands.controll;

import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.Main;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.managers.music.GuildMusicManager;
import pothi_discord_music.utils.Param;

/**
 * Created by Pascal Pothmann on 03.02.2017.
 */
public class ResetCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();


        if (!checkPermission(guild, user)) {
            return;
        }

        GuildMusicManager manager = Main.getGuildAudioPlayer(guild);

        manager.player.stopTrack();
        if(guild.getAudioManager().isConnected()) {
            VoiceChannel currentVoice = guild.getAudioManager().getConnectedChannel();
            guild.getAudioManager().closeAudioConnection();
            guild.getAudioManager().openAudioConnection(currentVoice);
        }
        manager.scheduler.nextTrack();
    }


    @Override
    public String helpString() {
        return "Befehl zum Neustarten des Musikplayers.";
    }
}
