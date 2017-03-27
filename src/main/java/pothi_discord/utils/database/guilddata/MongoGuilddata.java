package pothi_discord.utils.database.guilddata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import pothi_discord.Main;
import pothi_discord.utils.database.guilddata.audio_commands.MongoAudioCommand;
import pothi_discord.utils.database.guilddata.permissions.MongoGuildPermissionsObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

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
    private MongoGuildPermissionsObject permissions = new MongoGuildPermissionsObject();
    private List<MongoAudioCommand> soundCommands = new ArrayList<>();
    private List<MongoAudioCommand> tmpSoundCommands = new ArrayList<>();
    private int audioCommandsStartVolume;


    //////////////////////////////////////////////////

    public static MongoGuilddata getObjectById(String id) {
        MongoGuilddata tmpObj = null;
        MongoCollection<Document> col = Main.mongoDB
                .getMongoDatabase("pothibot")
                .getCollection("guilddatas");

        FindIterable<Document> iterable = col.find(eq("_id", id));


        ObjectMapper mapper = new ObjectMapper();

        if(iterable.first() == null) {
            try {
                String newObj = mapper.writeValueAsString(defaultObjectById(id));
                System.out.println(newObj);
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

    public MongoGuildPermissionsObject getPermissions() {
        return permissions;
    }

    public void setPermissions(MongoGuildPermissionsObject permissions) {
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

    public List<MongoAudioCommand> getSoundCommands() {
        return soundCommands;
    }

    public void setSoundCommands(List<MongoAudioCommand> soundCommands) {
        this.soundCommands = soundCommands;
    }

    public void addSoundCommand(MongoAudioCommand command) {
        this.soundCommands.add(command);
        try {
            addListItem(command, this.soundCommands, "soundCommands");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    public void removeSoundCommand(MongoAudioCommand command) {
        this.soundCommands.remove(command);
        try {
            removeListItem(command, this.soundCommands, "soundCommands");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void clearSoundCommands(){
        this.soundCommands.clear();
        try {
            clearList(this.soundCommands, "soundComands");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public List<MongoAudioCommand> getTmpSoundCommands() {
        return tmpSoundCommands;
    }

    public void setTmpSoundCommands(List<MongoAudioCommand> tmpSoundCommands) {
        this.tmpSoundCommands = tmpSoundCommands;
    }

    public void addTmpSoundCommand(MongoAudioCommand command) {
        this.tmpSoundCommands.add(command);
        try {
            addListItem(command, this.tmpSoundCommands, "tmpSoundCommands");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    public void removeTmpSoundCommand(MongoAudioCommand command) {
        this.tmpSoundCommands.remove(command);
        try {
            removeListItem(command, this.tmpSoundCommands, "tmpSoundCommands");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void clearTmpSoundCommands(){
        this.tmpSoundCommands.clear();
        try {
            clearList(this.tmpSoundCommands, "tmpSoundCommands");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }



    private <T> void addListItem(T newItem, List<T> list, String fieldName) throws JsonProcessingException {
        MongoCollection<Document> col = Main.mongoDB
                .getMongoDatabase("pothibot")
                .getCollection("guilddatas");

        ObjectMapper mapper = new ObjectMapper();
        String value = mapper.writeValueAsString(newItem);
        list.add(newItem);

        col.updateOne(eq("_id", getId()), push(fieldName, Document.parse(value)));
    }

    private <T> void removeListItem(T newItem, List<T> list, String fieldName) throws JsonProcessingException {
        MongoCollection<Document> col = Main.mongoDB
                .getMongoDatabase("pothibot")
                .getCollection("guilddatas");

        ObjectMapper mapper = new ObjectMapper();
        String value = mapper.writeValueAsString(newItem);
        list.remove(newItem);

        col.updateOne(eq("_id", getId()), pull(fieldName, Document.parse(value)));
    }

    private <T> void clearList(List<T> list, String fieldName) throws JsonProcessingException {
        MongoCollection<Document> col = Main.mongoDB
                .getMongoDatabase("pothibot")
                .getCollection("guilddatas");
        for (T t : list) {
            ObjectMapper mapper = new ObjectMapper();
            String value = mapper.writeValueAsString(t);

            col.updateOne(eq("_id", getId()), pull(fieldName, Document.parse(value)));
        }
;

    }

    private boolean saveField(String fieldname, Object value) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String newValue = mapper.writeValueAsString(value);

        if (value instanceof Collection || newValue.startsWith("[")) {
            throw new RuntimeException("cant save List.");
        }

        MongoCollection<Document> col = Main.mongoDB
                .getMongoDatabase("pothibot")
                .getCollection("guilddatas");

        UpdateResult result = col.updateOne(eq("_id", getId()), set(fieldname, Document.parse(newValue)));
        return result.wasAcknowledged()
                && result.isModifiedCountAvailable()
                && result.getMatchedCount() == 1
                && result.getModifiedCount() == 1;
    }

    public int getAudioCommandsStartVolume() {
        return audioCommandsStartVolume;
    }

    public void setAudioCommandsStartVolume(int audioCommandsStartVolume) {
        this.audioCommandsStartVolume = audioCommandsStartVolume;
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
