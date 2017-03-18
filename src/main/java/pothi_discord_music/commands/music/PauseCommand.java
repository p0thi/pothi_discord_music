package pothi_discord_music.commands.music;

import pothi_discord_music.Main;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.utils.Param;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class PauseCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if(!checkPermission(guild, user)) {
            return;
        }


        GuildMusicManager musicManager = Main.getGuildAudioPlayer(guild);
        musicManager.player.setPaused(!musicManager.player.isPaused());
    }


    @Override
    public String helpString() {
        return null;
    }
}
