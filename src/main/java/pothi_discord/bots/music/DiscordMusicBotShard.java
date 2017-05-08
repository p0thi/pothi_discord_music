package pothi_discord.bots.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.http.client.config.RequestConfig;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.bots.Bot;
import pothi_discord.bots.BotShard;
import pothi_discord.bots.music.listeners.MusicBotMessageListener;

import java.util.HashMap;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public class DiscordMusicBotShard implements BotShard{
    private static final Logger log = MorphiaLoggerFactory.get(DiscordMusicBotShard.class);;

    //Instance variables
    AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    JDA jda;
    private int shardId;
    private MusicBotMessageListener musicBotMessageListener;
    private int numShards;
    private Bot myMusicBot;
    private HashMap<String, User> allBotUsers;


    public DiscordMusicBotShard(int shardId, int numShards, Bot musicBot) {
        this.musicBotMessageListener = new MusicBotMessageListener(this);
        this.shardId = shardId;
        this.numShards = numShards;
        this.myMusicBot = musicBot;
        allBotUsers = new HashMap<>();

        boolean success = false;

        try {
            while (!success) {
                JDABuilder builder = new JDABuilder(AccountType.BOT)
                        .addEventListener(musicBotMessageListener)
                        .setToken(myMusicBot.getToken())
                        .setBulkDeleteSplittingEnabled(true)
                        .setEnableShutdownHook(false);
                //.setAudioSendFactory(new NativeAudioSendFactory()); // TODO ?

                if (numShards > 1) {
                    builder.useSharding(shardId, numShards);
                }

                try {
                    jda = builder.buildAsync();
                    success = true;
                } catch (RateLimitedException e) {
                    e.printStackTrace();
                    continue;
                }
                playerManager.setHttpRequestConfigurator((requestConfig ->
                        RequestConfig.copy(requestConfig).setConnectTimeout(10000).build()));
                AudioSourceManagers.registerRemoteSources(playerManager);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start jda shard " + shardId, e);
        }
    }

    @Override
    public JDA getJDA() {
        return jda;
    }

    @Override
    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public ShardInfo getShardInfo() {
        int sId = jda.getShardInfo() == null ? 0 : jda.getShardInfo().getShardId();

        if(jda.getAccountType() == AccountType.CLIENT) {
            return new ShardInfo(0, 1);
        } else {
            return new ShardInfo(sId, numShards);
        }
    }

    @Override
    public Bot getMyBot() {
        return myMusicBot;
    }

    public HashMap<String, User> getAllBotUsers() {
        return allBotUsers;
    }
}
