package pothi_discord.permissions;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.Permissions;
import pothi_discord.utils.database.morphia.guilddatas.RoleEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Pascal Pothmann on 10.03.2017.
 */
public class PermissionManager {

    public static boolean checkUserPermission(Guild guild, User user, String permission) {
        GuildData mongoGuilddata = GuildData.getGuildDataByGuildId(guild.getId());
        boolean result = Param.isDeveloper(user.getId());

        result = result || mongoGuilddata.getPermissions().getOwner().equals(user.getId());

        GuildData guildData = GuildData.getGuildDataByGuildId(guild.getId());

        Permissions permissions = guildData.getPermissions();

        result = result || permissions.hasUserPermissionForCommand(guild, user.getId(), permission);

        return result;
    }

    public static List<Member> getMembersWithPermission(Guild guild, String permission) {
        GuildData mongoGuilddata = GuildData.getGuildDataByGuildId(guild.getId());

        Set<Member> result = new HashSet<>();
        result.add(guild.getMemberById(mongoGuilddata.getPermissions().getOwner()));

        for (String developer : Param.getDevelopers()) {
            result.add(guild.getMemberById(developer));
        }

        Permissions guildPermissions = mongoGuilddata.getPermissions();

        RoleEntity defaultRole = guildPermissions.getDefaultRole();

        result.addAll(getMembersByMongoPermissionRole(defaultRole, guild, permission));

        for (RoleEntity role : mongoGuilddata.getPermissions().getRoles()) {
            result.addAll(getMembersByMongoPermissionRole(role, guild, permission));
        }
        result.remove(null);

        return new ArrayList<>(result);
    }

    private static Set<Member> getMembersByMongoPermissionRole(RoleEntity role, Guild guild, String permission) {
        Set<Member> result = new HashSet<>();
        if (role.getCommandNames().contains(permission)) {
            for (String userId : role.getUserIds()) {
                result.add(guild.getMemberById(userId));
            }

            for (String guildRoleId : role.getRoleIds()) {
                Role tmpRole = guild.getRoleById(guildRoleId);
                for (Member member : guild.getMembersWithRoles(tmpRole)) {
                    result.add(member);
                }
            }
        }
        return result;
    }
}
