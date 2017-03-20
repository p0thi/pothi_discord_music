package pothi_discord_music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord_music.utils.Param;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public class DiscordBot extends Main{
    private static final Logger log = LoggerFactory.getLogger(DiscordBot.class);

    //Instance variables
    AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    JDA jda;
    private int shardId;

    public DiscordBot(int shardId) {
        this.shardId = shardId;

        boolean success = false;

        try {
            while (!success) {
                JDABuilder builder = new JDABuilder(AccountType.BOT)
                        .addListener(messageListener)
                        .setToken(Param.BOT_TOKEN())
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

    public JDA getJDA() {
        return jda;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }


    public ShardInfo getShardInfo() {
        int sId = jda.getShardInfo() == null ? 0 : jda.getShardInfo().getShardId();

        if(jda.getAccountType() == AccountType.CLIENT) {
            return new ShardInfo(0, 1);
        } else {
            return new ShardInfo(sId, numShards);
        }
    }

    public class ShardInfo {

        int shardId;
        int shardTotal;

        ShardInfo(int shardId, int shardTotal) {
            this.shardId = shardId;
            this.shardTotal = shardTotal;
        }

        public int getShardId() {
            return this.shardId;
        }

        public int getShardTotal() {
            return this.shardTotal;
        }

        public String getShardString() {
            return String.format("[%02d / %02d]", this.shardId, this.shardTotal);
        }

    }
}
