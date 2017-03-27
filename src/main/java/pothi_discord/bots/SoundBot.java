package pothi_discord.bots;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ReadyEvent;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.bots.sound.DiscordSoundBotShard;
import pothi_discord.bots.sound.commands.audio.AddFileCommand;
import pothi_discord.bots.sound.commands.controll.CommandsCommand;
import pothi_discord.bots.sound.commands.controll.DeleteCommand;
import pothi_discord.bots.sound.commands.controll.RequestsCommand;
import pothi_discord.bots.sound.managers.audio.GuildSoundManager;
import pothi_discord.commands.PingCommand;
import pothi_discord.managers.GuildAudioManager;
import pothi_discord.managers.GuildCommandManager;
import pothi_discord.utils.DiscordUtil;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
public class SoundBot extends Bot{
    private static final Logger log = MorphiaLoggerFactory.get(SoundBot.class);

    private String botToken;

    public SoundBot(String botToken) {
        this.botToken = botToken;
        initShards();
    }

    private void initShards() {
        new Thread(() -> {
            ///*
            try {
                numShards = DiscordUtil.getRecommendedShardCount(botToken);
            } catch (UnirestException e) {
                throw new RuntimeException("Unable to get recommended shard count!", e);
            }
            //*/

            for (int i = 0; i < numShards; i++) {
                DiscordSoundBotShard bot = new DiscordSoundBotShard(i, numShards, this);
                shards.add(bot);

                try {
                    Thread.sleep(SHARD_CREATION_SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Got interrupted while setting up bot shards!", e);
                }
            }
        }).start();
    }

    public void onInit(ReadyEvent readyEvent) {
        int ready = numShardsReady.incrementAndGet();

        for(Guild guild : readyEvent.getJDA().getGuilds()) {
            if (!guildCommandManagers.containsKey(guild.getId())) {
                guildCommandManagers.put(guild.getId(), new GuildCommandManager(guild));
            }

            GuildCommandManager gcm = guildCommandManagers.get(guild.getId());

            gcm.addCommand("addfile", new AddFileCommand());
            gcm.addCommand("request", new RequestsCommand());
            gcm.addAlias("requests", "request");
            gcm.addCommand("commands", new CommandsCommand());
            gcm.addCommand("delete", new DeleteCommand());
            gcm.addCommand("ping", new PingCommand());
        }

        if(ready == numShards) {
            log.info("All " + ready + " shards are ready.");

            for(BotShard bot : shards) {
                List<Guild> allGuilds = bot.getJDA().getGuilds();
                for(Guild guild : allGuilds) {
                    // guild.getAudioManager().openAudioConnection(guild.getVoiceChannelById("273812217445744641"));
                    //TODO should also happen on GuildJoin event!!!!

                }
            }
        }
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

    @Override
    public GuildAudioManager getGuildAudioPlayer(Guild guild) {
        String guildId = guild.getId();
        GuildSoundManager musicManager = (GuildSoundManager) allAudioManagers.get(guildId);

        if(musicManager == null) {
            musicManager = new GuildSoundManager(guild, getDiscordBotByJDA(guild.getJDA()).getPlayerManager(), this);
            allAudioManagers.put(guildId, musicManager);
            GuildData mongoGuilddata = GuildData.getGuildDataById(guildId);
            musicManager.getPlayer().setVolume(mongoGuilddata.getPlayerStartVolume());
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }


    @Override
    public String getToken() {
        return botToken;
    }

}
