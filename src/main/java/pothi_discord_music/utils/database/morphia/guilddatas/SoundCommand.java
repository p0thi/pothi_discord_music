package pothi_discord_music.utils.database.morphia.guilddatas;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@Embedded
public class SoundCommand {
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
}
