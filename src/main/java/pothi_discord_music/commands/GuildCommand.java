package pothi_discord_music.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.database.morphia.guilddatas.GuildData;
import pothi_discord_music.utils.database.morphia.guilddatas.Permissions;

/**
 * Created by Pascal Pothmann on 10.03.2017.
 */
public abstract class GuildCommand implements Command {

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
