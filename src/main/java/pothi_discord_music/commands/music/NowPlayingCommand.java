package pothi_discord_music.commands.music;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.User;
import pothi_discord_music.Main;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONObject;
import org.json.XML;
import pothi_discord_music.utils.TextUtils;
import pothi_discord_music.utils.youtube.YoutubeAPI;
import pothi_discord_music.utils.youtube.YoutubeVideo;

import java.awt.*;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class NowPlayingCommand extends GuildCommand {
    private static final String DEFAULT_URL = "http://glowtrap.de/";
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();

        channel.sendTyping().queue();

        GuildMusicManager  musicManager = Main.getGuildAudioPlayer(guild);

        if(musicManager.player.getPlayingTrack() != null && !musicManager.player.isPaused()) {
            AudioTrack at = musicManager.player.getPlayingTrack();
            try {
                if (at instanceof YoutubeAudioTrack) {
                    sendYoutubeEmbed(channel, (YoutubeAudioTrack) at);
                } else if (at instanceof SoundCloudAudioTrack) {
                    sendSoundcloudEmbed(channel, (SoundCloudAudioTrack) at);
                } else if (at instanceof HttpAudioTrack && at.getIdentifier().contains("gensokyoradio.net")) {
                    //Special handling for GR
                    sendGensokyoRadioEmbed(channel);
                } else if (at instanceof HttpAudioTrack) {
                    sendHttpEmbed(channel, (HttpAudioTrack) at);
                } else if (at instanceof BandcampAudioTrack) {
                    sendBandcampResponse(channel, (BandcampAudioTrack) at);
                } else if (at instanceof TwitchStreamAudioTrack) {
                    sendTwitchEmbed(channel, (TwitchStreamAudioTrack) at);
                } else {
                    sendDefaultEmbed(channel, at);
                }
            } catch (Exception e){
                sendDefaultEmbed(channel, at);
            }
        }
        else {
            channel.sendMessage("Im Moment wird nichts abgespielt.").queue(new MessageDeleter());
        }
    }

    @Override
    public String helpString() {
        return null;
    }

    private void sendDefaultEmbed(TextChannel channel, AudioTrack at) {
        String desc = at.getDuration() == Long.MAX_VALUE ?
                "[LIVE]" :
                TextUtils.progressBar(at.getPosition(), at.getDuration(), 25) +
                        "\n["
                        + TextUtils.formatMillis(at.getPosition())
                        + "/"
                        + TextUtils.formatMillis(at.getDuration())
                        + "]";

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, null, null)
                .setTitle(at.getInfo().title, DEFAULT_URL)
                .setDescription(desc + "\n\nGeladen von " + at.getSourceManager().getSourceName())
                .addField("Aktuelle Autoplaylist", Main.getGuildAudioPlayer(channel.getGuild()).playlist.getName(), true)
                .setColor(Color.orange)
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl())
                .build();

        channel.sendMessage(embed).queue(new MessageDeleter());
    }

    private void sendBandcampResponse(TextChannel channel, BandcampAudioTrack at) {
        String desc = at.getDuration() == Long.MAX_VALUE ?
                "[LIVE]" :
                "["
                        + TextUtils.formatMillis(at.getPosition())
                        + "/"
                        + TextUtils.formatMillis(at.getDuration())
                        + "]";

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, null, null)
                .setTitle(at.getInfo().title, DEFAULT_URL)
                .setDescription(desc + "\n\nGeladen von Bandcamp")
                .addField("Aktuelle Autoplaylist", Main.getGuildAudioPlayer(channel.getGuild()).playlist.getName(), true)
                .setColor(new Color(99, 154, 169))
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl())
                .build();

        channel.sendMessage(embed).queue(new MessageDeleter());
    }

    private void sendTwitchEmbed(TextChannel channel, TwitchStreamAudioTrack at){
        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, at.getIdentifier(), null) //TODO: Add thumb
                .setTitle(at.getInfo().title, DEFAULT_URL)
                .setDescription("Geladen von Twitch")
                .addField("Aktuelle Autoplaylist", Main.getGuildAudioPlayer(channel.getGuild()).playlist.getName(), true)
                .setColor(new Color(100, 65, 164))
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl())
                .build();

        channel.sendMessage(embed).queue(new MessageDeleter());
    }

    private void sendHttpEmbed(TextChannel channel, HttpAudioTrack at) {
        String desc = at.getDuration() == Long.MAX_VALUE ?
                "[LIVE]" :
                TextUtils.progressBar(at.getPosition(), at.getDuration(), 25) +
                "\n["
                        + TextUtils.formatMillis(at.getPosition())
                        + "/"
                        + TextUtils.formatMillis(at.getDuration())
                        + "]";

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, null, null)
                .setTitle(at.getInfo().title, at.getIdentifier())
                .setDescription(desc + "\n\nGeladen von " + at.getIdentifier()) //TODO: Probe data
                .addField("Aktuelle Autoplaylist", Main.getGuildAudioPlayer(channel.getGuild()).playlist.getName(), true)
                .setColor(Color.orange)
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl())
                .build();

        channel.sendMessage(embed).queue(new MessageDeleter());
    }

    private void sendGensokyoRadioEmbed(TextChannel channel) {
        try {
            JSONObject data = XML.toJSONObject(Unirest.get("https://gensokyoradio.net/xml/").asString().getBody()).getJSONObject("GENSOKYORADIODATA");

            String rating = data.getJSONObject("MISC").getInt("TIMESRATED") == 0 ?
                    "Noch keine" :
                    data.getJSONObject("MISC").getInt("RATING") + "/5 from " + data.getJSONObject("MISC").getInt("TIMESRATED") + " vote(s)";

            String albumArt = data.getJSONObject("MISC").getString("ALBUMART").equals("") ?
                    "https://gensokyoradio.net/images/albums/c200/gr6_circular.png" :
                    "https://gensokyoradio.net/images/albums/original/" + data.getJSONObject("MISC").getString("ALBUMART");

            String titleUrl = data.getJSONObject("MISC").getString("CIRCLELINK").equals("") ?
                    "https://gensokyoradio.net/" :
                    data.getJSONObject("MISC").getString("CIRCLELINK");

            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle(data.getJSONObject("SONGINFO").getString("TITLE"), titleUrl)
                    .addField("Album", data.getJSONObject("SONGINFO").getString("ALBUM"), true)
                    .addField("Artist", data.getJSONObject("SONGINFO").getString("ARTIST"), true)
                    .addField("Circle", data.getJSONObject("SONGINFO").getString("CIRCLE"), true);

            if(data.getJSONObject("SONGINFO").optInt("YEAR") != 0){
                eb.addField("Year", Integer.toString(data.getJSONObject("SONGINFO").getInt("YEAR")), true);
            }

            eb.addField("Bewertung", rating, true)
                    .addField("Zuhörer", Integer.toString(data.getJSONObject("SERVERINFO").getInt("LISTENERS")), true)
                    .setImage(albumArt)
                    .setColor(new Color(66, 16, 80))
                    .addField("Aktuelle Autoplaylist", Main.getGuildAudioPlayer(channel.getGuild()).playlist.getName(), true)
                    .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl())
                    .build();

            channel.sendMessage(eb.build()).queue(new MessageDeleter());
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendYoutubeEmbed(TextChannel channel, YoutubeAudioTrack at) {
        YoutubeVideo yv = YoutubeAPI.getVideoFromID(at.getIdentifier(), true);
        String timeField =
                TextUtils.progressBar(at.getPosition(), at.getDuration(), 25) +
                "\n["
                + TextUtils.formatMillis(at.getPosition())
                + "/"
                + TextUtils.formatMillis(at.getDuration())
                + "]";

        String desc = yv.getDescription();

        //Shorten it to about 400 chars if it's too long
        if(desc.length() > 450){
            desc = TextUtils.substringPreserveWords(desc, 400) + " [...]";
        }

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(at.getInfo().title, "https://www.youtube.com/watch?v=" + at.getIdentifier())
                .addField("Zeit", timeField, true);

        if(desc != null && !desc.equals("")) {
            eb.addField("Beschreibung", desc, false);
        }

        MessageEmbed embed = eb.setColor(new Color(205, 32, 31))
                .setThumbnail("https://i.ytimg.com/vi/" + at.getIdentifier() + "/hqdefault.jpg")
                .setAuthor(yv.getCannelTitle(), yv.getChannelUrl(), yv.getChannelThumbUrl())
                .addField("Aktuelle Autoplaylist", Main.getGuildAudioPlayer(channel.getGuild()).playlist.getName(), true)
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl())
                .build();
        channel.sendMessage(embed).queue(new MessageDeleter());
    }

    private void sendSoundcloudEmbed(TextChannel channel, SoundCloudAudioTrack at) {
        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, null, null)
                .setTitle(at.getInfo().title, DEFAULT_URL)
                .setDescription(
                        TextUtils.progressBar(at.getPosition(), at.getDuration(), 25) +
                        "\n["
                        + TextUtils.formatMillis(at.getPosition())
                        + "/"
                        + TextUtils.formatMillis(at.getDuration())
                        + "]\n\nGeladen von Soundcloud") //TODO: Gather description, thumbnail, etc
                .addField("Aktuelle Autoplaylist", Main.getGuildAudioPlayer(channel.getGuild()).playlist.getName(), true)
                .setColor(new Color(255, 85, 0))
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl())
                .build();

        channel.sendMessage(embed).queue(new MessageDeleter());
    }
}

