package pothi_discord.utils.database.morphia.guilddatas;

import net.dv8tion.jda.core.entities.Message;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import pothi_discord.Main;
import pothi_discord.bots.music.handlers.MusicBotGuildReceiveHandler;
import pothi_discord.utils.audio.AudioUtils;
import pothi_discord.utils.audio.YoutubeMusicGenre;
import pothi_discord.utils.database.morphia.DataClass;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@Entity(value = "guilddatas", noClassnameStored = true)
public class GuildData extends DataClass<ObjectId> {

    private String guildId;

    private Boolean useCustomAutoplaylist = false;
    private Boolean recording = false;
    private Boolean autoJoin = false;

    private Integer playerStartVolume = 20;
    private Integer audioCommandsStartVolume = 60;
    private Integer songSkipPercent = 30;

    @Embedded
    private Permissions permissions = new Permissions();

    // TODO !!!!!!!!!!!!!!!! lazy was not possible
    @Reference(lazy = true)
    private MongoAutoPlaylist autoplaylist = new MongoAutoPlaylist();
    private List<String> bannedAudioCommandUsers = new ArrayList<>();

    @Embedded
    private List<SoundCommand> soundCommands = new ArrayList<>();
    @Embedded
    private List<SoundCommand> tmpSoundCommands = new ArrayList<>();

    private transient ArrayList<YoutubeMusicGenre> lastGenreSearch;
    private transient MusicBotGuildReceiveHandler musicBotGuildReceiveHandler;
    private transient AudioUtils audioUtils;
    private transient Message statusMessage;
    private transient MongoAutoPlaylist defaultAutoplaylist;


    public static GuildData getGuildDataByGuildId(String guildId) {

        GuildData result = Main.datastore.find(GuildData.class).field("guildId").equal(guildId).get();

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
            defaultAutoplaylist = getAutoplaylist();
        }
        else {
            MongoAutoPlaylist obj = MongoAutoPlaylist.getObjectById("58eb51a31cff3028dc240b20");

            this.defaultAutoplaylist = obj; //TODO pass in a valid source

        }
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
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

    public MongoAutoPlaylist getAutoplaylist() {
        return autoplaylist;
    }

    public void setAutoplaylist(MongoAutoPlaylist autoplaylist) {
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

    @Transient
    public ArrayList<YoutubeMusicGenre> getLastGenreSearch() {
        return lastGenreSearch;
    }

    @Transient
    public void setLastGenreSearch(ArrayList<YoutubeMusicGenre> lastGenreSearch) {
        this.lastGenreSearch = lastGenreSearch;
    }

    @Transient
    public MusicBotGuildReceiveHandler getMusicBotGuildReceiveHandler() {
        return musicBotGuildReceiveHandler;
    }

    @Transient
    public void setMusicBotGuildReceiveHandler(MusicBotGuildReceiveHandler musicBotGuildReceiveHandler) {
        this.musicBotGuildReceiveHandler = musicBotGuildReceiveHandler;
    }

    @Transient
    public AudioUtils getAudioUtils() {
        return audioUtils;
    }

    @Transient
    public void setAudioUtils(AudioUtils audioUtils) {
        this.audioUtils = audioUtils;
    }

    @Transient
    public Message getStatusMessage() {
        return statusMessage;
    }

    @Transient
    public void setStatusMessage(Message statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Transient
    public MongoAutoPlaylist getDefaultAutoplaylist() {
        return defaultAutoplaylist;
    }

    @Transient
    public void setDefaultAutoplaylist(MongoAutoPlaylist defaultAutoplaylist) {
        this.defaultAutoplaylist = defaultAutoplaylist;
    }
}
