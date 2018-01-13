package pothi_discord;

import net.dv8tion.jda.core.entities.Guild;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import org.springframework.boot.SpringApplication;
import pothi_discord.bots.MusicBot;
import pothi_discord.bots.SoundBot;
import pothi_discord.handlers.ExceptionHandler;
import pothi_discord.handlers.StaticSchedulePool;
import pothi_discord.utils.ErrorLogger;
import pothi_discord.utils.Param;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.MongoDB;
import pothi_discord.rest.WebApi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class Main {
    private static final Logger log = MorphiaLoggerFactory.get(Main.class);

    public static final long START_TIME = System.currentTimeMillis();

    public static MusicBot musicBot;
    public static SoundBot soundBot;

    public static MongoDB mongoDB;
    public static Datastore datastore;
    public static Morphia morphia;


    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {

                ErrorLogger.log(e.getMessage());
        });

        System.setProperty("file.encoding","UTF-8");
        Field charset;
        try {
            charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null,null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> StaticSchedulePool.executeAllTasks()));

        try {
            TextUtils.applyConfigFile("pothi_discord/config/config.json");
        } catch (IOException e) {
            e.printStackTrace();
            log.error("\nCould not load Config! Exiting...");
            return;
        }

        mongoDB = new MongoDB();
        morphia = new Morphia();
        morphia.getMapper().getOptions().setStoreEmpties(true);
        morphia.mapPackage("pothi_discord_music.utils.database.morphia");
        //morphia.mapPackage("pothi_discord_music.utils.database.morphia.guilddatas");
        datastore = morphia.createDatastore(mongoDB.getMongoClient(), "pothibot");


        //Attach log adapter
        //SimpleLog.addListener(new SimpleLogToSLF4JAdapter());

        musicBot = new MusicBot(Param.MUSIC_BOT_TOKEN());
        soundBot = new SoundBot(Param.SOUND_BOT_TOKEN());

        //This is the listener...TODO
        // jda.getGuildById("273812217445744640").getAudioManager().setReceivingHandler(new MusicBotGuildReceiveHandler());

        SpringApplication.run(WebApi.class, args);

    }

    public static List<Guild> getAllGuilds(){
        HashSet<Guild> tmp = new HashSet<>();
        tmp.addAll(musicBot.getAllGuilds());
        tmp.addAll(soundBot.getAllGuilds());
        ArrayList<Guild> result = new ArrayList<>();
        result.addAll(tmp);
        return result;
    }

}
