package pothi_discord_music.utils.database.autoplaylists;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import pothi_discord_music.Main;

import java.io.IOException;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Pascal Pothmann on 24.02.2017.
 */

public class MongoAutoplaylist {
    private static final String DATABASE_NAME = "autoplaylists";

    private String id;
    private String title;
    private List<String> content;

    private boolean isGenre;

    /////////////////////////////////////////////////////////////////////////


    public static MongoAutoplaylist getObjectById(String id){
        MongoAutoplaylist result = null;

        MongoCollection<Document> col = Main.getMongo()
                .getMongoDatabase("pothibot")
                .getCollection("autoplaylists");
        FindIterable<Document> iterable = col.find(eq("_id", id));
        ObjectMapper mapper = new ObjectMapper();

        String json = iterable.first().toJson();

        try {
            result = mapper.readValue(json, MongoAutoplaylist.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public boolean isIsGenre() {
        return isGenre;
    }

    public void setGenre(boolean genre) {
        this.isGenre = genre;
    }

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }
}
