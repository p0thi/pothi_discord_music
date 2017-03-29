package pothi_discord.bots.music.commands.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;

/**
 * Created by Pascal Pothmann on 25.03.2017.
 */
public class RepeatCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if (!checkPermission(event)) {
            return;
        }

        Guild guild = event.getGuild();

        TextChannel channel = event.getChannel();

        GuildMusicManager manager = (GuildMusicManager) shard.getMyBot().getGuildAudioPlayer(guild);
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
