package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;

/**
 * Created by Pascal Pothmann on 03.02.2017.
 */
public class SummonCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if(!checkPermission(event)) {
            return;
        }

        User user = event.getAuthor();
        Guild guild = event.getGuild();

        TextChannel channel = event.getChannel();

        VoiceChannel memberVoiceChannel = guild.getMember(user).getVoiceState().getChannel();
        Member botMember = guild.getMember(event.getJDA().getSelfUser());

        if(args.length >= 2) {
            if(args[1].toLowerCase().equals("default")) {
                shard.getMyBot().connectToVoiceChannel(guild.getAudioManager());
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

        if (guild.getAudioManager().getConnectedChannel() != null
                && guild.getAudioManager().getConnectedChannel().getId().equals(memberVoiceChannel.getId())) {
            guild.getAudioManager().closeAudioConnection();
        }

        guild.getAudioManager().openAudioConnection(memberVoiceChannel);
    }


    @Override
    public String helpString() {
        return "Befehl um den Bot in den eigenen VoiceChannel zu holen.";
    }
}
