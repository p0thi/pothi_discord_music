package pothi_discord_music.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class Param {

    public static String BOT_TOKEN;
    public static String PREFIX;
    public static String OWNER_ID;
    public static int MAX_PLAYLIST_SIZE = 20;
    public static long MAX_SONG_LENGTH = 720000;
    public static int PLAYER_START_VOLUME = 20;
    public static String GOOGLE_API_KEY;
    public static String MASHAPE_APP_KEY;
    public static String DATABASE_ROOT;
    public static String[] ADMINS;
    public static ArrayList<String> ACCEPTED_GUILDS;
    public static ArrayList<String> DEFAULT_CHANNELS;
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
        boolean result = isInList(ADMINS, id)
                || id.equals(OWNER_ID);
        return result;
    }

}
