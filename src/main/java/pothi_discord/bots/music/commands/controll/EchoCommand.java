package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;

import java.util.Arrays;

/**
 * Created by Pascal Pothmann on 25.03.2017.
 */
public class EchoCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard botShard) {
        Guild guild = event.getGuild();
        User user = event.getAuthor();

        if (!checkPermission(guild, user)) {
            return;
        }

        event.getMessage().delete().queue();

        for (Guild myGuild : botShard.getMyBot().getAllGuilds()) {
            myGuild.getPublicChannel().sendMessage(String.join(" ", Arrays.copyOfRange(args, 1, args.length)))
                    .queue(new MessageDeleter());
        }
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
