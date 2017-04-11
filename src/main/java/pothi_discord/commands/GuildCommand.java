package pothi_discord.commands;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.Permissions;

/**
 * Created by Pascal Pothmann on 10.03.2017.
 */
public abstract class GuildCommand implements Command {
    protected final Logger log = MorphiaLoggerFactory.get(this.getClass());

    public String getName(){
        return this.getClass().getSimpleName();
    }

    protected Message getAccessDeniedMessage(User user) {
        MessageBuilder builder = new MessageBuilder();
        builder.append(user.getAsMention());
        builder.append(" Du hast leider nicht die nötigen Rechte für diesen Befehl.");
        return builder.build();
    }

    @Override
    public boolean checkPermission(GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        User user = event.getAuthor();
        boolean result = Param.isDeveloper(user.getId());

        GuildData guildData = GuildData.getGuildDataByGuildId(guild.getId());
        Permissions permissions = guildData.getPermissions();

        String permission = getName();

        result = result || permissions.hasUserPermissionForCommand(guild, user.getId(), permission);

        if (!result) {
            event.getChannel().sendMessage(getAccessDeniedMessage(user)).queue(new MessageDeleter(5000));
            event.getChannel().sendMessage(String.format("Benötigte Berechtigung: %s \n\n" +
                    "Du kannst Dir deine Berechtigungsgruppen mit **%spermissions** anzeigen lassen.",
                    permission,
                    Param.PREFIX()))
                    .queue(new MessageDeleter(15000));
        }
        return result;
    }

}
