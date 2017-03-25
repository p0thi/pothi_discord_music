package pothi_discord_music.commands.controll;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.managers.AudioManager;

import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord_music.DiscordBot;
import pothi_discord_music.Main;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.StaticSchedulePool;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.utils.Param;

import java.util.List;

/**
 * Created by Pascal Pothmann on 27.01.2017.
 */
public class ShutdownCommand extends GuildCommand {
    private static final Logger log = MorphiaLoggerFactory.get(ShutdownCommand.class);

    private static long timeBeforeShutdown = 0;

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if(!checkPermission(guild, user)) {
            return;
        }

        execute();
    }


    @Override
    public String helpString() {
        return null;
    }

    public static void execute() {

        StaticSchedulePool.executeAllTasks();
        for (DiscordBot bot : Main.shards) {
            bot.getJDA().shutdown();
        }

        try {
            log.info("Everything done. Shutdown in " + (double)timeBeforeShutdown/1000 + " seconds.");
            Thread.sleep(timeBeforeShutdown);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

}
