package pothi_discord_music.commands.music;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.*;
import pothi_discord_music.Main;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.EmbedBuilder;
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
            EmbedBuilder embed = null;
            try {
                if (at instanceof YoutubeAudioTrack) {
                    embed = sendYoutubeEmbed(channel, (YoutubeAudioTrack) at);
                } else if (at instanceof SoundCloudAudioTrack) {
                    embed = sendSoundcloudEmbed(channel, (SoundCloudAudioTrack) at);
                } else if (at instanceof HttpAudioTrack && at.getIdentifier().contains("gensokyoradio.net")) {
                    //Special handling for GR
                    embed = sendGensokyoRadioEmbed(channel);
                } else if (at instanceof HttpAudioTrack) {
                    embed = sendHttpEmbed(channel, (HttpAudioTrack) at);
                } else if (at instanceof BandcampAudioTrack) {
                    embed = sendBandcampResponse(channel, (BandcampAudioTrack) at);
                } else if (at instanceof TwitchStreamAudioTrack) {
                    embed = sendTwitchEmbed(channel, (TwitchStreamAudioTrack) at);
                } else {
                    embed = sendDefaultEmbed(channel, at);
                }
            } catch (Exception e){
                sendDefaultEmbed(channel, at);
            }

            if (embed != null) {
                embed.addField("Aktuelle Autoplaylist", Main.getGuildAudioPlayer(channel.getGuild()).playlist.getName(), true)
                .addField("Volume", musicManager.player.getVolume() + "", false);
                channel.sendMessage(embed.build()).queue(new MessageDeleter());
            }
            else {
                channel.sendMessage("Ein Fehler ist aufgetreten.").queue(new MessageDeleter());
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

    private EmbedBuilder sendDefaultEmbed(TextChannel channel, AudioTrack at) {
        String desc = at.getDuration() == Long.MAX_VALUE ?
                "[LIVE]" :
                TextUtils.progressBar(at.getPosition(), at.getDuration(), 25) +
                        "\n["
                        + TextUtils.formatMillis(at.getPosition())
                        + "/"
                        + TextUtils.formatMillis(at.getDuration())
                        + "]";

        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, null, null)
                .setTitle(at.getInfo().title, DEFAULT_URL)
                .setDescription(desc + "\n\nGeladen von " + at.getSourceManager().getSourceName())
                .setColor(Color.orange)
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl());

        return embed;
    }

    private EmbedBuilder sendBandcampResponse(TextChannel channel, BandcampAudioTrack at) {
        String desc = at.getDuration() == Long.MAX_VALUE ?
                "[LIVE]" :
                "["
                        + TextUtils.formatMillis(at.getPosition())
                        + "/"
                        + TextUtils.formatMillis(at.getDuration())
                        + "]";

        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, null, null)
                .setTitle(at.getInfo().title, DEFAULT_URL)
                .setDescription(desc + "\n\nGeladen von Bandcamp")
                .setColor(new Color(99, 154, 169))
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl());

        return embed;
    }

    private EmbedBuilder sendTwitchEmbed(TextChannel channel, TwitchStreamAudioTrack at){
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, at.getIdentifier(), null) //TODO: Add thumb
                .setTitle(at.getInfo().title, DEFAULT_URL)
                .setDescription("Geladen von Twitch")
                .setColor(new Color(100, 65, 164))
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl());

        return embed;
    }

    private EmbedBuilder sendHttpEmbed(TextChannel channel, HttpAudioTrack at) {
        String desc = at.getDuration() == Long.MAX_VALUE ?
                "[LIVE]" :
                TextUtils.progressBar(at.getPosition(), at.getDuration(), 25) +
                "\n["
                        + TextUtils.formatMillis(at.getPosition())
                        + "/"
                        + TextUtils.formatMillis(at.getDuration())
                        + "]";

        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, null, null)
                .setTitle(at.getInfo().title, at.getIdentifier())
                .setDescription(desc + "\n\nGeladen von " + at.getIdentifier()) //TODO: Probe data
                .setColor(Color.orange)
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl());

        return embed;
    }

    private EmbedBuilder sendGensokyoRadioEmbed(TextChannel channel) {
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
                    .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl())
                    .build();

            return eb;
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
    }

    private EmbedBuilder sendYoutubeEmbed(TextChannel channel, YoutubeAudioTrack at) {
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

        EmbedBuilder embed = eb.setColor(new Color(205, 32, 31))
                .setThumbnail("https://i.ytimg.com/vi/" + at.getIdentifier() + "/hqdefault.jpg")
                .setAuthor(yv.getCannelTitle(), yv.getChannelUrl(), yv.getChannelThumbUrl())
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl());

        return embed;
    }

    private EmbedBuilder sendSoundcloudEmbed(TextChannel channel, SoundCloudAudioTrack at) {
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(at.getInfo().author, null, null)
                .setTitle(at.getInfo().title, DEFAULT_URL)
                .setDescription(
                        TextUtils.progressBar(at.getPosition(), at.getDuration(), 25) +
                        "\n["
                        + TextUtils.formatMillis(at.getPosition())
                        + "/"
                        + TextUtils.formatMillis(at.getDuration())
                        + "]\n\nGeladen von Soundcloud") //TODO: Gather description, thumbnail, etc
                .setColor(new Color(255, 85, 0))
                .setFooter(channel.getJDA().getSelfUser().getName(), channel.getJDA().getSelfUser().getAvatarUrl());

        return embed;
    }
}

