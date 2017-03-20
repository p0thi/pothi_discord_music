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
    public static String GOOGLE_API_KEY() {
        return config.getString("google_key");
    }
    public static String MASHAPE_APP_KEY() {
        return config.getString("mashape_application_key");
    }
    public static String MONGO_USER() { return config.getString("mongo_user"); }
    public static String MONGO_PW() { return config.getString("mongo_pw"); }
    public static ArrayList<String> ADMINS() {
        return getListFromKey("admins");
    }
    public static ArrayList<String> ACCEPTED_GUILDS() {
        return getListFromKey("guilds");
    }
    public static ArrayList<String> DEFAULT_CHANNELS() {
        return getListFromKey("default_channels");
    }
    public static String MONGO_ROOT() {
        return config.getString("mongo_root");
    }



    public static int SKIP_PERCENT = 20;
    public static final String BOT_COLOR_HEX = "#FF9900";


    private static ArrayList<String> getListFromKey(String key) {
        JSONArray jsonAdmins = config.getJSONArray(key);
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < jsonAdmins.length(); i++) {
            result.add(i, jsonAdmins.getString(i));
        }
        return result;
    }


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
