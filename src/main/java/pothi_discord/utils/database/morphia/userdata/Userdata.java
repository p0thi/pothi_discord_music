package pothi_discord.utils.database.morphia.userdata;

import net.dv8tion.jda.core.entities.Game;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.query.Query;
import pothi_discord.Main;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.morphia.DataClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
@Entity(value = "userdata", noClassnameStored = true)
public class Userdata extends DataClass<String> {
    private static transient HashMap<String, Userdata> allUserdatas = new HashMap<>();

    @Reference(lazy = true)
    private List<Gametime> gametime = new ArrayList<>();
    private Long lastGameUpdate = System.currentTimeMillis();
    private String currentGame = null;



    public static Userdata getUserdata(String id) {
        Query<Userdata> query = Main.datastore.createQuery(Userdata.class);
        Userdata result = query.field("_id").equal(id).get();

        if (result == null) {
            result = new Userdata();
            result.setId(id);
            Main.datastore.save(result);
        }
        if (!allUserdatas.containsKey(id)) {
            allUserdatas.put(id, result);
        }

        return result;
    }

    public void storeGame(Game game) {
        storeGame(game, false);
    }

    public void storeGame(Game game, boolean skipCheck) {

        if (!skipCheck && allUserdatas.containsKey(getId())) {
            if (allUserdatas.get(getId()).getCurrentGame().equals(game.getName())) {
                return;
            }
        }

        System.out.println("Storing game of user. " + getId());

        if (currentGame != null) {

            Gametime myGametime = null;
            for (Gametime gametime : gametime) {
                if (gametime.getGameName().equals(currentGame)) {
                    myGametime = gametime;
                    break;
                }
            }

            if (myGametime == null) {
                myGametime = new Gametime();
                myGametime.setGameName(currentGame);
                myGametime.setTimePairs(new ArrayList<>());
                gametime.add(myGametime);
            }

            String currentDate = TextUtils.millisToDate(System.currentTimeMillis(), "-");
            TimePair myTimepair = null;
            for (TimePair timePair : myGametime.getTimePairs()) {
                if (timePair.getDate().equals(currentDate)) {
                    myTimepair = timePair;
                    break;
                }
            }

            if (myTimepair == null) {
                myTimepair = new TimePair();
                myTimepair.setDate(currentDate);
                myTimepair.setDuration( (long) 0);
                myGametime.getTimePairs().add(myTimepair);
            }

            myTimepair.setDuration(myTimepair.getDuration() + (System.currentTimeMillis() - lastGameUpdate));
            Main.datastore.save(myGametime);
        }

        currentGame = game == null ? null : game.getName();
        lastGameUpdate = System.currentTimeMillis();
        Main.datastore.save(this);
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
}
