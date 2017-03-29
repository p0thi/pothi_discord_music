package pothi_discord.commands;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Member;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.bots.Bot;
import pothi_discord.bots.BotShard;
import pothi_discord.Main;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.handlers.StaticSchedulePool;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import java.util.ArrayList;

/**
 * Created by Pascal Pothmann on 27.01.2017.
 */
public class ShutdownCommand extends GuildCommand {
    private static final Logger log = MorphiaLoggerFactory.get(ShutdownCommand.class);

    private static long timeBeforeShutdown = 0;

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if(!checkPermission(event)) {
            return;
        }

        ArrayList<String> checkedUsers = new ArrayList<>();
        for (Guild tmpGuild : Main.musicBot.getAllGuilds()) {

            for (Member member : tmpGuild.getMembers()) {
                if (checkedUsers.contains(member.getUser().getId())
                        || member.getUser().isBot()
                        || member.getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
                    continue;
                }

                try {
                    Userdata userdata = Userdata.getUserdata(member.getUser().getId());

                    Game newGame;
                    try {
                        newGame = member.getGame();
                    } catch (Exception e) {
                        log.error(e.getLocalizedMessage());
                        return;
                    }

                    try {
                        userdata.storeGame(newGame, true);
                        checkedUsers.add(member.getUser().getId());
                    } catch (Exception e) {
                        log.error(e.getLocalizedMessage());
                        return;
                    }
                } catch (Exception e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        }


        execute(Main.soundBot);
        execute(Main.musicBot);

        System.exit(0);
    }


    @Override
    public String helpString() {
        return null;
    }

    public synchronized static void execute(Bot bot) {

        StaticSchedulePool.executeAllTasks();
        for (BotShard shard : bot.shards) {
            shard.getJDA().shutdown();
        }

        try {
            log.info("Everything done. Shutdown in " + (double)timeBeforeShutdown/1000 + " seconds.");
            Thread.sleep(timeBeforeShutdown);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
