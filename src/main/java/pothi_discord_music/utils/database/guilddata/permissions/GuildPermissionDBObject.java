package pothi_discord_music.utils.database.guilddata.permissions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.dv8tion.jda.core.entities.Guild;
import pothi_discord_music.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 10.03.2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuildPermissionDBObject {
    private String owner = "Unknown";
    private List<PermissionRole> roles = new ArrayList<>();
    private PermissionRole defaultRole = PermissionRole.createDefaultRole();



    public boolean hasUserPermissionForCommand(Guild guild, String userId, String commandName) {
        boolean result = Param.isDeveloper(userId);
        result = result || userId.equals(owner);

        if (defaultRole.hasRolePermissionForCommand(commandName)) {
            result = true;
        }
        else {
            for (PermissionRole pr : roles) {
                if (pr.hasUserAccess(guild, userId) && pr.hasRolePermissionForCommand(commandName)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public ArrayList<PermissionRole> getRolesOfUser(Guild guild, String userID) {
        ArrayList<PermissionRole> result = new ArrayList<>();
        result.add(defaultRole);

        for(PermissionRole pr : roles) {
            if (pr.hasUserAccess(guild, userID)) {
                result.add(pr);
            }
        }

        return result;
    }

    public int getMaxPlaylistSizeOfUser(Guild guild, String userId) {
        int result = defaultRole.getMaxPlaylistSize();

        for (PermissionRole pr : roles) {
            result = Math.max(result, pr.getMaxPlaylistSizeOfUser(guild, userId));
        }

        return result;
    }

    public long getMaxSongLengthOfUser(Guild guild, String userId) {
        long result = defaultRole.getMaxSongLengthMillis();

        for (PermissionRole pr : roles) {
            result = Math.max(result, pr.getMaxSongLengthOfUser(guild, userId));
        }

        return result;
    }

    public boolean canUserInstaskip(Guild guild, String userId) {
        if(defaultRole.canUserInstaskip(guild, userId)) {
            return true;
        }

        for(PermissionRole pr : roles) {
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

    public List<PermissionRole> getRoles() {
        return roles;
    }

    public void setRoles(List<PermissionRole> roles) {
        this.roles = roles;
    }

    public PermissionRole getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(PermissionRole defaultRole) {
        this.defaultRole = defaultRole;
    }
}
