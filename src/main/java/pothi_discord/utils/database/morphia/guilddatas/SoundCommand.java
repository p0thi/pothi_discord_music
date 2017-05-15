package pothi_discord.utils.database.morphia.guilddatas;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import pothi_discord.utils.database.morphia.DataClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@Entity(value = "soundcommands", noClassnameStored = true)
public class SoundCommand extends DataClass<String>{

    @Embedded
    private List<SoundCommandEntry> soundCommands = new ArrayList<>();

    @Embedded
    private List<SoundCommandEntry> tmpSoundCommands = new ArrayList<>();
    private List<String> bannedAudioCommandUsers = new ArrayList<>();

    public List<SoundCommandEntry> getSoundCommandEntries() {
        return soundCommands;
    }

    public void setSoundCommandEntries(List<SoundCommandEntry> soundCommands) {
        this.soundCommands = soundCommands;
    }

    public List<SoundCommandEntry> getTmpSoundCommandEntries() {
        return tmpSoundCommands;
    }

    public void setTmpSoundCommandEntries(List<SoundCommandEntry> tmpSoundCommandEntries) {
        this.tmpSoundCommands = tmpSoundCommandEntries;
    }

    public List<String> getBannedAudioCommandUsers() {
        return bannedAudioCommandUsers;
    }

    public void setBannedAudioCommandUsers(List<String> bannedAudioCommandUsers) {
        this.bannedAudioCommandUsers = bannedAudioCommandUsers;
    }
}
