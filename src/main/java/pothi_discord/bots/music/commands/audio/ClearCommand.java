package pothi_discord.bots.music.commands.audio;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.bots.music.listeners.MusicTrackScheduler;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.listeners.TrackScheduler;

/**
 * Created by Pascal Pothmann on 12.05.2017.
 */
public class ClearCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard botShard) {
        if (!checkPermission(event)) {
            return;
        }

        Guild guild = event.getGuild();

        MusicTrackScheduler scheduler = (MusicTrackScheduler) botShard.getMyBot().getGuildAudioPlayer(guild).getScheduler();

        int size = scheduler.queue.size();

        scheduler.queue.clear();

        event.getChannel().sendMessage(String.format("Die Playlist wurde gelöscht. **%d** Einträge wurden entfernt.",
                size )).queue(new MessageDeleter());
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
