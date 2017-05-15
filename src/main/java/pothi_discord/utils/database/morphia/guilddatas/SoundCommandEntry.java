package pothi_discord.utils.database.morphia.guilddatas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by Pascal Pothmann on 15.05.2017.
 */
@Embedded
//@JsonIgnoreProperties(ignoreUnknown = true)
public class SoundCommandEntry {
    private String guildId;
    private String command;
    private String description;
    private String fileId;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
}