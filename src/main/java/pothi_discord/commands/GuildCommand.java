package pothi_discord.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.Permissions;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

/**
 * Created by Pascal Pothmann on 10.03.2017.
 */
public abstract class GuildCommand implements Command {
    protected final Logger log = MorphiaLoggerFactory.get(this.getClass());

    public String getName(){
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean checkPermission(Guild guild, User user) {
        boolean result = Param.isDeveloper(user.getId());

        GuildData guildData = GuildData.getGuildDataById(guild.getId());
        Permissions permissions = guildData.getPermissions();

        result = result || permissions.hasUserPermissionForCommand(guild, user.getId(), getName());

        return result;
    }
}
