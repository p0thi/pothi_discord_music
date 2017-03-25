package pothi_discord_music.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord_music.Main;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;

import java.util.Arrays;

/**
 * Created by Pascal Pothmann on 25.03.2017.
 */
public class EchoCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        User user = event.getAuthor();

        if (!checkPermission(guild, user)) {
            return;
        }

        event.getMessage().delete().queue();

        for (Guild myGuild : Main.getAllGuilds()) {
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
