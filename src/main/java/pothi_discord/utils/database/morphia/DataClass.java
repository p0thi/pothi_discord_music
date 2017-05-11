package pothi_discord.utils.database.morphia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.WriteResult;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord.Main;

import java.util.ConcurrentModificationException;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DataClass<T> {
    private transient final Logger log = LoggerFactory.getLogger(this.getClass());
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

    public Key saveInstance() {
        try {
            return Main.datastore.save(this);
        } catch (ConcurrentModificationException e) {
            log.error("Could not store instance " + this.getClass().getSimpleName() + " " + getId().toString()
                + " | " + e.getMessage());
        }
        return null;
    }

    public WriteResult deleteInstance() {
        try {
            return Main.datastore.delete(this);
        } catch (ConcurrentModificationException e) {
            log.error("Could not delete instance " + this.getClass().getSimpleName() + " " + getId().toString()
                + " | " + e.getMessage());
        }
        return null;
    }
}
