package pothi_discord_music.utils.database.morphia.guilddatas;

import net.dv8tion.jda.core.entities.Message;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.Query;
import pothi_discord_music.Main;
import pothi_discord_music.handlers.GuildReceiveHandler;
import pothi_discord_music.managers.music.AutoPlaylist;
import pothi_discord_music.utils.audio.AudioUtils;
import pothi_discord_music.utils.audio.YoutubeMusicGenre;
import pothi_discord_music.utils.database.autoplaylists.MongoAutoplaylist;
import pothi_discord_music.utils.database.morphia.DataClass;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pascal Pothmann on 24.03.2017.
 */
@Entity(value = "guilddatas", noClassnameStored = true)
public class GuildData extends DataClass {
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
    private transient GuildReceiveHandler guildReceiveHandler;
    private transient AudioUtils audioUtils;
    private transient Message statusMessage;
    private transient AutoPlaylist defaultAutoplaylist;


    public static GuildData getGuildDataById(String guildId) {
        Query<GuildData> query = Main.datastore.createQuery(GuildData.class);
        GuildData result = query.field("_id").equal(guildId).get();

        if (result == null) {
            result = new GuildData();
            Main.datastore.save(result);
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
