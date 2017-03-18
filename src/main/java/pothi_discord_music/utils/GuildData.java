package pothi_discord_music.utils;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord_music.handlers.GuildReceiveHandler;
import pothi_discord_music.managers.music.AutoPlaylist;
import pothi_discord_music.utils.audio.AudioUtils;
import pothi_discord_music.utils.audio.YoutubeMusicGenre;
import pothi_discord_music.utils.couch_db.autoplaylists.AutoplaylistDBObject;
import pothi_discord_music.utils.couch_db.guilddata.GuildDBObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public class GuildData {
    private static final Logger log = LoggerFactory.getLogger(GuildData.class);
    public static final HashMap<String, GuildData> ALL_GUILD_DATAS = new HashMap<>();

    // Final Variables
    public final Guild GUILD;

    private AutoPlaylist defaultAutoplaylist;



    // Instance Variables
    private ArrayList<YoutubeMusicGenre> lastGenreSearch;

    private GuildReceiveHandler guildReceiveHandler;
    private AudioUtils audioUtils;

    private Message statusMessage;

    /*
    CONSTRUCTORS
     */
    public GuildData(Guild guild) {
        ALL_GUILD_DATAS.put(guild.getId(), this);
        this.GUILD = guild;
        lastGenreSearch = new ArrayList<>();
        loadDefaultAutoplaylist();
    }

    /*
    METHODS
     */

    public GuildDBObject getGuildDBObject() {
        return GuildDBObject.getObjectById(GUILD.getId());
    }

    public void destroy() {
        ALL_GUILD_DATAS.remove(GUILD.getId());
    }


    private void loadDefaultAutoplaylist() {
        if(getGuildDBObject().isUseCustomAutoplaylist()) {
            this.defaultAutoplaylist = new AutoPlaylist(this.getGuildDBObject().getAutoplaylist());
            log.info( GUILD.getName() + " Custom Playlist loaded with " + this.defaultAutoplaylist.size() + " entries.");
        }
        else {
            AutoplaylistDBObject obj = AutoplaylistDBObject.getObjectById("14f2af318304a83d5385df23450657d5");

            this.defaultAutoplaylist = new AutoPlaylist(obj.getContent(), null); //TODO pass in a valid source

            log.info(GUILD.getName() + ": Default Playlist loaded with " + this.defaultAutoplaylist.size() + " entries.");
        }
    }


    /*
    GETTER & SETTER
     */

    public Message getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(Message statusMessage) {
        this.statusMessage = statusMessage;
    }

    public GuildReceiveHandler getGuildReceiveHandler() {
        return guildReceiveHandler;
    }

    public void setGuildReceiveHandler(GuildReceiveHandler guildReceiveHandler) {
        this.guildReceiveHandler = guildReceiveHandler;
    }

    public AudioUtils getAudioUtils() {
        return audioUtils;
    }

    public void setAudioUtils(AudioUtils audioUtils) {
        this.audioUtils = audioUtils;
    }

    public ArrayList<YoutubeMusicGenre> getLastGenreSearch() {
        return lastGenreSearch;
    }

    public void setLastGenreSearch(ArrayList<YoutubeMusicGenre> lastGenreSearch) {
        this.lastGenreSearch = lastGenreSearch;
    }

    public AutoPlaylist getDefaultAutoplaylist() {
        return defaultAutoplaylist;
    }
}
