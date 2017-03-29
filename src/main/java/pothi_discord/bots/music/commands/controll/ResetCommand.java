package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;

/**
 * Created by Pascal Pothmann on 03.02.2017.
 */
public class ResetCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        if (!checkPermission(event)) {
            return;
        }

        Guild guild = event.getGuild();
        TextChannel channel = event.getChannel();

        boolean skipTrack = args.length > 1 && args[1].toLowerCase().equals("song");

        channel.sendTyping();

        GuildMusicManager manager = (GuildMusicManager) shard.getMyBot().getGuildAudioPlayer(guild);

        if(skipTrack) {
            manager.player.stopTrack();
        }
        else {
            manager.player.setPaused(true);
        }

        if(guild.getAudioManager().isConnected()) {
            VoiceChannel currentVoice = guild.getAudioManager().getConnectedChannel();
            guild.getAudioManager().closeAudioConnection();
            guild.getAudioManager().openAudioConnection(currentVoice);
        }

        if(skipTrack) {
            manager.scheduler.nextTrack();
        }
        else {
            manager.player.setPaused(false);
        }

    }


    @Override
    public String helpString() {
        return "Befehl zum Neustarten des Musikplayers.";
    }
}
