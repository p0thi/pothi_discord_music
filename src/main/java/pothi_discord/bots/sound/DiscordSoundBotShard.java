package pothi_discord.bots.sound;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord.bots.Bot;
import pothi_discord.bots.BotShard;
import pothi_discord.bots.sound.listeners.SoundBotMessageListener;

import java.util.HashMap;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public class DiscordSoundBotShard implements BotShard {
    private static final Logger log = LoggerFactory.getLogger(DiscordSoundBotShard.class);

    //Instance variables
    AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    JDA jda;
    private int shardId;
    private SoundBotMessageListener soundBotMessageListener;
    private int numShards;
    private Bot mySoundBotBot;

    public DiscordSoundBotShard(int shardId, int numShards, Bot soundBot) {
        this.soundBotMessageListener = new SoundBotMessageListener(this);
        this.shardId = shardId;
        this.numShards = numShards;
        this.mySoundBotBot = soundBot;

        boolean success = false;

        try {
            while (!success) {
                JDABuilder builder = new JDABuilder(AccountType.BOT)
                        .addEventListener(soundBotMessageListener)
                        .setToken(mySoundBotBot.getToken())
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
                //AudioSourceManagers.registerRemoteSources(playerManager);
                AudioSourceManagers.registerLocalSource(playerManager);
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
        return mySoundBotBot;
    }

}
