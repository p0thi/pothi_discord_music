package pothi_discord.utils.database.guilddata.audio_commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.bson.types.ObjectId;
import pothi_discord.Main;

import java.io.IOException;

/**
 * Created by Pascal Pothmann on 21.03.2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class MongoAudioCommand implements Comparable<MongoAudioCommand>{
    private String command;
    private String fileId;
    private String description;

    @JsonCreator
    public static MongoAudioCommand create(String jsonString) {
        MongoAudioCommand result = null;
        ObjectMapper mapper = new ObjectMapper();

        try {
            result = mapper.readValue(jsonString, MongoAudioCommand.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void removeAudioFileFromDatabase(String fileId) {
        MongoDatabase db = Main.mongoDB.getMongoDatabase("pothibot");
        GridFSBucket gridFSBucket = GridFSBuckets.create(db);
        gridFSBucket.delete(new ObjectId(fileId));
    }

    @JsonProperty("command")
    public String getCommand() {
        return command;
    }

    @JsonProperty("command")
    public void setCommand(String command) {
        this.command = command;
    }

    @JsonProperty("fileId")
    public String getFileId() {
        return fileId;
    }

    @JsonProperty("fileId")
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(MongoAudioCommand o) {
        return this.command.compareTo(o.getCommand());
    }
}
