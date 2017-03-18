package pothi_discord_music.commands.controll;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord_music.Main;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.managers.music.GuildMusicManager;
import pothi_discord_music.utils.GuildData;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.TextUtils;
import pothi_discord_music.utils.audio.VideoSelection;
import pothi_discord_music.utils.couch_db.guilddata.GuildDBObject;
import pothi_discord_music.utils.couch_db.guilddata.permissions.GuildPermissionDBObject;
import pothi_discord_music.utils.youtube.YoutubeVideo;

/**
 * Created by Pascal Pothmann on 31.01.2017.
 */
public class SelectCommand extends GuildCommand {
    private static final Logger log = LoggerFactory.getLogger(SelectCommand.class);
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        //TODO: Dafuq??
    }

    @Override
    public String helpString() {
        return null;
    }

    public static void select(Guild guild, TextChannel channel, User user, String[] args) {
        GuildMusicManager manager = Main.getGuildAudioPlayer(guild);

        GuildData guildData = GuildData.ALL_GUILD_DATAS.get(guild.getId());
        GuildDBObject guildDBObject = guildData.getGuildDBObject();
        GuildPermissionDBObject gpo = guildDBObject.getPermissions();

        AudioPlayer player = manager.player;
        manager.setCurrentTC(channel);
        if (manager.selections.containsKey(user.getId())) {
            VideoSelection selection = manager.selections.get(user.getId());
            try {
                int i = Integer.valueOf(args[1]);
                if (selection.getChoices().size() < i || i < 1) {
                    throw new NumberFormatException();
                } else {
                    YoutubeVideo selected = selection.choices.get(i - 1);
                    long duration = TextUtils.parseISO8601DurationToMillis(selected.getDuration());

                    if (duration <= gpo.getMaxSongLengthOfUser(guild, user.getId())) {
                        manager.selections.remove(user.getId());
                        String msg = "Lied **#" + i + "** wurde ausgewählt: **" + selected.getName() + "** (" + selected.getDurationFormatted() + ")";
                        selection.getOutMsg().editMessage(msg).complete(true);
                        manager.playRequestByKey(guild, user.getId(), "https://www.youtube.com/watch?v=" + selected.getId(), channel, true);
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
