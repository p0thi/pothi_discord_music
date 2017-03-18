package pothi_discord_music.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Pascal Pothmann on 27.02.2017.
 */
public class PingCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if(!checkPermission(guild, user)) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        boolean isPinged;
        try {
            isPinged = InetAddress.getByName("google.com").isReachable(2000);
        } catch (IOException e) {
            e.printStackTrace();
            isPinged = false;
        }
        currentTime = System.currentTimeMillis() - currentTime;

        if(isPinged) {
            TextChannel channel = event.getChannel();

            channel.sendMessage("**Pong!** :nerd: (" + currentTime + "ms).").queue(new MessageDeleter());
        }
    }

    @Override
    public String helpString() {
        return null;
    }
}
