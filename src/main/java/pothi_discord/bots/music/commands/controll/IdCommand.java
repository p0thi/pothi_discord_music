package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.utils.Param;

import java.awt.*;
import java.util.List;

/**
 * Created by Pascal Pothmann on 10.03.2017.
 */
public class IdCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }


        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(guild.getJDA().getSelfUser().getName(), null, null)
                .setColor(Color.decode(Param.BOT_COLOR_HEX))
                .setTitle("Alles IDs:", "http://glowtrap.de/");

        List<Role> roles =  guild.getRoles();
        for (Role role : roles) {
            eb.addField(role.getName(), role.getId(), true);
        }

        eb.addBlankField(false);

        TextChannel channel = event.getChannel();

        channel.sendMessage(eb.build()).queue(new MessageDeleter(120000));


    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
