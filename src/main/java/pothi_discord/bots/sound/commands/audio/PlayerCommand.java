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
import pothi_discord.listeners.TrackScheduler;
import pothi_discord.permissions.PermissionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Pascal Pothmann on 22.03.2017.
 */
public class PlayerCommand extends GuildCommand {
    private String fileId;

    public PlayerCommand(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        Guild guild = event.getGuild();
        User user = event.getAuthor();

        if (!checkPermission(guild, user)) {
            return;
        }

        VoiceChannel userVc = guild.getMember(user).getVoiceState().getChannel();

        if (args.length > 1
                && PermissionManager.checkUserPermission(guild, user, "send-sound-bot-to-user")) {
            Member tmpMember = guild.getMemberById(args[1]);
            if (tmpMember != null) {
                userVc = tmpMember.getVoiceState().getChannel();
            }
        }

        play(guild, userVc, shard);
    }

    public void play(Guild guild, VoiceChannel vc, BotShard shard) {
        guild.getAudioManager().openAudioConnection(vc);
        SoundTrackScheduler sheduler = (SoundTrackScheduler) shard.getMyBot().getGuildAudioPlayer(guild).getScheduler();
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
