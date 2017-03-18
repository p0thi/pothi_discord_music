package pothi_discord_music.utils.couch_db.autoplaylists;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.CouchDbDocument;
import pothi_discord_music.utils.Param;

import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.02.2017.
 */

public class AutoplaylistDBObject extends CouchDbDocument{
    private static final String DATABASE_NAME = "autoplaylists";

    private String title;
    private List<String> content;

    private boolean isGenre;

    /////////////////////////////////////////////////////////////////////////

    private HttpClient httpClient;
    private CouchDbInstance dbInstance;
    private CouchDbConnector db;
    private AutoplaylistDBObjectRepository repo;


    public static AutoplaylistDBObject getObjectById(String id){
        AutoplaylistDBObject result = null;
        try {
            HttpClient httpClient = new StdHttpClient.Builder()
                    .url(Param.DATABASE_ROOT)
                    .build();

            CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
            CouchDbConnector db = new StdCouchDbConnector(DATABASE_NAME, dbInstance);

            AutoplaylistDBObjectRepository repo = new AutoplaylistDBObjectRepository(db);

            result = repo.get(id);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public boolean isIsGenre() {
        return isGenre;
    }

    public void setGenre(boolean genre) {
        this.isGenre = genre;
    }
}
