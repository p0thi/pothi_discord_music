package pothi_discord.bots;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.bots.music.DiscordMusicBotShard;
import pothi_discord.bots.music.commands.audio.*;
import pothi_discord.bots.music.commands.controll.*;
import pothi_discord.bots.music.commands.fun.*;
import pothi_discord.bots.music.handlers.MusicBotGuildReceiveHandler;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;
import pothi_discord.commands.*;
import pothi_discord.commands.PingCommand;
import pothi_discord.managers.GuildAudioManager;
import pothi_discord.managers.GuildCommandManager;
import pothi_discord.utils.DiscordUtil;
import pothi_discord.utils.audio.AudioUtils;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
public class MusicBot extends Bot{
    private static final Logger log = MorphiaLoggerFactory.get(MusicBot.class);

    private String botToken;



    public MusicBot(String botToken) {
        this.botToken = botToken;
        initShards();
    }


    protected void initShards() {
        new Thread(() -> {

            ///*
            try {
                numShards = DiscordUtil.getRecommendedShardCount(getToken());
            } catch (UnirestException e) {
                throw new RuntimeException("Unable to get recommended shard count!", e);
            }
            //*/

            for (int i = 0; i < numShards; i++) {
                DiscordMusicBotShard bot = new DiscordMusicBotShard(i, numShards, this);
                shards.add(bot);

                try {
                    Thread.sleep(SHARD_CREATION_SLEEP_INTERVAL);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Got interrupted while setting up bot shards!", e);
                }
            }
        }).start();
    }

    public  void onInit(ReadyEvent readyEvent) {
        int ready = numShardsReady.incrementAndGet();

        for (User user : readyEvent.getJDA().getUsers()) {
            ((DiscordMusicBotShard) getDiscordBotByJDA(readyEvent.getJDA())).getAllBotUsers().put(user.getId(), user);
        }

        for(Guild guild : readyEvent.getJDA().getGuilds()) {
            if (!guildCommandManagers.containsKey(guild.getId())) {
                guildCommandManagers.put(guild.getId(), new GuildCommandManager(guild));
            }

            GuildCommandManager gcm = guildCommandManagers.get(guild.getId());

            //Commands TODO: special commands per guild
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
            gcm.addCommand("echo", new EchoCommand());
            gcm.addCommand("clear", new ClearCommand());



            gcm.addCommand("react", new ReactCommand());
            gcm.addAlias("reaction", "react");
            gcm.addCommand("playlist", new PlaylistCommand());
            gcm.addAlias("playlists", "playlist");
            gcm.addCommand("setting", new SettingsCommand());
            gcm.addAlias("settings", "setting");
            gcm.addCommand("play", new PlayCommand());
            gcm.addAlias(">", "play");
            gcm.addCommand("repeat", new RepeatCommand());
            gcm.addAlias("<", "repeat");
            gcm.addCommand("pause", new PauseCommand());
            gcm.addAlias("||", "pause");
            gcm.addAlias("II", "pause");
            gcm.addAlias("ll", "pause");
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

            for(BotShard bot : shards) {
                List<Guild> allGuilds = bot.getJDA().getGuilds();
                log.info("Connecting to " + allGuilds.size() + " guilds...");
                for(Guild guild : allGuilds) {

                    //TODO should also happen on GuildJoin event!!!!
                    GuildData guildData = GuildData.getGuildDataByGuildId(guild.getId());

                    MusicBotGuildReceiveHandler grh = new MusicBotGuildReceiveHandler(guild);
                    guildData.setMusicBotGuildReceiveHandler(grh);

                    guildData.setAudioUtils(new AudioUtils());
                    try {
                        GuildMusicManager musicManager = (GuildMusicManager)getGuildAudioPlayer(guild);
                        musicManager.connectAndPlay();
                        log.info("Joining guild. (" + guild.getName() + ")");
                    } catch (InterruptedException e){
                        log.warning("could not autojoin guild: " + guild.getName());
                    }
                }
            }
        }

    }


    @Override
    public GuildAudioManager getGuildAudioPlayer(Guild guild) {
        String guildId = guild.getId();
        GuildMusicManager musicManager = (GuildMusicManager) allAudioManagers.get(guildId);

        if(musicManager == null) {
            musicManager = new GuildMusicManager(guild, getDiscordBotByJDA(guild.getJDA()).getPlayerManager(), this);
            allAudioManagers.put(guildId, musicManager);
            GuildData mongoGuilddata = GuildData.getGuildDataByGuildId(guildId);
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
