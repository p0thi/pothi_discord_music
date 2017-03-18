package pothi_discord_music.commands.controll;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.Main;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.utils.Param;

/**
 * Created by Pascal Pothmann on 03.02.2017.
 */
public class SummonCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if(!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();

        VoiceChannel memberVoiceChannel = guild.getMember(user).getVoiceState().getChannel();
        Member botMember = guild.getMember(event.getJDA().getSelfUser());

        if(args.length >= 2) {
            if(args[1].toLowerCase().equals("default")) {
                Main.connectToVoiceChannel(guild.getAudioManager());
                return;
            }
        }

        if (memberVoiceChannel == null) {
            channel.sendMessage(String.format("Du musst dich in einem Voice Channel befinden."))
                    .queue(new MessageDeleter());
            return;
        }

        if (!botMember.hasPermission(memberVoiceChannel, Permission.VOICE_CONNECT)) {
            channel.sendMessage(String.format("Der Bot hat nicht die Rechte um diesem Voice Channel beizutreten."))
                    .queue(new MessageDeleter());
            return;
        }

        guild.getAudioManager().openAudioConnection(memberVoiceChannel);
    }


    @Override
    public String helpString() {
        return "Befehl um den Bot in den eigenen VoiceChannel zu holen.";
    }
}
