package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Pascal Pothmann on 27.02.2017.
 */
public class PingCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
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
