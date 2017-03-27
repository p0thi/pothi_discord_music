package pothi_discord;

import net.dv8tion.jda.core.utils.SimpleLog;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.bots.MusicBot;
import pothi_discord.bots.SoundBot;
import pothi_discord.handlers.StaticSchedulePool;
import pothi_discord.utils.Param;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.MongoDB;
import pothi_discord.utils.log.SimpleLogToSLF4JAdapter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class Main {
    private static final Logger log = MorphiaLoggerFactory.get(Main.class);

    public static final Main self = new Main();
    static final int SHARD_CREATION_SLEEP_INTERVAL = 5100;

    public static final long START_TIME = System.currentTimeMillis();

    public static MusicBot musicBot;
    public static SoundBot soundBot;

    public static MongoDB mongoDB;
    public static Datastore datastore;
    public static Morphia morphia;


    public static void main(String[] args) {
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
        SimpleLog.addListener(new SimpleLogToSLF4JAdapter());

        //Make jda not print to console, we have Logback for that
        SimpleLog.LEVEL = SimpleLog.Level.OFF;

        musicBot = new MusicBot(Param.MUSIC_BOT_TOKEN());
        soundBot = new SoundBot(Param.SOUND_BOT_TOKEN());

        //This is the listener...TODO
        // jda.getGuildById("273812217445744640").getAudioManager().setReceivingHandler(new MusicBotGuildReceiveHandler());

    }

    private static void startMusicBot(){

    }




}
