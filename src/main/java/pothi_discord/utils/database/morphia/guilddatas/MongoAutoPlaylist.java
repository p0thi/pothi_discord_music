package pothi_discord.utils.database.morphia.guilddatas;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.Query;
import pothi_discord.Main;

import pothi_discord.utils.database.morphia.DataClass;
import pothi_discord.utils.database.morphia.MongoAudioTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Pascal Pothmann on 24.02.2017.
 */
@Entity(value = "autoplaylists", noClassnameStored = true)
public class MongoAutoPlaylist extends DataClass<ObjectId> {

    private String title;
    @Embedded
    private List<MongoAudioTrack> content = new ArrayList<>();

    private boolean genre;

    /////////////////////////////////////////////////////////////////////////

    public static MongoAutoPlaylist getGenreByName(String name){
        Query<MongoAutoPlaylist> query = Main.datastore.createQuery(MongoAutoPlaylist.class);
        MongoAutoPlaylist result = query.field("title").equal(name).get();
        return result;
    }

    public static MongoAutoPlaylist getObjectById(String id){
        MongoAutoPlaylist result = Main.datastore.get(MongoAutoPlaylist.class, new ObjectId(id));

        return result;
    }

    public MongoAudioTrack getRandomElement(){
        if (content == null || content.size() == 0) {
            return null;
        }
        return content.get(new Random().nextInt(content.size()));
    }

    public int size() {
        return content.size();
    }

    public MongoAudioTrack get(int i) {
        if(i >= size() || i < 0) {
            throw new IndexOutOfBoundsException("Index " + i + " not in scope.");
        }
        return this.content.get(i);
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
}
