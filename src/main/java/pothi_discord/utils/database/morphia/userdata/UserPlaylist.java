package pothi_discord.utils.database.morphia.userdata;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import pothi_discord.utils.database.morphia.DataClass;
import pothi_discord.utils.database.morphia.MongoAudioTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 06.04.2017.
 */
@Entity(value = "userplaylists", noClassnameStored = true)
public class UserPlaylist extends DataClass<ObjectId> {

    private String name;
    @Embedded
    private List<MongoAudioTrack> tracks = new ArrayList<>();


    public int getIndexOfExactTitle(String title) {
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return -1;
    }

    public boolean containsIdentifier(String identifier) {
        for (MongoAudioTrack track : tracks) {
            if (track.getIdentifier().equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    public void removeTrackByIdentifier(String identifier) {
        MongoAudioTrack pseudo = new MongoAudioTrack();
        pseudo.setIdentifier(identifier);

        while(tracks.remove(pseudo));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MongoAudioTrack> getTracks() {
        return tracks;
    }

    public void setTracks(List<MongoAudioTrack> tracks) {
        this.tracks = tracks;
    }

}
