package pothi_discord_music;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.events.ReadyEvent;
import pothi_discord_music.commands.controll.StatusCommand;
import pothi_discord_music.commands.controll.*;
import pothi_discord_music.commands.fun.GifCommand;
import pothi_discord_music.commands.fun.JokeCommand;
import pothi_discord_music.commands.fun.RollCommand;
import pothi_discord_music.commands.music.*;

import pothi_discord_music.handlers.GuildReceiveHandler;
import pothi_discord_music.handlers.StaticSchedulePool;
import pothi_discord_music.listeners.MessageListener;
import pothi_discord_music.listeners.TrackScheduler;
import pothi_discord_music.managers.GuildCommandManager;
import pothi_discord_music.managers.music.GuildMusicManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord_music.utils.DiscordUtil;
import pothi_discord_music.utils.GuildData;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.TextUtils;
import pothi_discord_music.utils.audio.AudioUtils;
import pothi_discord_music.utils.couch_db.guilddata.GuildDBObject;
import pothi_discord_music.utils.log.SimpleLogToSLF4JAdapter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static final Main self = new Main();
    static final int SHARD_CREATION_SLEEP_INTERVAL = 5100;
    private static AtomicInteger numShardsReady = new AtomicInteger(0);
    public static final long START_TIME = System.currentTimeMillis();

    // Static variables

    public static final ArrayList<DiscordBot> shards = new ArrayList<>();
    static Map<String, TrackScheduler> trackSchedulers = new HashMap<>();
    static Map<String, GuildCommandManager> guildCommandManagers = new HashMap<>();
    static Map<String, GuildMusicManager> allMusicManagers = new HashMap<>();
    static MessageListener messageListener = new MessageListener();
    static int numShards = 2;


    public static void main(String[] args) {
        System.setProperty("file.encoding","UTF-8");
        Field charset = null;
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
            TextUtils.applyConfigFile("./pothi_discord_music/config/config.txt");
        } catch (IOException e) {
            log.error("Could not load Config! Exiting...");
            return;
        }


        //Attach log adapter
        SimpleLog.addListener(new SimpleLogToSLF4JAdapter());

        //Make jda not print to console, we have Logback for that
        SimpleLog.LEVEL = SimpleLog.Level.OFF;

        initShards();

        //This is the listener...TODO
        // jda.getGuildById("273812217445744640").getAudioManager().setReceivingHandler(new GuildReceiveHandler());

    }

    private static void initShards() {

        ///*
        try {
            numShards = DiscordUtil.getRecommendedShardCount(Param.BOT_TOKEN);
        } catch (UnirestException e) {
            throw new RuntimeException("Unable to get recommended shard count!", e);
        }
        //*/

        for (int i = 0; i < numShards; i++) {
            DiscordBot bot = new DiscordBot(i);
            shards.add(bot);

            try {
                Thread.sleep(SHARD_CREATION_SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException("Got interrupted while setting up bot shards!", e);
            }
        }
    }

    public static void onInit(ReadyEvent readyEvent) {
        int ready = numShardsReady.incrementAndGet();
        //log.info("Received ready event for " + Main.getInstance(readyEvent.getJDA()).getShardInfo().getShardString());
        // log.info("Received ready event for " + readyEvent.getJDA().getShardInfo().getShardString());

        //CommandInitializer.initCommands();
        for(Guild guild : readyEvent.getJDA().getGuilds()) {
            if (!guildCommandManagers.containsKey(guild.getId())) {
                guildCommandManagers.put(guild.getId(), new GuildCommandManager(guild));
            }

            GuildCommandManager gcm = guildCommandManagers.get(guild.getId());


            //Commands TODO: special commands per guild
            gcm.addCommand("play", new PlayCommand());
            gcm.addCommand("pause", new PauseCommand());
            gcm.addCommand("queue", new QueueCommand());
            gcm.addCommand("skip", new SkipCommand());
            gcm.addCommand("volume", new VolumeCommand());
            gcm.addCommand("shuffle", new ShuffleCommand());
            gcm.addCommand("record", new RecordCommand());
            gcm.addCommand("joke", new JokeCommand());
            gcm.addCommand("fetch", new FetchCommand());
            gcm.addCommand("reset", new ResetCommand());
            gcm.addCommand("summon", new SummonCommand());
            gcm.addCommand("help", new HelpCommand());
            gcm.addCommand("ping", new PingCommand());
            gcm.addCommand("status", new StatusCommand());
            gcm.addCommand("id", new IdCommand());



            gcm.addCommand("permission", new PermissionsCommand());
            gcm.addAlias("permissions", "permission");
            gcm.addCommand("shutdown", new ShutdownCommand());
            gcm.addAlias("s", "shutdown");
            gcm.addCommand("gif", new GifCommand());
            gcm.addAlias("g", "gif");
            gcm.addCommand("roll", new RollCommand());
            gcm.addAlias("r", "roll");
            gcm.addCommand("np", new NowPlayingCommand());
            gcm.addAlias("nowplaying", "np");

        }

        if(ready == numShards) {
            log.info("All " + ready + " shards are ready.");

            for(DiscordBot bot : shards) {
                List<Guild> allGuilds = bot.getJDA().getGuilds();
                log.info("Connecting to " + allGuilds.size() + " guilds...");
                for(Guild guild : allGuilds) {
                    //TODO should also happen on GuildJoin event!!!!
                    GuildData guildData = new GuildData(guild);
                    GuildData.ALL_GUILD_DATAS.put(guild.getId(), guildData);

                    GuildReceiveHandler grh = new GuildReceiveHandler(guild);
                    guildData.setGuildReceiveHandler(grh);

                    guildData.setAudioUtils(new AudioUtils());
                    try {
                        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
                        musicManager.connectAndPlay();
                        log.info("Joining guild. (" + guild.getName() + ")");
                    } catch (InterruptedException e){
                        log.warn("could not autojoin guild: " + guild.getName());
                    }
                }
            }
        }
    }

    public static DiscordBot getInstance(JDA jda) {

        int sId = jda.getShardInfo() == null ? 0 : jda.getShardInfo().getShardId();

        for(DiscordBot fb : shards) {
            if((fb).getShardInfo().getShardId() == sId) {
                return fb;
            }
        }


        throw new IllegalStateException("Attempted to get instance for JDA shard that is not indexed");
    }

    public static List<Guild> getAllGuilds(){
        HashMap<String, Guild> guilds = new HashMap<>();
        for(DiscordBot bot : shards) {
            for(Guild guild : bot.getJDA().getGuilds()) {
                guilds.put(guild.getId(), guild);
            }
        }
        return new ArrayList<>(guilds.values());
    }

    public static synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        String guildId = guild.getId();
        GuildMusicManager musicManager = allMusicManagers.get(guildId);

        if(musicManager == null) {
            musicManager = new GuildMusicManager(guild, getDiscordBotByJDA(guild.getJDA()).getPlayerManager());
            allMusicManagers.put(guildId, musicManager);
            GuildDBObject guildDBObject = GuildData.ALL_GUILD_DATAS.get(guildId).getGuildDBObject();
            musicManager.player.setVolume(guildDBObject.getPlayerStartVolume());
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public static DiscordBot getDiscordBotByJDA(JDA jda) {
        if (shards.size() == 1 && numShards == 1) {
            return shards.get(0);
        }
        int shardId = jda.getShardInfo().getShardId();
        for (DiscordBot shard : shards) {
            if (shard.getJDA().getShardInfo().getShardId() == shardId) {
                return shard;
            }
        }
        return null;
    }

    public static void connectToVoiceChannel(AudioManager audioManager) {
        if (audioManager.isConnected() || audioManager.isAttemptingToConnect()) {
            audioManager.closeAudioConnection();
        }
        Guild guild = audioManager.getGuild();
        List<VoiceChannel> voiceChannels = guild.getVoiceChannels();
        VoiceChannel defaultVoice = null;

        //TODO: get specific VOiceChannel
        for(VoiceChannel vc : voiceChannels) {
            if (Param.isInList(Param.DEFAULT_CHANNELS, vc.getId())) {
                defaultVoice = vc;
                break;
            }
        }
        try {
            audioManager.openAudioConnection(defaultVoice);
        } catch (Exception e) {
            for (VoiceChannel voiceChannel : voiceChannels) {
                try {
                    audioManager.openAudioConnection(voiceChannel);
                } catch (Exception e1) {}
                break;
            }
        }
    }

    public static Map<String, TrackScheduler> getTrackSchedulers() {
        return trackSchedulers;
    }

    public static Map<String, GuildCommandManager> getGuildCommandManagers() {
        return guildCommandManagers;
    }
}
