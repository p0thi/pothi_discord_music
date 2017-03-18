package pothi_discord_music.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import pothi_discord_music.utils.GuildData;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.couch_db.guilddata.GuildDBObject;
import pothi_discord_music.utils.couch_db.guilddata.permissions.GuildPermissionDBObject;

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

        GuildData guildData = GuildData.ALL_GUILD_DATAS.get(guild.getId());
        GuildDBObject guildDBObject = guildData.getGuildDBObject();
        GuildPermissionDBObject permissions = guildDBObject.getPermissions();

        result = result || permissions.hasUserPermissionForCommand(guild, user.getId(), getName());

        return result;
    }
}
