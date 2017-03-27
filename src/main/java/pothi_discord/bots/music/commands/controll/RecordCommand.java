package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.bots.music.handlers.MusicBotGuildReceiveHandler;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.audio.AudioUtils;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public class RecordCommand extends GuildCommand {

    private User user;
    private TextChannel channel;
    private Guild guild;
    private GuildData guildData;
    private AudioUtils audioUtils;
    private MusicBotGuildReceiveHandler grh;

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        user = event.getAuthor();
        guild = event.getGuild();

        if(!checkPermission(guild, user)) {
            return;
        }
        channel = event.getChannel();

        guildData = GuildData.getGuildDataById(guild.getId());

        if(guildData.getAudioUtils() == null) {
            guildData.setAudioUtils(new AudioUtils());
        }

        grh = guildData.getMusicBotGuildReceiveHandler();
        audioUtils = guildData.getAudioUtils();

        if (guild.getAudioManager().getReceiveHandler() != null
                && guild.getAudioManager().getReceiveHandler() instanceof MusicBotGuildReceiveHandler) {
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
        audioUtils.setFilePath("pothi_discord/out/recording_" + guild.getName() + "_" + sdf.format(now) + ".mp3");

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
