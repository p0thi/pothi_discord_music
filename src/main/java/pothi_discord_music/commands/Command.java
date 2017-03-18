package pothi_discord_music.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.handlers.MessageDeleter;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public interface Command {
    default void action(GuildMessageReceivedEvent event, String[] args) {
        event.getChannel()
                .sendMessage("Dieser Befehl befindet sich noch in Bearbeitung. :rolling_eyes: :see_no_evil:")
                .queue(new MessageDeleter());
    }
    default void prepare(){

    }
    boolean checkPermission(Guild guild, User user);
    String helpString();
}
