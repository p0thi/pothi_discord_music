package pothi_discord_music.commands.music;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord_music.Main;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.commands.controll.SelectCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord_music.managers.MessageManager;
import pothi_discord_music.utils.GuildData;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.audio.VideoSelection;
import pothi_discord_music.utils.audio.YoutubeMusicGenre;
import pothi_discord_music.utils.youtube.YoutubeAPI;
import pothi_discord_music.utils.youtube.YoutubeVideo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class PlayCommand extends GuildCommand {
    private static final Logger log = LoggerFactory.getLogger(PlayCommand.class);

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }

        VoiceChannel membersVoiceChannel = guild.getMember(user).getVoiceState().getChannel();
        TextChannel channel = event.getChannel();

        String content = " ";
        for(int i = 1; i < args.length; i++) {
            content += args[i] + " ";
        }
        final String value = content.trim();

        //Checks if the user is in the same VoiceChannel with the bot and aborts if not
        if((guild.getAudioManager().getConnectedChannel() != null
                && membersVoiceChannel != null
                && !guild.getAudioManager().getConnectedChannel().getId().equals(membersVoiceChannel.getId()))) {
            channel.sendMessage("Du musst dich im selben Channel wie der Bot befinden.").queue(new MessageDeleter());
            return;
        }

        if (args.length == 2 && StringUtils.isNumeric(args[1])){
            SelectCommand.select(guild, channel, user, args);
            log.info("Selection detected.");
            return;
        }

        if (args.length > 2) {
            GuildMusicManager manager = Main.getGuildAudioPlayer(guild);
            switch (args[1].toLowerCase()) {
                case "genre":
                case  "genres":
                    GuildData guildData = GuildData.ALL_GUILD_DATAS.get(guild.getId());

                    if (StringUtils.isNumeric(args[2])) {
                        int index = Integer.parseInt(args[2]);

                        if (YoutubeMusicGenre.values().length < index || index < 1) {
                            channel.sendMessage("Das ist keine Gültige Genre-Nummer.").queue(new MessageDeleter());
                            return;
                        }

                        YoutubeMusicGenre genre = YoutubeMusicGenre.values()[index - 1];
                        manager.setGenrePlaylist(genre);
                        channel.sendMessage("Ab jetzt werden lieder aus dem Genre **"
                                + genre.getReadableName() + "** gepielt." +
                                "\n:information_source: Die standart Playlist kann mit **'"
                                + Param.PREFIX() + "play default'** wieder aktiviert werden")
                                .queue(new MessageDeleter(120000));
                    }
                    else {
                        String[] relevantWords = Arrays.copyOfRange(args, 2, args.length);
                        String searchKey = String.join(" ", relevantWords);
                        ArrayList<YoutubeMusicGenre> results = null;
                        try {
                            results = YoutubeMusicGenre.getGenresBySearch(searchKey);
                        } catch (ArithmeticException e) {
                            channel.sendMessage("Das ist kein gültiger Suchbegriff.").queue(new MessageDeleter());
                            return;
                        }

                        if (results.size() == 0) {
                            channel.sendMessage("Zu der Suchanfrage wurden keine Ergebnisse gefunden.").queue(new MessageDeleter());
                            return;
                        }

                        guildData.setLastGenreSearch(results);
                        MessageManager mM = new MessageManager();
                        mM.append("Folgende Genres wurden gefunden:\n");
                        for (YoutubeMusicGenre genre : results) {
                            mM.append("**" + genre.getReadableName()
                                    + "         " + "**Number: " + (genre.ordinal() + 1) + "\n");
                        }
                        mM.append("\n:information_source: Das gewünschte Genre kann mit **'" + Param.PREFIX() + "play genre <Nummer>'** gewählt werden.");

                        for(String msg : mM.complete()) {
                            channel.sendMessage(msg).queue(new MessageDeleter(90000));
                        }
                    }
                    return;
            }
        }

        if(!args[1].toLowerCase().startsWith("http")) {
            GuildMusicManager manager = Main.getGuildAudioPlayer(guild);
            if (args[1].toLowerCase().equals("genre") || args[1].toLowerCase().equals("genres")) {
                channel.sendMessage(":information_source: Du kannst ein Genre mit dem Befehl **'"
                        + Param.PREFIX() + "play genre <Suchbegriff>'** suchen, oder Dir mit **'"
                        + Param.PREFIX() + "play genre all'** alle verfügbaren anzeigen lassen.")
                        .queue(new MessageDeleter());
                return;
            }
            if (args[1].toLowerCase().equals("default")) {
                channel.sendMessage("Ab jetzt werden wieder Lieder aus der Standart-Playlist gespielt.")
                        .queue(new MessageDeleter());
                manager.loadDefaultPlaylist();
                return;
            }
            try {
                searchForVideos(guild, channel, user, event.getMessage());
            } catch (RateLimitedException e) {
                e.printStackTrace();
            }
            return;
        }

        GuildMusicManager musicManager = Main.getGuildAudioPlayer(guild);

        if(musicManager.player.isPaused()) {
            musicManager.player.setPaused(false);
        }

        musicManager.playRequestByKey(guild, user.getId(), value, channel, false);
    }

    @Override
    public String helpString() {
        return null;
    }

    private void searchForVideos(Guild guild, TextChannel channel, User user, Message message) throws RateLimitedException {
        Matcher m = Pattern.compile("\\S+\\s+(.*)").matcher(message.getRawContent());
        m.find();
        String query = m.group(1);

        //Now remove all punctuation
        query = query.replaceAll("[.,/#!$%\\^&*;:{}=\\-_`~()]", "");

        Message outMsg = channel.sendMessage("Durchsuche YouTube nach `{q}`...".replace("{q}", query)).complete(true);

        ArrayList<YoutubeVideo> vids = null;
        try {
            vids = YoutubeAPI.searchForVideos(query);
        } catch (JSONException e) {
            channel.sendMessage("Ein Fehler bei der Suche ist aufgetreten. Möglicherweise funktioniert es, wenn direkt eine Quelle angegeben wird\n```\n" + Param.PREFIX() + "play <url>```").queue(new MessageDeleter());
            log.debug("YouTube search exception", e);
            return;
        }

        if (vids.isEmpty()) {
            outMsg.editMessage("Keine Ergebnisse für `{q}`".replace("{q}", query)).queue(new MessageDeleter());
        } else {
            //Clean up any last search by this user
            GuildMusicManager manager = Main.getGuildAudioPlayer(guild);

            VideoSelection oldSelection = manager.selections.get(user.getId());
            if(oldSelection != null) {
                oldSelection.getOutMsg().delete().queue();
            }

            MessageBuilder builder = new MessageBuilder();
            builder.append("**Bitte ein Video mit dem Befehl `" + Param.PREFIX() + "play n` auswählen:**");

            int i = 1;
            for (YoutubeVideo vid : vids) {
                builder.append("\n**")
                        .append(String.valueOf(i))
                        .append(":** ")
                        .append(vid.getName())
                        .append(" (")
                        .append(vid.getDurationFormatted())
                        .append(")");
                i++;
            }

            outMsg.editMessage(builder.build().getRawContent()).queue(new MessageDeleter());

            manager.setCurrentTC(channel);

            manager.selections.put(user.getId(), new VideoSelection(vids, outMsg));
        }
    }
}
