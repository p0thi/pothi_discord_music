package pothi_discord.utils.database.morphia.guilddatas;

import net.dv8tion.jda.core.entities.Guild;
import org.mongodb.morphia.annotations.Embedded;
import oshi.jna.platform.unix.solaris.LibKstat;
import pothi_discord.utils.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@Embedded
public class Permissions {
    private static final String DEFAULT_ROLE_NAME = "Default";

    private String owner = "Unknown";
    @Embedded private RoleEntity defaultRole = createDefaultRole();
    @Embedded private List<RoleEntity> roles = new ArrayList<>();

    private static RoleEntity createDefaultRole() {
        RoleEntity defaultRoleEntity = new RoleEntity();
        defaultRoleEntity.setName(DEFAULT_ROLE_NAME);
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
            roleIteration:
            for (RoleEntity role : roles) {
                if (role.hasUserAccess(guild, userId) && role.hasRolePermissionForCommand(commandName)) {
                    result = true;
                    break roleIteration;
                }
                subroleIteration:
                for (String roleName : role.getSubroles()) {
                    RoleEntity tmpRole = getRoleByName(roleName);
                    if (tmpRole == null) {
                        continue subroleIteration;
                    }

                    if (tmpRole.hasUserAccess(guild, userId) && role.hasRolePermissionForCommand(commandName)) {
                        result = true;
                        break roleIteration;
                    }
                }
            }
        }

        return result;
    }

    public List<String> getAllCommandStringsOfUser(Guild guild, String userID) {
        List<String> result = new ArrayList<>();

        for (RoleEntity role : getRolesOfUser(guild, userID)) {
            for (String permName : role.getCommandNames()) {
                if (!result.contains(permName)) {
                    result.add(permName);
                }
            }
        }
        return result;
    }

    public RoleEntity getRoleByName(String rolename) {
        if (rolename == null ||rolename.isEmpty()) {
            return null;
        }

        if (rolename.equals(DEFAULT_ROLE_NAME)) {
            return defaultRole;
        }

        for (RoleEntity role : roles) {
            if (role.getName().equals(rolename)) {
                return role;
            }
        }
        return null;
    }

    public List<RoleEntity> getRolesOfUser(Guild guild, String userID) {
        ArrayList<RoleEntity> result = new ArrayList<>();
        result.add(defaultRole);

        for(RoleEntity pr : roles) {
            if (pr.hasUserAccess(guild, userID)) {
                result.add(pr);

                for (String name : pr.getSubroles()) {
                    RoleEntity subrole = getRoleByName(name);

                    if (subrole == null || !subrole.hasUserAccess(guild, userID)) {
                        continue;
                    }

                    if (!result.contains(subrole)) {
                        result.add(subrole);
                    }
                }
            }
        }

        return result;
    }

    public int getMaxPlaylistSizeOfUser(Guild guild, String userId) {
        int result = defaultRole.getMaxPlaylistSize();

        for (RoleEntity pr : roles) {
            result = Math.max(result, pr.getMaxPlaylistSize(guild, userId));

            for (String name : pr.getSubroles()) {
                RoleEntity subrole = getRoleByName(name);

                if (subrole == null) {
                    continue;
                }

                result = Math.max(result, subrole.getMaxPlaylistSize(guild, userId));
            }
        }

        return result;
    }

    public long getMaxSongLengthOfUser(Guild guild, String userId) {
        long result = defaultRole.getMaxSongLengthMillis();

        for (RoleEntity pr : roles) {
            result = Math.max(result, pr.getMaxSongLength(guild, userId));

            for (String name : pr.getSubroles()) {
                RoleEntity subrole = getRoleByName(name);

                if (subrole == null) {
                    continue;
                }

                result = Math.max(result, subrole.getMaxSongLength(guild, userId));
            }
        }

        return result;
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
