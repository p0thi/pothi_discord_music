package pothi_discord.utils.database.guilddata.permissions;

import net.dv8tion.jda.core.entities.Guild;
import pothi_discord.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 10.03.2017.
 */
public class MongoGuildPermissionsObject {
    private String owner = "Unknown";
    private List<MongoPermissionRole> roles = new ArrayList<>();
    private MongoPermissionRole defaultRole = MongoPermissionRole.createDefaultRole();



    public boolean hasUserPermissionForCommand(Guild guild, String userId, String commandName) {
        boolean result = Param.isDeveloper(userId);
        result = result || userId.equals(owner);

        if (defaultRole.hasRolePermissionForCommand(commandName)) {
            result = true;
        }
        else {
            for (MongoPermissionRole pr : roles) {
                if (pr.hasUserAccess(guild, userId) && pr.hasRolePermissionForCommand(commandName)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public ArrayList<MongoPermissionRole> getRolesOfUser(Guild guild, String userID) {
        ArrayList<MongoPermissionRole> result = new ArrayList<>();
        result.add(defaultRole);

        for(MongoPermissionRole pr : roles) {
            if (pr.hasUserAccess(guild, userID)) {
                result.add(pr);
            }
        }

        return result;
    }

    public int getMaxPlaylistSizeOfUser(Guild guild, String userId) {
        int result = defaultRole.getMaxPlaylistSize();

        for (MongoPermissionRole pr : roles) {
            result = Math.max(result, pr.getMaxPlaylistSizeOfUser(guild, userId));
        }

        return result;
    }

    public long getMaxSongLengthOfUser(Guild guild, String userId) {
        long result = defaultRole.getMaxSongLengthMillis();

        for (MongoPermissionRole pr : roles) {
            result = Math.max(result, pr.getMaxSongLengthOfUser(guild, userId));
        }

        return result;
    }

    public boolean canUserInstaskip(Guild guild, String userId) {
        if(defaultRole.canUserInstaskip(guild, userId)) {
            return true;
        }

        for(MongoPermissionRole pr : roles) {
            if (pr.canUserInstaskip(guild, userId)) {
                return true;
            }
        }
        return false;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<MongoPermissionRole> getRoles() {
        return roles;
    }

    public void setRoles(List<MongoPermissionRole> roles) {
        this.roles = roles;
    }

    public MongoPermissionRole getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(MongoPermissionRole defaultRole) {
        this.defaultRole = defaultRole;
    }
}
