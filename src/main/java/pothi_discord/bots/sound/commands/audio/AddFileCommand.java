package pothi_discord.bots.sound.commands.audio;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.bson.Document;
import org.bson.types.ObjectId;
import pothi_discord.bots.BotShard;
import pothi_discord.Main;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.permissions.PermissionManager;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommandEntry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

/**
 * Created by Pascal Pothmann on 22.03.2017.
 */
public class AddFileCommand extends GuildCommand{
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if (!checkPermission(event)) {
            return;
        }

        User user = event.getAuthor();
        Guild guild = event.getGuild();

        TextChannel channel = event.getChannel();

        if (args.length <= 2) {
            channel.sendMessage("Ungültige Aktion (Command/Beschreibung vergessen?) " +
                    "(Schreibweise (Ohne \"<\" und \">\"):\n\n!addfile <command> <beschreibung>)\n\n" +
                    "Anleitung unter: http://glowtrap.de/guides/").queue(new MessageDeleter());
            return;
        }

        Message msg = event.getMessage();

        Message.Attachment attachment = msg.getAttachments().get(0);

        // Checks if at least 1 attachment is attached (The first is relevant)
        if (msg.getAttachments().size() > 0) {

            // Checks if the first attachment is valid audio file
            if (Param.isValidAudioFile(attachment.getFileName())) {

                String commandName = args[1].toLowerCase();
                String commandDescription = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                GuildData mongoGuild = GuildData.getGuildDataByGuildId(guild.getId());

                boolean commandAlreadyExists = false;

                for(SoundCommandEntry command : mongoGuild.getSoundCommands().getSoundCommandEntries()) {
                    if (command.getCommand().equals(commandName)) {
                        commandAlreadyExists = true;
                        break;
                    }
                }

                // Checks if command already exists
                if (!commandAlreadyExists) {

                    // Save File to mongoDB
                    InputStream fileInputStream;
                    try {
                        URL fileUrl = new URL(attachment.getUrl());
                        URLConnection urlConnection = fileUrl.openConnection();
                        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
                        fileInputStream = urlConnection.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    SoundCommandEntry mongoAudioCommand = new SoundCommandEntry();

                    mongoAudioCommand.setCommand(commandName);
                    mongoAudioCommand.setDescription(commandDescription);

                    // Checks weather user can add directly to list without admin request
                    if (PermissionManager.checkUserPermission(guild, user, "addfile-without-adminrequest")) {
                        String fileId = saveFileToDatabse(fileInputStream, attachment.getFileName());
                        mongoAudioCommand.setFileId(fileId);

                        mongoGuild.getSoundCommands().getSoundCommandEntries().add(mongoAudioCommand);
                        mongoGuild.saveInstance();

                        channel.sendMessage(String.format("Erfolgreich. Der Befehl %s spielt jetzt die Datei " +
                                "%s ab.", Param.PREFIX() + commandName, attachment.getFileName()))
                                .queue(new MessageDeleter());

                    } else {

                        if (mongoGuild.getSoundCommands().getTmpSoundCommandEntries().size() >= 100) { // TODO this variable could be made configurable
                            channel.sendMessage("Die Warteschlange für Adminabfragen ist zu lang.")
                                    .queue(new MessageDeleter());
                            return;
                        }
                        String fileId = saveFileToDatabse(fileInputStream, attachment.getFileName());
                        mongoAudioCommand.setFileId(fileId);

                        mongoGuild.getSoundCommands().getTmpSoundCommandEntries().add(mongoAudioCommand);
                        mongoGuild.saveInstance();

                        StringBuilder b = new StringBuilder();
                        b.append(String.format("Erfolgreich. Der Befehl %s wurde zur Adminabfrage hinzugefügt." +
                                "\nWende dich an einen der Folgenden Benutzer, um den Befehl freischalten zu lassen:",
                                Param.PREFIX() + commandName));
                        b.append("\n\n");

                        for(Member member : PermissionManager.getMembersWithPermission(guild, "manage-audio-command-requests")) {
                            b.append(member.getAsMention());
                            b.append("  ");
                        }

                        channel.sendMessage(b.toString()).queue(new MessageDeleter(120000));

                    }

                } else {
                    channel.sendMessage(String.format("%s  Ein Befehl mit diesem Namen ist bereits vorhanden.", user.getAsMention()))
                            .queue(new MessageDeleter());
                }

            } else {
                channel.sendMessage("Ungültiges Dateiformat.").queue(new MessageDeleter());
            }

        }
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }

    private String saveFileToDatabse(InputStream inputStream, String filename) {
        MongoDatabase db = Main.mongoDB.getMongoDatabase("pothibot");
        GridFSBucket gridFSBucket = GridFSBuckets.create(db);

        GridFSUploadOptions options = new GridFSUploadOptions()
                .metadata(new Document("type", "audio_command_file"));

        ObjectId objectId = gridFSBucket.uploadFromStream(filename, inputStream, options);
        return objectId.toHexString();
    }
}
