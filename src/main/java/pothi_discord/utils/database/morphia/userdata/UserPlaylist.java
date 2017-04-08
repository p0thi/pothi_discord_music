package pothi_discord.utils.database.morphia.userdata;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import pothi_discord.utils.database.morphia.DataClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 06.04.2017.
 */
@Entity(value = "userplaylists", noClassnameStored = true)
public class UserPlaylist extends DataClass<ObjectId> {

    private String name;
    @Embedded
    private List<UserAudioTrack> tracks = new ArrayList<>();


    public int getIndexOfExactTitle(String title) {
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserAudioTrack> getTracks() {
        return tracks;
    }

    public void setTracks(List<UserAudioTrack> tracks) {
        this.tracks = tracks;
    }
}
