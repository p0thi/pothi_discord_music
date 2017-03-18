package pothi_discord_music.utils.couch_db.guilddata;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

/**
 * Created by Pascal Pothmann on 24.02.2017.
 */
public class GuildDBObjectRepository extends CouchDbRepositorySupport<GuildDBObject> {

    public GuildDBObjectRepository(CouchDbConnector db) {
        super(GuildDBObject.class, db);
    }
}
