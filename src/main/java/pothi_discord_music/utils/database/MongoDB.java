package pothi_discord_music.utils.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord_music.utils.Param;


import java.util.Arrays;

/**
 * Created by Pascal Pothmann on 20.03.2017.
 */
public class MongoDB {
    private static final Logger log = LoggerFactory.getLogger(MongoDB.class);

    private MongoCredential credentials;
    private ServerAddress serverAddress;
    private MongoClient mongoClient;

    public MongoDB() {
        this.credentials = MongoCredential.createCredential(
                Param.MONGO_USER(),
                "admin",
                Param.MONGO_PW().toCharArray());

        this.serverAddress = new ServerAddress(Param.MONGO_ROOT());
        this.mongoClient = new MongoClient(this.serverAddress, Arrays.asList(this.credentials));
    }

    public MongoDatabase getMongoDatabase(String databaseName) {
        log.info("Accessing MongoDb...");
        return this.mongoClient.getDatabase(databaseName);
    }

    public MongoCredential getCredentials() {
        return credentials;
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}
