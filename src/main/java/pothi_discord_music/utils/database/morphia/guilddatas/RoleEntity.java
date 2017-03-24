package pothi_discord_music.utils.database.morphia.guilddatas;

import net.dv8tion.jda.core.entities.Guild;
import org.mongodb.morphia.annotations.Embedded;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@Embedded
public class RoleEntity {
    private String name = "Unnamed";
    private int maxPlaylistSize = 25;
    private long maxSongLengthMillis = 720000;
    private boolean defaultRole = false;
    private boolean canInstaSkip = false;

    private List<String> roleIds = new ArrayList<>();
    private List<String> userIds = new ArrayList<>();
    private List<String> commandNames = new ArrayList<>();


    public boolean hasUserAccess(Guild guild, String userId) {
        if (defaultRole) {
            return true;
        }

        if (userIds.contains(userId)) {
            return true;
        }

        for(String roleId : roleIds) {
            net.dv8tion.jda.core.entities.Role tmpRole = guild.getRoleById(roleId);
            if (guild.getMemberById(userId).getRoles().contains(tmpRole)){
                return true;
            }
        }
        return false;
    }

    public boolean hasRolePermissionForCommand(String commandName) {
        return this.commandNames.contains(commandName);
    }

    public int getMaxPlaylistSizeOfUser(Guild guild, String userId) {
        if (hasUserAccess(guild, userId)) {
            return this.maxPlaylistSize;
        }
        else {
            return 0;
        }
    }

    public long getMaxSongLengthOfUser(Guild guild, String userId) {
        if (hasUserAccess(guild, userId)) {
            return this.maxSongLengthMillis;
        }
        else {
            return 0;
        }
    }

    public boolean canUserInstaskip(Guild guild, String userId) {
        if(!hasUserAccess(guild, userId)) {
            return false;
        }
        else {
            return canInstaSkip;
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxPlaylistSize() {
        return maxPlaylistSize;
    }

    public void setMaxPlaylistSize(int maxPlaylistSize) {
        this.maxPlaylistSize = maxPlaylistSize;
    }

    public long getMaxSongLengthMillis() {
        return maxSongLengthMillis;
    }

    public void setMaxSongLengthMillis(long maxSongLengthMillis) {
        this.maxSongLengthMillis = maxSongLengthMillis;
    }

    public boolean isDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(boolean defaultRole) {
        this.defaultRole = defaultRole;
    }

    public boolean isCanInstaSkip() {
        return canInstaSkip;
    }

    public void setCanInstaSkip(boolean canInstaSkip) {
        this.canInstaSkip = canInstaSkip;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public List<String> getCommandNames() {
        return commandNames;
    }

    public void setCommandNames(List<String> commandNames) {
        this.commandNames = commandNames;
    }
}
