package pothi_discord.utils.database.morphia.guilddatas;

import net.dv8tion.jda.core.entities.Message;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.Query;
import pothi_discord.Main;
import pothi_discord.bots.music.handlers.MusicBotGuildReceiveHandler;
import pothi_discord.bots.music.managers.audio.AutoPlaylist;
import pothi_discord.utils.audio.AudioUtils;
import pothi_discord.utils.audio.YoutubeMusicGenre;
import pothi_discord.utils.database.autoplaylists.MongoAutoplaylist;
import pothi_discord.utils.database.morphia.DataClass;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@Entity(value = "guilddatas", noClassnameStored = true)
public class GuildData extends DataClass<String> {
    private Boolean useCustomAutoplaylist = false;
    private Boolean recording = false;
    private Boolean autoJoin = false;

    private Integer playerStartVolume = 20;
    private Integer audioCommandsStartVolume = 60;
    private Integer songSkipPercent = 30;

    @Embedded private Permissions permissions = new Permissions();

    private List<String> autoplaylist = new ArrayList<>();
    private List<String> bannedAudioCommandUsers = new ArrayList<>();

    @Embedded private List<SoundCommand> soundCommands = new ArrayList<>();
    @Embedded private List<SoundCommand> tmpSoundCommands = new ArrayList<>();


    private transient ArrayList<YoutubeMusicGenre> lastGenreSearch;
    private transient MusicBotGuildReceiveHandler musicBotGuildReceiveHandler;
    private transient AudioUtils audioUtils;
    private transient Message statusMessage;
    private transient AutoPlaylist defaultAutoplaylist;


    public static GuildData getGuildDataById(String guildId) {
        Query<GuildData> query = Main.datastore.createQuery(GuildData.class);
        GuildData result = query.field("_id").equal(guildId).get();

        if (result == null) {
            result = new GuildData();
            result.saveInstance();
        }

        result.setLastGenreSearch(new ArrayList<>());
        result.loadDefaultAutoplaylist();

        return result;
    }

    private void loadDefaultAutoplaylist() {
        if(getUseCustomAutoplaylist()) {
            defaultAutoplaylist = new AutoPlaylist(autoplaylist);
        }
        else {
            MongoAutoplaylist obj = MongoAutoplaylist.getObjectById("14f2af318304a83d5385df23450657d5");

            this.defaultAutoplaylist = new AutoPlaylist(obj.getContent(), null); //TODO pass in a valid source

        }
    }


    public Boolean getUseCustomAutoplaylist() {
        return useCustomAutoplaylist;
    }

    public void setUseCustomAutoplaylist(Boolean useCustomAutoplaylist) {
        this.useCustomAutoplaylist = useCustomAutoplaylist;
    }

    public Boolean getRecording() {
        return recording;
    }

    public void setRecording(Boolean recording) {
        this.recording = recording;
    }

    public Boolean getAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(Boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public Integer getPlayerStartVolume() {
        return playerStartVolume;
    }

    public void setPlayerStartVolume(Integer playerStartVolume) {
        this.playerStartVolume = playerStartVolume;
    }

    public Integer getAudioCommandsStartVolume() {
        return audioCommandsStartVolume;
    }

    public void setAudioCommandsStartVolume(Integer audioCommandsStartVolume) {
        this.audioCommandsStartVolume = audioCommandsStartVolume;
    }

    public Integer getSongSkipPercent() {
        return songSkipPercent;
    }

    public void setSongSkipPercent(Integer songSkipPercent) {
        this.songSkipPercent = songSkipPercent;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    public List<String> getAutoplaylist() {
        return autoplaylist;
    }

    public void setAutoplaylist(List<String> autoplaylist) {
        this.autoplaylist = autoplaylist;
    }

    public List<String> getBannedAudioCommandUsers() {
        return bannedAudioCommandUsers;
    }

    public void setBannedAudioCommandUsers(List<String> bannedAudioCommandUsers) {
        this.bannedAudioCommandUsers = bannedAudioCommandUsers;
    }

    public List<SoundCommand> getSoundCommands() {
        return soundCommands;
    }

    public void setSoundCommands(List<SoundCommand> soundCommands) {
        this.soundCommands = soundCommands;
    }

    public List<SoundCommand> getTmpSoundCommands() {
        return tmpSoundCommands;
    }

    public void setTmpSoundCommands(List<SoundCommand> tmpSoundCommands) {
        this.tmpSoundCommands = tmpSoundCommands;
    }


    // Not versioned

    public ArrayList<YoutubeMusicGenre> getLastGenreSearch() {
        return lastGenreSearch;
    }

    public void setLastGenreSearch(ArrayList<YoutubeMusicGenre> lastGenreSearch) {
        this.lastGenreSearch = lastGenreSearch;
    }

    public MusicBotGuildReceiveHandler getMusicBotGuildReceiveHandler() {
        return musicBotGuildReceiveHandler;
    }

    public void setMusicBotGuildReceiveHandler(MusicBotGuildReceiveHandler musicBotGuildReceiveHandler) {
        this.musicBotGuildReceiveHandler = musicBotGuildReceiveHandler;
    }

    public AudioUtils getAudioUtils() {
        return audioUtils;
    }

    public void setAudioUtils(AudioUtils audioUtils) {
        this.audioUtils = audioUtils;
    }

    public Message getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(Message statusMessage) {
        this.statusMessage = statusMessage;
    }

    public AutoPlaylist getDefaultAutoplaylist() {
        return defaultAutoplaylist;
    }

    public void setDefaultAutoplaylist(AutoPlaylist defaultAutoplaylist) {
        this.defaultAutoplaylist = defaultAutoplaylist;
    }
}
