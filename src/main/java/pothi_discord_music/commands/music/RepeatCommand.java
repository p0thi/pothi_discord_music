package pothi_discord_music.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.Main;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.managers.music.GuildMusicManager;
import pothi_discord_music.utils.database.morphia.guilddatas.GuildData;

/**
 * Created by Pascal Pothmann on 25.03.2017.
 */
public class RepeatCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        Guild guild = event.getGuild();
        User user = event.getAuthor();

        if (!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();

        GuildMusicManager manager = Main.getGuildAudioPlayer(guild);
        AudioTrack currentTrack = manager.player.getPlayingTrack().makeClone();

        if (currentTrack != null) {

            try {
                manager.scheduler.queueFirst(currentTrack);
            } catch (InterruptedException e) {
                channel.sendMessage("Ein Fehler ist aufgetreten." + " :see_no_evil: ")
                        .queue(new MessageDeleter());
                e.printStackTrace();
                return;
            }

            channel.sendMessage(String
                                .format("Der Track **%s** wurde vorne in die Warteschlange erneut eingereiht.",
                                currentTrack.getInfo().title))
                    .queue(new MessageDeleter());

        }
         else {
            channel.sendMessage("Es wird nichts gespielt. " +
                            "Also kann auch nichts wiederholt werden. Duuhhh..." + " :eyes:")
                    .queue(new MessageDeleter());
        }
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
