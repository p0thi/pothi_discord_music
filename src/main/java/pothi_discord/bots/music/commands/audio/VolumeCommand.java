package pothi_discord.bots.music.commands.audio;

import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class VolumeCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if (!checkPermission(event)) {
            return;
        }

        Guild guild = event.getGuild();

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

        GuildMusicManager musicManager = (GuildMusicManager) shard.getMyBot().getGuildAudioPlayer(guild);
        GuildData mongoGuilddata = GuildData.getGuildDataByGuildId(guild.getId());

        if(!valid) {
            if(words.length == 1) {
                channel.sendMessage("Aktuelle Lautstärke: **" + musicManager.player.getVolume()
                        + "** (Standart: " + mongoGuilddata.getPlayerStartVolume() + ").").queue(new MessageDeleter());
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
