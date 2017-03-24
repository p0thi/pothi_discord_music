package pothi_discord_music.utils.database.morphia.guilddatas;

import net.dv8tion.jda.core.entities.Guild;
import org.mongodb.morphia.annotations.Embedded;
import pothi_discord_music.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@Embedded
public class Permissions {
    private String owner = "";
    @Embedded private RoleEntity defaultRole = createDefaultRole();
    @Embedded private List<RoleEntity> roles = new ArrayList<>();

    private static RoleEntity createDefaultRole() {
        RoleEntity defaultRoleEntity = new RoleEntity();
        defaultRoleEntity.setName("Default");
        defaultRoleEntity.setDefaultRole(true);

        return defaultRoleEntity;
    }

    public boolean hasUserPermissionForCommand(Guild guild, String userId, String commandName) {
        boolean result = Param.isDeveloper(userId);
        result = result || userId.equals(owner);

        if (defaultRole.hasRolePermissionForCommand(commandName)) {
            result = true;
        }
        else {
            for (RoleEntity pr : roles) {
                if (pr.hasUserAccess(guild, userId) && pr.hasRolePermissionForCommand(commandName)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    public ArrayList<RoleEntity> getRolesOfUser(Guild guild, String userID) {
        ArrayList<RoleEntity> result = new ArrayList<>();
        result.add(defaultRole);

        for(RoleEntity pr : roles) {
            if (pr.hasUserAccess(guild, userID)) {
                result.add(pr);
            }
        }

        return result;
    }

    public int getMaxPlaylistSizeOfUser(Guild guild, String userId) {
        int result = defaultRole.getMaxPlaylistSize();

        for (RoleEntity pr : roles) {
            result = Math.max(result, pr.getMaxPlaylistSizeOfUser(guild, userId));
        }

        return result;
    }

    public long getMaxSongLengthOfUser(Guild guild, String userId) {
        long result = defaultRole.getMaxSongLengthMillis();

        for (RoleEntity pr : roles) {
            result = Math.max(result, pr.getMaxSongLengthOfUser(guild, userId));
        }

        return result;
    }

    public boolean canUserInstaskip(Guild guild, String userId) {
        if(defaultRole.canUserInstaskip(guild, userId)) {
            return true;
        }

        for(RoleEntity pr : roles) {
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

    public RoleEntity getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(RoleEntity defaultRoleEntity) {
        this.defaultRole = defaultRoleEntity;
    }

    public List<RoleEntity> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleEntity> roleEntities) {
        this.roles = roleEntities;
    }
}
