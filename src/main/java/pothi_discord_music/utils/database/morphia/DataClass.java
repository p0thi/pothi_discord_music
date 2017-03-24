package pothi_discord_music.utils.database.morphia;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Version;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
public abstract class DataClass {
    @Id
    private String id;
    @Version
    private long v;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getV() {
        return v;
    }

    public void setV(long v) {
        this.v = v;
    }
}
