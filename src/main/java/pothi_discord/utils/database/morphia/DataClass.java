package pothi_discord.utils.database.morphia;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Version;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
public abstract class DataClass<T> {
    @Id
    private T id;
    @Version
    private long v;

    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    public long getV() {
        return v;
    }

    public void setV(long v) {
        this.v = v;
    }
}
