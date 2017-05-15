package pothi_discord.bots.sound.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommandEntry;

import java.util.List;

/**
 * Created by Pascal Pothmann on 22.03.2017.
 */
public class DeleteCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if (!checkPermission(event)) {
            return;
        }

        Guild guild = event.getGuild();

        if(args.length < 2) {
            event.getChannel().sendMessage("Fehlender Befehl. " + ":see_no_evil:").queue(new MessageDeleter());
            return;
        }

        GuildData guildData = GuildData.getGuildDataByGuildId(guild.getId());
        List<SoundCommandEntry> allSoundCommands = guildData.getSoundCommands().getSoundCommandEntries();
        StringBuilder builder = new StringBuilder("Folgende Befehle wurden gelöscht:" + "\n\n");

        for (int i = 1; i < args.length; i++) {
            inner:
            for (SoundCommandEntry command : allSoundCommands) {
                if (command.getCommand().equals(args[i].toLowerCase())) {
                    builder.append(command.getCommand() + ", ");
                    guildData.getSoundCommands().getSoundCommandEntries().remove(command);
                    guildData.saveInstance();
                    break inner;
                }
            }

        }

        event.getChannel().sendMessage(builder.toString()).queue(new MessageDeleter());

    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }

}
