package pothi_discord_music.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pothi_discord_music.Main;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import pothi_discord_music.utils.Param;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Pascal Pothmann on 26.01.2017.
 */
public class ShuffleCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();
        GuildMusicManager musicManager = Main.getGuildAudioPlayer(guild);
        boolean currentlyPlaying = musicManager.player.getPlayingTrack() != null;

        if(musicManager.scheduler.queue.isEmpty()) {
            channel.sendMessage("Die Warteschlange ist leer...").queue();
            return;
        }
        ArrayList<AudioTrack> tracks = new ArrayList<>();
        while(!musicManager.scheduler.queue.isEmpty()) {
            tracks.add(musicManager.scheduler.queue.poll());
        }

        Collections.shuffle(tracks);

        musicManager.scheduler.queue.addAll(tracks);

        if(currentlyPlaying && musicManager.player.getPlayingTrack() == null) {
            musicManager.scheduler.nextTrack();
        }

        new Thread(() -> {
            ArrayList<String> symbols = new ArrayList<>();
            symbols.add(":clubs:");
            symbols.add(":hearts:");
            symbols.add(":diamonds:");
            symbols.add(":spades:");

            final long sleep = 750;

            channel.sendMessage(StringUtils.join(symbols, " ")).queue(message1 -> {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {}

                Collections.shuffle(symbols);
                message1.editMessage(StringUtils.join(symbols, " ")).queue(message2 -> {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {}

                    Collections.shuffle(symbols);
                    message2.editMessage(StringUtils.join(symbols, " ")).queue(message3 -> {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {}

                        Collections.shuffle(symbols);
                        message3.editMessage(StringUtils.join(symbols, " ")).queue(message4 -> {
                            try {
                                Thread.sleep(sleep);
                            } catch (InterruptedException e) {}

                            Collections.shuffle(symbols);
                            message4.editMessage(StringUtils.join(symbols, " ")).queue(message5 -> {
                                try {
                                    Thread.sleep(sleep);
                                } catch (InterruptedException e) {}

                                Collections.shuffle(symbols);
                                message5.editMessage(StringUtils.join(symbols, " ")).queue(new MessageDeleter());
                            });
                        });
                    });
                });
            });
        }).start();

    }


    @Override
    public String helpString() {
        return null;
    }
}
