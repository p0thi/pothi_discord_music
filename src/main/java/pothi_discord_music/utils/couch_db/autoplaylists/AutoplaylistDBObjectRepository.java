package pothi_discord_music.utils.couch_db.autoplaylists;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

/**
 * Created by Pascal Pothmann on 24.02.2017.
 */
public class AutoplaylistDBObjectRepository extends CouchDbRepositorySupport<AutoplaylistDBObject> {
    public AutoplaylistDBObjectRepository(CouchDbConnector db) {
        super(AutoplaylistDBObject.class, db);
    }
}
