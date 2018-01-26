package pothi_discord.utils.database.morphia.autoplaylists;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.Query;
import pothi_discord.Main;
import pothi_discord.utils.database.morphia.DataClass;
import pothi_discord.utils.database.morphia.MongoAudioTrack;

import java.beans.Transient;
import java.util.List;
import java.util.Random;

/**
 * Created by Pascal Pothmann on 11.04.2017.
 */
@Entity(value = "autoplaylist", noClassnameStored = true)
public class AutoPlaylist extends DataClass<ObjectId> implements Cloneable{
    private String title;
    private Boolean genre;
    @Embedded
    private List<MongoAudioTrack> content;

    @Transient
    public static AutoPlaylist getAutoPlaylistByName(String name){
        Query<AutoPlaylist> query = Main.datastore.createQuery(AutoPlaylist.class);
        AutoPlaylist result = query.field("title").equal(name).get();
        return result;
    }

    @Transient
    public static AutoPlaylist getAutoPlaylistById(String id){
        AutoPlaylist result = Main.datastore.get(AutoPlaylist.class, new ObjectId(id));

        return result;
    }

    @Transient
    public int getIndexOfExactTitle(String title) {
        for (int i = 0; i < content.size(); i++) {
            if (content.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return -1;
    }

    public MongoAudioTrack getRandomElement(){
        if (content == null || content.size() == 0) {
            return null;
        }
        return content.get(new Random().nextInt(content.size()));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getGenre() {
        return genre;
    }

    public void setGenre(Boolean genre) {
        this.genre = genre;
    }

    public List<MongoAudioTrack> getContent() {
        return content;
    }

    public List<MongoAudioTrack> getContent(int from, int to) {
        //TODO check if out of bounds is possible
        return content.subList(from, to);
    }

    public void setContent(List<MongoAudioTrack> content) {
        this.content = content;
    }
}
