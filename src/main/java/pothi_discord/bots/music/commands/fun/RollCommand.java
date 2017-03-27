package pothi_discord.bots.music.commands.fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;

import java.util.Random;

/**
 * Created by Pascal Pothmann on 27.02.2017.
 */
public class RollCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if(!checkPermission(guild, user)){
            return;
        }

        TextChannel channel = event.getChannel();

        int sides = 6;
        String successor = "";

        if(args.length > 1) {
            try {
                sides = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                successor = "\n*(" + args[1] + " ist keine gültiige Eingabe für einen Bereich)*";
            }
        }

        channel.sendMessage("Ich würfel einen Würfel mit " + sides + " Seiten...  :game_die:" +
                "\n**Das Ergebins: " + (new Random().nextInt(sides) + 1) + ".**" +
                successor).queue(new MessageDeleter());
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
