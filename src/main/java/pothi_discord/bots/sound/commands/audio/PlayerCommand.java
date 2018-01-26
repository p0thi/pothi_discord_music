package pothi_discord.bots.sound.commands.audio;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import pothi_discord.bots.BotShard;
import pothi_discord.Main;
import pothi_discord.bots.sound.listeners.SoundTrackScheduler;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.listeners.TrackScheduler;
import pothi_discord.permissions.PermissionManager;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommandEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Pascal Pothmann on 22.03.2017.
 */
public class PlayerCommand extends GuildCommand {
    private String fileId;
    private String command;
    private boolean verified = true;

    static Timer timer = new Timer();
    static HashMap<Guild, HashSet<User>> cooldown = new HashMap<>();

    public PlayerCommand(String fileId) {
        this.fileId = fileId;
    }
    public PlayerCommand(String commannd, boolean verified) {
        this.command = commannd;
        this.verified = verified;
    }

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if (!checkPermission(event)) {
            return;
        }

        Guild guild = event.getGuild();

        if (!trackVerified(guild)) {
            return;
        }

        User user = event.getAuthor();

        boolean allowwedCooldown = cooldownAllowed(guild, user);
        if (!allowwedCooldown) {
            event.getChannel().sendMessage(user.getAsMention() + " Timeout... Bitte warten. :stuck_out_tongue:")
                    .queue(new MessageDeleter(4000));
            return;
        }

        VoiceChannel userVc = guild.getMember(user).getVoiceState().getChannel();

        if (args.length > 1){
            Member tmpMember = guild.getMemberById(args[1]);
            if (tmpMember != null) {
                if (PermissionManager.checkUserPermission(guild, user, "send-sound-bot-to-user")) {
                    userVc = tmpMember.getVoiceState().getChannel();
                }
            }
            else if (args[1].toLowerCase().equals("remove") || args[1].toLowerCase().equals("delete")) {
                if (PermissionManager.checkUserPermission(guild, user, "remove-sound-command")) {
                    //TODO delete functionallity
                    event.getChannel().sendMessage("Das ist noch nicht möglich. Bitte wende dich an einen Admin.")
                            .queue(new MessageDeleter());
                    return;
                }
            }
        }

        play(guild, userVc, shard.getMyBot().getGuildAudioPlayer(guild).getScheduler());
    }

    public void action(Guild guild, User user, VoiceChannel voiceChannel, TrackScheduler scheduler) {
        if (!trackVerified(guild)) {
            return;
        }

        play(guild, voiceChannel, scheduler);
    }

    private boolean trackVerified(Guild guild){
        if (!this.verified) {

            GuildData guildData = GuildData.getGuildDataByGuildId(guild.getId());
            for (SoundCommandEntry soundCommand : guildData.getSoundCommands().getSoundCommandEntries()) {
                if (soundCommand.getCommand().toLowerCase().equals(command.toLowerCase())) {
                    this.fileId = soundCommand.getFileId();
                    this.verified = true;
                    break;
                }
            }
        }
        return this.verified;
    }

    public static boolean cooldownAllowed(Guild guild, User user) {
        if (!cooldown.containsKey(guild)) {
            cooldown.put(guild, new HashSet<>());
        }

        if (cooldown.get(guild).contains(user)) {
            return false;
        }

        cooldown.get(guild).add(user);
        //TODO set delay to a value that is stored in the database
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cooldown.get(guild).remove(user);
            }
        }, 3000);
        return true;
    }

    public void play(Guild guild, VoiceChannel vc, TrackScheduler scheduler) {
        guild.getAudioManager().openAudioConnection(vc);
        SoundTrackScheduler sheduler = (SoundTrackScheduler) scheduler;
        sheduler.vc = vc;

        MongoDatabase db = Main.mongoDB.getMongoDatabase("pothibot");
        GridFSBucket gridFSBucket = GridFSBuckets.create(db);

        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(new ObjectId(this.fileId));
        String prefix = gridFSDownloadStream.getGridFSFile().getMD5();
        String suffix = gridFSDownloadStream.getGridFSFile().getFilename();
        File tmpFile;

        try {
            tmpFile = stream2file(gridFSDownloadStream, prefix, suffix);
            tmpFile.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        log.info("File created:\n\t" + tmpFile.getAbsolutePath());

        sheduler.queue(tmpFile.getAbsolutePath());

    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }

    private File stream2file (InputStream in, String prefix, String suffix) throws IOException {
        final File tempFile = File.createTempFile(prefix, suffix);
        //final File tempFile = new File("pothi_discord_sound/audio/" + prefix + suffix);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }
}
