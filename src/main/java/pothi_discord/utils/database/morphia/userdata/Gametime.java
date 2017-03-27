package pothi_discord.utils.database.morphia.userdata;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Version;
import pothi_discord.utils.database.morphia.DataClass;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
@Entity(value = "gametimes", noClassnameStored = true)
public class Gametime extends DataClass<ObjectId> implements Comparable<Gametime>{
    private String gameName;

    @Embedded private List<TimePair> timePairs = new ArrayList<>();

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public List<TimePair> getTimePairs() {
        return timePairs;
    }

    public void setTimePairs(List<TimePair> timePairs) {
        this.timePairs = timePairs;
    }

    @Override
    public int compareTo(Gametime o) {
        return this.gameName.compareTo(o.getGameName());
    }
}
