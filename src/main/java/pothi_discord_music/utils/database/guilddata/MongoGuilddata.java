package pothi_discord_music.utils.database.guilddata;

import static com.mongodb.client.model.Filters.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import pothi_discord_music.Main;
import pothi_discord_music.utils.database.guilddata.permissions.GuildPermissionDBObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.02.2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MongoGuilddata {
    private static final String DATABASE_NAME = "guilddatas";

    private String id;
    private List<String> autoplaylist = new ArrayList<>();
    private boolean useCustomAutoplaylist = false;
    private boolean recording = false;
    private boolean autoJoin = true;
    private int playerStartVolume = 20;
    private int songSkipPercent = 30;
    private GuildPermissionDBObject permissions = new GuildPermissionDBObject();



    //////////////////////////////////////////////////

    public static MongoGuilddata getObjectById(String id) {
        MongoGuilddata tmpObj = null;
        MongoCollection<Document> col = Main.getMongo()
                .getMongoDatabase("pothibot")
                .getCollection("guilddatas");

        FindIterable<Document> iterable = col.find(eq("_id", id));


        ObjectMapper mapper = new ObjectMapper();

        if(iterable.first() == null) {
            try {
                String newObj = mapper.writeValueAsString(defaultObjectById(id));
                col.insertOne(Document.parse(newObj));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        String json = iterable.first().toJson();

        try {
            tmpObj = mapper.readValue(json, MongoGuilddata.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmpObj;
    }

    public static MongoGuilddata defaultObjectById(String id) {
        MongoGuilddata obj = new MongoGuilddata();
        obj.setId(id);
        return obj;
    }

    ///////////////////////////////////////////////////////////////

    public List<String> getAutoplaylist() {
        return autoplaylist;
    }

    public void setAutoplaylist(List<String> autoplaylist) {
        this.autoplaylist = autoplaylist;
    }

    public boolean isUseCustomAutoplaylist() {
        return useCustomAutoplaylist;
    }

    public void setUseCustomAutoplaylist(boolean useCustomAutoplaylist) {
        this.useCustomAutoplaylist = useCustomAutoplaylist;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public GuildPermissionDBObject getPermissions() {
        return permissions;
    }

    public void setPermissions(GuildPermissionDBObject permissions) {
        this.permissions = permissions;
    }

    public int getPlayerStartVolume() {
        return playerStartVolume;
    }

    public void setPlayerStartVolume(int playerStartVolume) {
        this.playerStartVolume = playerStartVolume;
    }

    public int getSongSkipPercent() {
        return songSkipPercent;
    }

    public void setSongSkipPercent(int songSkipPercent) {
        this.songSkipPercent = songSkipPercent;
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
