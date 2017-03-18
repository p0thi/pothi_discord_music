package pothi_discord_music.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.User;
import pothi_discord_music.Main;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.utils.TextUtils;

import java.util.ArrayList;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class QueueCommand extends GuildCommand {
    private static final int RESULT_LENGTH = 10;
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();

        GuildMusicManager musicManager = Main.getGuildAudioPlayer(guild);

        ArrayList<String> results = new ArrayList<>();

        int queueSize = musicManager.scheduler.queue.size();

        if(queueSize == 0) {
            channel.sendMessage("Die Warteschlange ist momentan leer.").queue(new MessageDeleter());
            return;
        }

        for(AudioTrack track : musicManager.scheduler.queue) {
            String tmp = (results.size() + 1) + ". **" + track.getInfo().title + "**  " +
                    "[" + TextUtils.formatMillis(track.getDuration()) + "]";
            results.add(tmp);

            if(results.size() >= RESULT_LENGTH) {
                if(queueSize > RESULT_LENGTH) {
                    results.add("Und " + (queueSize - RESULT_LENGTH) + " weitere.");
                }
                break;
            }
        }

        String result = "";
        for(String line : results) {
            result += line + "\n";
        }

        channel.sendMessage("Hier die Warteschlange:\n" + result).queue(new MessageDeleter());
    }

    @Override
    public String helpString() {
        return null;
    }
}
