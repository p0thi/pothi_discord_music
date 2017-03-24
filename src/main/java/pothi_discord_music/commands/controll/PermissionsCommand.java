package pothi_discord_music.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.utils.database.morphia.guilddatas.GuildData;
import pothi_discord_music.utils.database.morphia.guilddatas.Permissions;
import pothi_discord_music.utils.database.morphia.guilddatas.RoleEntity;

import java.util.List;

/**
 * Created by Pascal Pothmann on 10.03.2017.
 */
public class PermissionsCommand extends GuildCommand{
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();
        Permissions gpo = GuildData.getGuildDataById(guild.getId()).getPermissions();
        List<RoleEntity> roleEntities = gpo.getRolesOfUser(guild, user.getId());

        StringBuilder sb = new StringBuilder("Das sind deine internen Rollen: \n\n");

        for(RoleEntity pr : roleEntities) {
            sb.append(pr.getName() + "\n");
            for(String cmd : pr.getCommandNames()) {
                sb.append("\t" + cmd + "\n");
            }
            sb.append("\n");
        }

        channel.sendMessage(sb.toString()).queue(new MessageDeleter());
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }

    @Override
    public boolean checkPermission(Guild guild, User user) {
        return true;
    }
}
