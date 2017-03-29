package pothi_discord.bots.music.commands.audio;

import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class PauseCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if(!checkPermission(event)) {
            return;
        }

        Guild guild = event.getGuild();

        GuildMusicManager musicManager = (GuildMusicManager) shard.getMyBot().getGuildAudioPlayer(guild);
        musicManager.player.setPaused(!musicManager.player.isPaused());
    }


    @Override
    public String helpString() {
        return null;
    }
}
