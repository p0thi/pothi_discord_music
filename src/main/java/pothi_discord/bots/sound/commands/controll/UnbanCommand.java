package pothi_discord.bots.sound.commands.controll;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;

/**
 * Created by Pascal Pothmann on 22.03.2017.
 */
public class UnbanCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
