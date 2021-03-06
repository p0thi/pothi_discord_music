package pothi_discord.utils.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.utils.Param;


import java.util.Arrays;

/**
 * Created by Pascal Pothmann on 20.03.2017.
 */
public class MongoDB {
    private static final Logger log = MorphiaLoggerFactory.get(MongoFile.class);

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
