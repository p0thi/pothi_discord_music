package pothi_discord_music.utils.couch_db.guilddata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.CouchDbDocument;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.couch_db.guilddata.permissions.GuildPermissionDBObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.02.2017.
 */
public class GuildDBObject extends CouchDbDocument{
    private static final String DATABASE_NAME = "guilddatas";

    private List<String> autoplaylist = new ArrayList<>();
    private boolean useCustomAutoplaylist = false;
    private boolean recording = false;
    private boolean autoJoin = true;
    private int playerStartVolume = 20;
    private int songSkipPercent = 30;
    private GuildPermissionDBObject permissions = new GuildPermissionDBObject();

    private CouchDbConnector db;
    private GuildDBObjectRepository repo;

    //////////////////////////////////////////////////

    public static GuildDBObject getObjectById(String id) {
        GuildDBObject result = null;
        try {
            HttpClient httpClient = new StdHttpClient.Builder()
                    .url(Param.DATABASE_ROOT)
                    .build();

            CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
            CouchDbConnector db = new StdCouchDbConnector(DATABASE_NAME, dbInstance);

            GuildDBObjectRepository repo = new GuildDBObjectRepository(db);

            if (!repo.contains(id)) {
                repo.add(defaultObjectById(id));
            }

            result = repo.get(id);

            result.setDb(db);
            result.setRepo(repo);

            repo.update(result);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static GuildDBObject defaultObjectById(String id) {
        GuildDBObject obj = new GuildDBObject();
        obj.setId(id);
        return obj;
    }

    public void update() {
        repo.update(this);
    }

    ///////////////////////////////////////////////////////////////

    public List<String> getAutoplaylist() {
        return autoplaylist;
    }

    public void setAutoplaylist(List<String> autoplaylist) {
        this.autoplaylist = autoplaylist;
    }

    public boolean isUseCustomAutoplaylist() {
        return useCustomAutoplaylist;
    }

    public void setUseCustomAutoplaylist(boolean useCustomAutoplaylist) {
        this.useCustomAutoplaylist = useCustomAutoplaylist;
    }

    @JsonIgnore
    public CouchDbConnector getDb() {
        return db;
    }

    @JsonIgnore
    public void setDb(CouchDbConnector db) {
        this.db = db;
    }

    @JsonIgnore
    public GuildDBObjectRepository getRepo() {
        return repo;
    }

    @JsonIgnore
    public void setRepo(GuildDBObjectRepository repo) {
        this.repo = repo;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public GuildPermissionDBObject getPermissions() {
        return permissions;
    }

    public void setPermissions(GuildPermissionDBObject permissions) {
        this.permissions = permissions;
    }

    public int getPlayerStartVolume() {
        return playerStartVolume;
    }

    public void setPlayerStartVolume(int playerStartVolume) {
        this.playerStartVolume = playerStartVolume;
    }

    public int getSongSkipPercent() {
        return songSkipPercent;
    }

    public void setSongSkipPercent(int songSkipPercent) {
        this.songSkipPercent = songSkipPercent;
    }
}
