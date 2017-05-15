package pothi_discord.bots.sound.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.managers.HugeMessageSender;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommand;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommandEntry;

import java.util.List;

/**
 * Created by Pascal Pothmann on 22.03.2017.
 */
public class CommandsCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if (!checkPermission(event)) {
            return;
        }

        Guild guild = event.getGuild();

        HugeMessageSender hugeMessageSender = new HugeMessageSender();
        hugeMessageSender.setTag(HugeMessageSender.MessageTag.INFO);
        hugeMessageSender.setHeader("Alle Audio-Commands" + "$MULTIPAGE$:");
        hugeMessageSender.setMpInfix(". Seite $CURRENTPAGE$/$TOTALPAGES$");

        List<SoundCommandEntry> allCommands = GuildData.getGuildDataByGuildId(guild.getId()).getSoundCommands().getSoundCommandEntries();

        for(SoundCommandEntry soundCommand : allCommands){
            // 15 is the length of the spaces
            String description = "               ".substring(Math.min(15, soundCommand.getCommand().length()));

            hugeMessageSender.append(Param.PREFIX() + soundCommand.getCommand() +
                    "   " + description + soundCommand.getDescription() + "\n");
        }
        hugeMessageSender.append("\nUm neue Befehle hinzuzufügen: http://glowtrap.de/guides/");

        hugeMessageSender.send(event.getChannel());
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
