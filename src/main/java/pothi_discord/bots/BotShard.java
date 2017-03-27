package pothi_discord.bots;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.JDA;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
public interface BotShard {
    Bot getMyBot();

    ShardInfo getShardInfo();
    JDA getJDA();
    AudioPlayerManager getPlayerManager();

    class ShardInfo {

        int shardId;
        int shardTotal;

        public ShardInfo(int shardId, int shardTotal) {
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
