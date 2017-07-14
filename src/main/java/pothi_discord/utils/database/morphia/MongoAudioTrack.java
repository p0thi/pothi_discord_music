package pothi_discord.utils.database.morphia;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.mongodb.morphia.annotations.Embedded;
import pothi_discord.Main;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Pascal Pothmann on 06.04.2017.
 */
@Embedded
public class MongoAudioTrack {

    private String title;
    private String author;
    private Long length;
    private String identifier;
    private String uri;

    public static MongoAudioTrack convertAudioTrack(AudioTrack audioTrack) {
        AudioTrackInfo info = audioTrack.getInfo();

        if (info.isStream) {
            throw new RuntimeException("Cant handle Strams");
        }

        MongoAudioTrack result = new MongoAudioTrack();

        result.setTitle(info.title);
        result.setAuthor(info.author);
        result.setLength(info.length);
        result.setIdentifier(info.identifier);
        result.setUri(info.uri);

        return result;
    }

    public static MongoAudioTrack getTrackFromIdentifier(String identifier) {
        AudioLoader loader = new AudioLoader();
        try {
            Main.musicBot.shards.get(0).getPlayerManager().loadItem(identifier, loader).get();
            ArrayList<AudioTrack> results = loader.tracks;

            if (results.size() != 1) {
                throw new Exception();
            }
            
            return convertAudioTrack(results.get(0));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null
                || ! (obj instanceof MongoAudioTrack)) {
            return false;
        }
        return this.getIdentifier().equals(((MongoAudioTrack) obj).getIdentifier());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    private static class AudioLoader implements AudioLoadResultHandler {
        ArrayList<AudioTrack> tracks = null;
        int resultCode = 0;

        @Override
        public void trackLoaded(AudioTrack track) {
            this.tracks = new ArrayList<>();
            this.resultCode = 1;
            this.tracks.add(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            this.tracks = new ArrayList<>();
            this.resultCode = 2;
            this.tracks.addAll(playlist.getTracks());
        }

        @Override
        public void noMatches() {
            this.resultCode = 3;
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            this.resultCode = 4;
        }
    }
}
