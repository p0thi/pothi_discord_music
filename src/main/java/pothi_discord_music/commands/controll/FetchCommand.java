package pothi_discord_music.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord_music.Main;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.audio.YoutubeMusicGenre;

/**
 * Created by Pascal Pothmann on 30.01.2017.
 */
public class FetchCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if(!checkPermission(guild, user)) {
            return;
        } if(args.length < 2) {
            return;
        }

        TextChannel channel = event.getChannel();

        switch (args[1].toLowerCase()) {
            case "genre":
            case "genres":
                new Thread(() -> {
                    YoutubeMusicGenre.saveVideosForAllGenres();
                    channel.sendMessage(user.getAsMention() + " Alle Genres aktualisiert.")
                            .queue(new MessageDeleter());
                }).start();
                break;
        }
    }

    @Override
    public boolean checkPermission(Guild guild, User user) {
        boolean permissionGranted = true;

        permissionGranted = permissionGranted && Param.isDeveloper(user.getId());

        return permissionGranted;
    }

    @Override
    public String helpString() {
        return "Befehl um die Liste mit Genre-Videos zu aktualisieren.";
    }
}
