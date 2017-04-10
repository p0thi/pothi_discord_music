package pothi_discord.utils.database.morphia.guilddatas;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.Query;
import pothi_discord.Main;
import pothi_discord.utils.database.morphia.DataClass;
import pothi_discord.utils.database.morphia.MongoAudioTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.mongodb.client.model.Filters.*;

/**
 * Created by Pascal Pothmann on 24.02.2017.
 */
@Entity(value = "autoplaylists", noClassnameStored = true)
public class MongoAutoplaylist extends DataClass<ObjectId>{

    private String title;
    @Embedded
    private List<MongoAudioTrack> content = new ArrayList<>();

    private boolean genre;

    /////////////////////////////////////////////////////////////////////////

    public static MongoAutoplaylist getGenreByName(String name){
        Query<MongoAutoplaylist> query = Main.datastore.createQuery(MongoAutoplaylist.class);
        MongoAutoplaylist result = query.field("title").equal(name).get();
        return result;
    }

    public static MongoAutoplaylist getObjectById(String id){
        Query<MongoAutoplaylist> query = Main.datastore.createQuery(MongoAutoplaylist.class);
        MongoAutoplaylist result = query.field("_id").equal(new ObjectId(id)).get();
        return result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MongoAudioTrack> getContent() {
        return content;
    }

    public void setContent(List<MongoAudioTrack> content) {
        this.content = content;
    }

    public boolean isGenre() {
        return genre;
    }

    public void setGenre(boolean genre) {
        this.genre = genre;
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
