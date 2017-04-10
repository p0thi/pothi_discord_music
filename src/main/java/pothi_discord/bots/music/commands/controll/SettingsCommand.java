package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;

/**
 * Created by Pascal Pothmann on 09.04.2017.
 */
public class SettingsCommand extends GuildCommand{
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard botShard) {

        if (!checkPermission(event)) {
            return;
        }

        if (args.length <= 1){
            //TODO help?
            return;
        }

        switch (args[1].trim().toLowerCase()) {
            case "playlist":
                playlist(event, args);
                break;
            default:
                //TODO
                break;
        }
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }

    private void playlist(GuildMessageReceivedEvent event, String[] args) {
        if (args.length <= 2) {
            //TODO show playlist
            return;
        }

        switch (args[2].trim().toLowerCase()) {
            case "add": {
                //TODO
                break;
            }
            case "remove": {
                //TODO
                break;
            }
            case "search": {
                //TODO
                break;
            }
        }

    }
}
