package pothi_discord.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.handlers.MessageDeleter;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Pascal Pothmann on 27.02.2017.
 */
public class PingCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if(!checkPermission(event)) {
            return;
        }
        try {
            InetAddress.getByName("google.com").isReachable(2000);
        } catch (IOException e) {
            e.printStackTrace();
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
