package pothi_discord_music.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class Param {
    private static JSONObject config;

    public static String BOT_TOKEN() {
        return config.getString("token");
    }
    public static String PREFIX() {
        return config.getString("prefix");
    }
    public static String OWNER_ID() {
        return config.getString("owner");
    }
    public static int MAX_PLAYLIST_SIZE = 20;
    public static long MAX_SONG_LENGTH = 720000;
    public static int PLAYER_START_VOLUME = 20;
    public static String GOOGLE_API_KEY() {
        return config.getString("google_key");
    }
    public static String MASHAPE_APP_KEY() {
        return config.getString("mashape_application_key");
    }
    public static String DATABASE_ROOT() {
        return config.getString("database_root");
    }
    public static String[] ADMINS() {
        JSONArray jsonAdmins = config.getJSONArray("admins");
        String[] result = new String[jsonAdmins.length()];

        for (int i = 0; i < jsonAdmins.length(); i++) {
            result[i] = jsonAdmins.getString(i);
        }
        return result;
    }
    public static ArrayList<String> ACCEPTED_GUILDS() {
        JSONArray jsonAdmins = config.getJSONArray("guilds");
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < jsonAdmins.length(); i++) {
            result.add(i, jsonAdmins.getString(i));
        }
        return result;
    }
    public static ArrayList<String> DEFAULT_CHANNELS() {
        JSONArray jsonAdmins = config.getJSONArray("default_channels");
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < jsonAdmins.length(); i++) {
            result.add(i, jsonAdmins.getString(i));
        }
        return result;
    }
    public static int SKIP_PERCENT = 20;
    public static final String BOT_COLOR_HEX = "#FF9900";



    public static <T extends Comparable> boolean isInList(T[] list, T t){
        return isInList(new ArrayList<T>(Arrays.asList(list)), t);
    }

    public static <T extends Comparable> boolean isInList(List<T> list, T t){
        if(list.size() <= 0)
            return false;
        for(T a:list)
            if(a.compareTo(t) == 0)
                return true;
        return false;

    }

    public static boolean isDeveloper(String id){
        boolean result = isInList(ADMINS(), id)
                || id.equals(OWNER_ID());
        return result;
    }

    public static void setConfig(JSONObject obj) {
        config = obj;
    }
}
