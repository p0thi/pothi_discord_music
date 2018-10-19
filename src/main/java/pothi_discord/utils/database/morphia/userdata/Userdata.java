package pothi_discord.utils.database.morphia.userdata;

import net.dv8tion.jda.core.entities.Game;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.query.Query;
import pothi_discord.Main;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.morphia.DataClass;

import java.util.*;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
@Entity(value = "userdata", noClassnameStored = true)
public class Userdata extends DataClass<String> {
    private static transient HashMap<String, Userdata> allUserdatas = new HashMap<>();

    @Reference(lazy = true)
    private List<Gametime> gametime = new ArrayList<>();

    @Reference(lazy = true)
    private List<UserPlaylist> playlists = new ArrayList<>();

    @Reference(lazy = true)
    private UserPlaylist activePlaylist = null;

    private Long lastGameUpdate = System.currentTimeMillis();
    private String currentGame = null;

    private String joinCommand = null;

    private String access_token = null;
    private String refresh_token = null;
    private Date token_exp = null;




    public static Userdata getUserdata(String id) {
        Query<Userdata> query = Main.datastore.createQuery(Userdata.class);
        Userdata result = query.field("_id").equal(id).get();

        if (result == null) {
            result = new Userdata();
            result.setId(id);
            result.saveInstance();
        }
        if (!allUserdatas.containsKey(id)) {
            allUserdatas.put(id, result);
        }

        return result;
    }

    public UserPlaylist getActivePlaylist() {
        return activePlaylist;
    }

    public void setActivePlaylist(UserPlaylist activePlaylist) {
        this.activePlaylist = activePlaylist;
    }

    public List<Gametime> getGametime() {
        return gametime;
    }

    public void setGametime(List<Gametime> gametime) {
        this.gametime = gametime;
    }

    public Long getLastGameUpdate() {
        return lastGameUpdate;
    }

    public void setLastGameUpdate(Long lastGameUpdate) {
        this.lastGameUpdate = lastGameUpdate;
    }

    public String getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }

    public List<UserPlaylist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<UserPlaylist> playlists) {
        this.playlists = playlists;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public Date getToken_exp() {
        return token_exp;
    }

    public void setToken_exp(Date token_exp) {
        this.token_exp = token_exp;
    }

    public String getJoinCommand() {
        return joinCommand;
    }

    public void setJoinCommand(String joinCommand) {
        this.joinCommand = joinCommand;
    }
}
