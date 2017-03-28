package pothi_discord.bots.sound.commands.controll;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.Main;
import pothi_discord.bots.sound.commands.audio.PlayerCommand;

import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.utils.HugeMessageSender;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.guilddata.audio_commands.MongoAudioCommand;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommand;

import java.util.List;

/**
 * Created by Pascal Pothmann on 22.03.2017.
 */
public class RequestsCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }

        if (args.length < 2) {
            event.getChannel().sendMessage(String.format("Mit **%srequests show** kannst du dir alle " +
                    "Befehle in der Adminabfrage anzeigen lassen.", Param.PREFIX())).queue(new MessageDeleter());
            return;
        }
        GuildData guildData = GuildData.getGuildDataById(guild.getId());
        List<SoundCommand> allTmpCommands = guildData.getTmpSoundCommands();

        if (allTmpCommands.size() <= 0) {
            event.getChannel().sendMessage("Die Liste ist leer. :rolling_eyes:").queue(new MessageDeleter());
            return;
        }

        switch (args[1].toLowerCase()) {
            case "show":
                HugeMessageSender hugeMessageSender = new HugeMessageSender();
                hugeMessageSender.setHeader("Alle Command-Anfragen$MULTIPAGE$:");
                hugeMessageSender.setMpInfix(". Seite $CURRENTPAGE$ von $TOTALPAGES$");
                hugeMessageSender.setTag(HugeMessageSender.MessageTag.INFO);
                hugeMessageSender.setSendAsCodeblock(false);

                for (int i = 0; i < allTmpCommands.size(); i++) {
                    SoundCommand tmp = allTmpCommands.get(i);
                    hugeMessageSender.append("**" + (i+1) + "**.  " +
                            Param.PREFIX() + tmp.getCommand() + " - " + tmp.getDescription() + "\n");
                }
                hugeMessageSender.append("\nBefehle testen mit **%1$srequests test <Nummer>**\n" +
                        "Befehle akzeptieren mit **%1$srequests accept <Nummer>**\n" +
                        "Befehle löschen mit **%1$srequests delete <Nummer>** oder **%1$srequests clear**");

                hugeMessageSender.send(event.getChannel());

                break;
            case "test": {
                if (checkThirdArgument(args, allTmpCommands)) {
                    event.getChannel().sendMessage("Kein/Falsches drittes Argument.")
                            .queue(new MessageDeleter());
                    return;
                }

                SoundCommand myCommand = allTmpCommands.get(Integer.parseInt(args[2]) - 1);
                VoiceChannel vc = guild.getMember(user).getVoiceState().getChannel();
                new PlayerCommand(myCommand.getFileId()).play(guild, vc, shard);
                break;
            }
            case "accept": {
                if (checkThirdArgument(args, allTmpCommands)) {
                    event.getChannel().sendMessage("Kein/Falsches drittes Argument.")
                            .queue(new MessageDeleter());
                    return;
                }

                SoundCommand myCommand = allTmpCommands.get(Integer.parseInt(args[2]) - 1);

                for (SoundCommand command : guildData.getSoundCommands()) {
                    if(command.getCommand().toLowerCase().equals(myCommand.getCommand().toLowerCase())) {
                        event.getChannel().sendMessage("Der Befehl kann nicht aktiviert werden, " +
                                "da der Befehls-String bereits verwendet wird." + ":see_no_evil: ")
                                .queue(new MessageDeleter());
                        return;
                    }
                }
                guildData.getTmpSoundCommands().remove(myCommand);
                guildData.getSoundCommands().add(myCommand);

                guildData.saveInstance();

                event.getChannel().sendMessage(String.format("Der Befehl **%S** ist nun " +
                                "für alle auf diesem Server verfügbar.",
                        Param.PREFIX() + myCommand.getCommand())).queue(new MessageDeleter());

                break;
            }
            case "delete": {
                if (checkThirdArgument(args, allTmpCommands)) {
                    event.getChannel().sendMessage("Kein/Falsches drittes Argument.")
                            .queue(new MessageDeleter());
                    return;
                }

                SoundCommand myCommand = allTmpCommands.get(Integer.parseInt(args[2]) - 1);
                String command = new String(myCommand.getCommand());

                MongoAudioCommand.removeAudioFileFromDatabase(myCommand.getFileId());

                guildData.getTmpSoundCommands().remove(myCommand);
                guildData.saveInstance();

                event.getChannel()
                        .sendMessage(String
                                .format("Erfolgreich den Befehl **%s** aus der Adminabfrage gelöscht.",
                                        Param.PREFIX() + command)).queue(new MessageDeleter());

                break;
            }
            case "clear":
                for (SoundCommand command : guildData.getTmpSoundCommands()) {
                    MongoAudioCommand.removeAudioFileFromDatabase(command.getFileId());
                }
                guildData.getTmpSoundCommands().clear();
                guildData.saveInstance();

                event.getChannel().sendMessage("Alle Einträge aus der Adminabfrage gelöscht."
                        + " :nerd:").queue(new MessageDeleter());
                break;
        }
    }

    private boolean checkThirdArgument(String[] args, List<SoundCommand> allTmpCommands) {
        return args.length < 3
                || !args[2].matches("\\d+")
                || Integer.parseInt(args[2]) > allTmpCommands.size()
                || Integer.parseInt(args[2]) < 1;
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
