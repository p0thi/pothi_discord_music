package pothi_discord.bots.music.commands.controll;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;
import pothi_discord.managers.GuildAudioManager;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.audio.VideoSelection;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.Permissions;
import pothi_discord.utils.youtube.YoutubeVideo;

/**
 * Created by Pascal Pothmann on 31.01.2017.
 */
public class SelectCommand extends GuildCommand {
    private static final Logger log = MorphiaLoggerFactory.get(SelectCommand.class);
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        //TODO: Dafuq??
    }

    @Override
    public String helpString() {
        return null;
    }

    public static void select(Guild guild, TextChannel channel, User user, String[] args, BotShard shard) {
        GuildAudioManager manager = shard.getMyBot().getGuildAudioPlayer(guild);

        GuildData guildData = GuildData.getGuildDataById(guild.getId());
        Permissions gpo = guildData.getPermissions();

        AudioPlayer player = manager.getPlayer();
        ((GuildMusicManager)manager).setCurrentTC(channel);
        if (((GuildMusicManager)manager).selections.containsKey(user.getId())) {
            VideoSelection selection = ((GuildMusicManager)manager).selections.get(user.getId());
            try {
                int i = Integer.valueOf(args[1]);
                if (selection.getChoices().size() < i || i < 1) {
                    throw new NumberFormatException();
                } else {
                    YoutubeVideo selected = selection.choices.get(i - 1);
                    long duration = TextUtils.parseISO8601DurationToMillis(selected.getDuration());

                    if (duration <= gpo.getMaxSongLengthOfUser(guild, user.getId())) {
                        ((GuildMusicManager)manager).selections.remove(user.getId());
                        String msg = "Lied **#" + i + "** wurde ausgewählt: **" + selected.getName() + "** (" + selected.getDurationFormatted() + ")";
                        selection.getOutMsg().editMessage(msg).complete(true);
                        ((GuildMusicManager)manager).playRequestByKey(guild, user.getId(), "https://www.youtube.com/watch?v=" + selected.getId(), channel, true);
                        player.setPaused(false);
                    }
                    else {
                        channel.sendMessage(String.format("Das ausgewählte Video ist zu lang. (Maximale Länge: %s)",
                                TextUtils.formatMillis(gpo.getMaxSongLengthOfUser(guild, user.getId())))).queue(new MessageDeleter());
                        log.info("Video Duration: " + TextUtils.formatMillis(duration));
                    }
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                channel.sendMessage("Das muss eine Nummer zwischen 1-" + selection.getChoices().size() + " sein.").queue(new MessageDeleter());
            } catch (RateLimitedException e) {
                throw new RuntimeException(e);
            }
        } else {
            channel.sendMessage("Dazu ist zuerst eine Auswahl nötig.").queue(new MessageDeleter());
        }
    }
}
