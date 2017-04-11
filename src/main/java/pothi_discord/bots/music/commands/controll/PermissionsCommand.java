package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.Permissions;
import pothi_discord.utils.database.morphia.guilddatas.RoleEntity;

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
        Permissions gpo = GuildData.getGuildDataByGuildId(guild.getId()).getPermissions();
        List<RoleEntity> roleEntities = gpo.getRolesOfUser(guild, user.getId());

        StringBuilder sb = new StringBuilder("Das sind deine internen Rollen: \n\n");

        for(RoleEntity pr : roleEntities) {
            sb.append("**" + pr.getName() + "**\n");
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
    public boolean checkPermission(GuildMessageReceivedEvent event) {
        return true;
    }
}
