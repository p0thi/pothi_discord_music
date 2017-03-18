package pothi_discord_music.commands.controll;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.GuildReceiveHandler;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.utils.GuildData;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.TextUtils;
import pothi_discord_music.utils.audio.AudioUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public class RecordCommand extends GuildCommand {
    private static final Logger log = LoggerFactory.getLogger(RecordCommand.class);

    private User user;
    private TextChannel channel;
    private Guild guild;
    private GuildData guildData;
    private AudioUtils audioUtils;
    private GuildReceiveHandler grh;

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        user = event.getAuthor();
        guild = event.getGuild();

        if(!checkPermission(guild, user)) {
            return;
        }
        channel = event.getChannel();

        guildData = GuildData.ALL_GUILD_DATAS.get(guild.getId());

        if(guildData.getAudioUtils() == null) {
            guildData.setAudioUtils(new AudioUtils());
        }

        grh = guildData.getGuildReceiveHandler();
        audioUtils = guildData.getAudioUtils();

        if (guild.getAudioManager().getReceiveHandler() != null
                && guild.getAudioManager().getReceiveHandler() instanceof GuildReceiveHandler) {
            stopRecording(this.user);
        }
        else {
            startRecording();
        }
    }


    @Override
    public String helpString() {
        return null;
    }

    private void startRecording() {
        audioUtils.setChannels(2);
        audioUtils.setStartTime(System.currentTimeMillis());

        guild.getAudioManager().setReceivingHandler(grh);

        audioUtils.message = channel.sendMessage("__Aufnahme Läuft.__").complete();
        audioUtils.recording = true;
        audioUtils.user = user;

        String msgContent = "__**Aufnahme läuft.**__    **(%s)**" +
                "\n\n*(Wird bei 15 Minuten automaisch beendet)*";

        new Thread(() -> {
            while(audioUtils.recording) {
                long currentDuration = System.currentTimeMillis() - audioUtils.getStartTime();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    audioUtils.message.editMessage(String.format(msgContent, TextUtils.formatMillis(currentDuration)))
                            .complete();
                } catch (NullPointerException ignore) {}

                if (currentDuration >= (15 * 60 * 1000)) {
                    log.info(audioUtils.user.getName() + "'s recording stopped (Time limit reached).");
                    stopRecording(audioUtils.user);
                }
            }
        }).start();
    }

    private void stopRecording(User starter) {

        if(!audioUtils.user.getId().equals(starter.getId())) {
            channel.sendMessage("Es läuft momentan eine Aufnahme von **" + audioUtils.user.getName()
                    + "**. Diese **muss** von ihm **beendet werden**, bevor eine neue gestartet werden kann...")
                    .queue(new MessageDeleter());
            return;
        }
        guild.getAudioManager().setReceivingHandler(null);
        MessageDeleter.deleteMessage(audioUtils.message);

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy__HH_mm");
        audioUtils.setFilePath("./pothi_discord_music/out/recording_" + guild.getName() + "_" + sdf.format(now) + ".mp3");

        channel.sendTyping();
        audioUtils.recording = false;channel.sendMessage("Die **Aufnahme wurde beendet** und wird versendet." +
                "\nErst hier in den Chat und danach auch noch an "
                + audioUtils.user.getAsMention() + " als PN. Das kann einen Augenblick dauern.")
                .queue(new MessageDeleter());

        File result = audioUtils.saveToFile();

        PrivateChannel privateChannel = audioUtils.user.getPrivateChannel();

        audioUtils.clear();

        MessageBuilder b = new MessageBuilder();

        try {
            b.append("Hier die Aufnahme:");
            MessageDeleter.deleteMessage(channel.sendFile(result, b.build()).complete(), 50000);

            MessageBuilder mb = new MessageBuilder();
            SimpleDateFormat tmpFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
            mb.append("Hier die Aufnahme vom " + tmpFormat.format(now) + ".");
            privateChannel.sendFile(result, mb.build()).complete();

            result.delete();
        } catch (IOException ignore) {
            channel.sendMessage("Ich konnte die Datei leider nicht verschicken...")
                    .queue(new MessageDeleter());
        } catch (IllegalArgumentException e) {
            channel.sendMessage("Ich konnte die Datei leider nicht verschicken. " +
                    "Sie bleibt jedoch auf dem Server gespeichert.")
                    .queue(new MessageDeleter());
        }
    }
}
