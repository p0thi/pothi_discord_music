package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.managers.MessageManager;
import pothi_discord.permissions.PermissionManager;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.Permissions;
import pothi_discord.utils.database.morphia.guilddatas.RoleEntity;

import java.util.Collections;
import java.util.List;

/**
 * Created by Pascal Pothmann on 10.03.2017.
 */
public class PermissionsCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        if (!checkPermission(event)) {
            return;
        }

        User user = event.getAuthor();
        Guild guild = event.getGuild();

        TextChannel channel = event.getChannel();

        if (args.length > 1) {

            boolean check = PermissionManager.checkUserPermission(guild, user, args[1]);

            if (check) {
                channel.sendMessage("Du hast die Rechte für diesen Befehl.").queue(new MessageDeleter());
            }
            else {
                channel.sendMessage("Du hast **NICHT** die Rechte für diesen Befehl.").queue(new MessageDeleter());
            }
            return;
        }
        Permissions gpo = GuildData.getGuildDataByGuildId(guild.getId()).getPermissions();

        MessageManager mm = new MessageManager();

        mm.append("**Das sind Deine internen Berechtigungen:**\n\n");

        List<String> allCommandNames = gpo.getAllCommandStringsOfUser(guild, user.getId());
        Collections.sort(allCommandNames);

        for (String commandName : allCommandNames) {
            mm.append("\t**-** " + commandName + "\n");
        }

        mm.append("\n");
        mm.append(String.format("Maximale Playlistlänge: **%d**",
                gpo.getMaxPlaylistSizeOfUser(guild, user.getId())) + "\n");
        mm.append(String.format("Maximale Liedlänge: **%s**",
                TextUtils.getMillisFormattedMS(gpo.getMaxSongLengthOfUser(guild, user.getId()))) + "\n");

        List<RoleEntity> allUserRoles = gpo.getRolesOfUser(guild, user.getId());

        mm.append("\n**Deine Gruppen:**\n\n");

        for (RoleEntity role : allUserRoles) {
            mm.append("\t**-** _" + role.getName() + "_\n");
        }

        for (String message : mm.complete()) {
            channel.sendMessage(message).queue(new MessageDeleter());
        }

    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }

    @Override
    public boolean checkPermission(GuildMessageReceivedEvent event) {
        return true;
    }
}
