package pothi_discord.utils.database.morphia.guilddatas;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import pothi_discord.utils.database.morphia.DataClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@Entity(value = "soundcommands", noClassnameStored = true)
public class SoundCommand extends DataClass<String>{
    @Embedded
    private List<SoundCommandEntry> soundCommandEntries = new ArrayList<>();
    @Embedded
    private List<SoundCommandEntry> tmpSoundCommandEntries = new ArrayList<>();
    private List<String> bannedAudioCommandUsers = new ArrayList<>();

    public List<SoundCommandEntry> getSoundCommandEntries() {
        return soundCommandEntries;
    }

    public void setSoundCommandEntries(List<SoundCommandEntry> soundCommandEntries) {
        this.soundCommandEntries = soundCommandEntries;
    }

    public List<SoundCommandEntry> getTmpSoundCommandEntries() {
        return tmpSoundCommandEntries;
    }

    public void setTmpSoundCommandEntries(List<SoundCommandEntry> tmpSoundCommandEntries) {
        this.tmpSoundCommandEntries = tmpSoundCommandEntries;
    }

    public List<String> getBannedAudioCommandUsers() {
        return bannedAudioCommandUsers;
    }

    public void setBannedAudioCommandUsers(List<String> bannedAudioCommandUsers) {
        this.bannedAudioCommandUsers = bannedAudioCommandUsers;
    }
}
