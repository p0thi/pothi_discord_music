package pothi_discord.bots;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.bots.music.listeners.MusicTrackScheduler;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;
import pothi_discord.managers.GuildAudioManager;
import pothi_discord.managers.GuildCommandManager;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
public abstract class Bot {
    private final Logger log = MorphiaLoggerFactory.get(this.getClass());

    static final int SHARD_CREATION_SLEEP_INTERVAL = 5100;

    public final ArrayList<BotShard> shards = new ArrayList<>();
    protected AtomicInteger numShardsReady = new AtomicInteger(0);
    protected int numShards = 2;
    protected Map<String, MusicTrackScheduler> trackSchedulers = new HashMap<>();
    protected Map<String, GuildCommandManager> guildCommandManagers = new HashMap<>();
    protected Map<String, GuildAudioManager> allAudioManagers = new HashMap<>();


    public abstract String getToken();
    public abstract void onInit(ReadyEvent event);



    public Map<String, MusicTrackScheduler> getTrackSchedulers() {
        return trackSchedulers;
    }

    public Map<String, GuildCommandManager> getGuildCommandManagers() {
        return guildCommandManagers;
    }

    public VoiceChannel getBestVoiceChannel(Member member, List<VoiceChannel> voiceChannels) {
        GuildData guildData = GuildData.getGuildDataByGuildId(member.getGuild().getId());

        if (guildData.getAutoJoinBestChannel()) {
            VoiceChannel bestChannel = voiceChannels.get(0);
            int bestCount = 0;
            // TODO default channel could be in the settings/database

            for (VoiceChannel voiceChannel : voiceChannels) {

                if (voiceChannel.getGuild().getAfkChannel().compareTo(voiceChannel) == 0) {
                    continue;
                }

                List<Permission> memberPermissions = member.getPermissions(voiceChannel);

                if (!(memberPermissions.contains(Permission.VOICE_CONNECT)
                        && memberPermissions.contains(Permission.VOICE_SPEAK))) {
                    continue;
                }
                int currentCount = ((GuildMusicManager) getGuildAudioPlayer(member.getGuild()))
                        .getMembersInChannelWithoutBots(voiceChannel).size();
                if (currentCount > bestCount) {
                    bestChannel = voiceChannel;
                    bestCount = currentCount;
                }
            }
            return bestChannel;
        }
        else {
            return voiceChannels.get(0);
        }

    }

    public void connectToVoiceChannel(AudioManager audioManager) {
        if (audioManager.isConnected() || audioManager.isAttemptingToConnect()) {
            audioManager.closeAudioConnection();
        }
        Guild guild = audioManager.getGuild();
        List<VoiceChannel> voiceChannels = guild.getVoiceChannels();
        VoiceChannel bestVoiceChannel =
                getBestVoiceChannel(guild.getMember(audioManager.getJDA().getSelfUser()), voiceChannels);

        try {
            audioManager.openAudioConnection(bestVoiceChannel);
        } catch (Exception e) {
            for (VoiceChannel voiceChannel : voiceChannels) {
                try {
                    audioManager.openAudioConnection(voiceChannel);
                } catch (Exception e1) {}
                break;
            }
        }
    }

    public Member getMemberOfActiveVoiceChannel(String userId) {

        System.out.println("\nAll guild:");
        System.out.println("\t\tUserId: " + userId);
        for (Guild guild : getAllGuilds()) {
            System.out.println("\t" + guild.getName());
            Member member = guild.getMemberById(userId);
            if (member == null) {
                continue;
            }
            if (member.getVoiceState().inVoiceChannel()) {
                return member;
            }
        }
        return null;
    }


    public BotShard getDiscordBotByJDA(JDA jda) {
        if (shards.size() == 1 && numShards == 1) {
            return shards.get(0);
        }
        int shardId = jda.getShardInfo().getShardId();
        for (BotShard shard : shards) {
            if (shard.getJDA().getShardInfo().getShardId() == shardId) {
                return shard;
            }
        }
        return null;
    }


    public BotShard getInstance(JDA jda) {

        int sId = jda.getShardInfo() == null ? 0 : jda.getShardInfo().getShardId();

        for(BotShard fb : shards) {
            if((fb).getShardInfo().getShardId() == sId) {
                return fb;
            }
        }


        throw new IllegalStateException("Attempted to get instance for JDA shard that is not indexed");
    }



    public List<Guild> getAllGuilds(){
        HashMap<String, Guild> guilds = new HashMap<>();
        for(BotShard bot : shards) {
            for(Guild guild : bot.getJDA().getGuilds()) {
                guilds.put(guild.getId(), guild);
            }
        }
        return new ArrayList<>(guilds.values());
    }

    public Guild getGuildById(String id) {
        for (BotShard bot : shards) {
            for (Guild guild : bot.getJDA().getGuilds()) {
                if (guild.getId().equals(id)) {
                    return guild;
                }
            }
        }
        return null;
    }

    public abstract GuildAudioManager getGuildAudioPlayer(Guild guild);
}
