package pothi_discord_music.commands.music;

import net.dv8tion.jda.core.entities.User;
import pothi_discord_music.Main;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.utils.GuildData;
import pothi_discord_music.utils.couch_db.guilddata.GuildDBObject;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class VolumeCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();
        String[] words = event.getMessage().getContent().split("[ \n\r]");
        boolean valid = true;
        int newVolume = 0;

        try {
            newVolume = Integer.parseInt(words[1].trim());
        } catch (Exception e) {
            valid = false;
        }

        valid = valid && (words.length == 2);

        GuildMusicManager musicManager = Main.getGuildAudioPlayer(guild);
        GuildDBObject guildDBObject = GuildData.ALL_GUILD_DATAS.get(guild.getId()).getGuildDBObject();

        if(!valid) {
            if(words.length == 1) {
                channel.sendMessage("Aktuelle Lautstärke: **" + musicManager.player.getVolume()
                        + "** (Standart: " + guildDBObject.getPlayerStartVolume() + ").").queue(new MessageDeleter());
                return;
            }
            channel.sendMessage("Ungültiger Befehl.").queue();
            return;
        }

        int oldVolume = musicManager.player.getVolume();
        musicManager.player.setVolume(newVolume);

        channel.sendMessage("Lautsärke von " + oldVolume + " auf "
                + musicManager.player.getVolume() + " geändert.").queue(new MessageDeleter());
    }

    @Override
    public String helpString() {
        return null;
    }
}
