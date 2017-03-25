package pothi_discord_music.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Pascal Pothmann on 25.01.2017.
 */
public class TextUtils {
    private static final Logger log = MorphiaLoggerFactory.get(TextUtils.class);

    public static long parseISO8601DurationToMillis(String duration) {
        Duration d = Duration.parse(duration);
        return d.get(ChronoUnit.SECONDS) * 1000;
    }

    public static void applyConfigFile(String path) throws IOException {
        File configFile = new File(path);
        ArrayList<String> lines = readTxt(configFile);
        StringBuilder b = new StringBuilder();

        for (String line : lines) {
            b.append(line);
        }
        Param.setConfig(new JSONObject(b.toString()));

        /*
        ArrayList<String[]> sortedList = seperateList(path);
        for(String[] a:sortedList  ){
            ArrayList<String> tmp1 = new ArrayList<>();
            for(int i=0;i<a.length;i++){
                if(a[i]!=null)tmp1.add(a[i]);
            }
            String[] tmp2 = new String[tmp1.size()];
            for(int i=0;i<tmp2.length;i++){
                tmp2[i]=tmp1.get(i);
            }
            a=tmp2;
        }

        for(int i = 0; i<sortedList.size(); i++){
            switch(sortedList.get(i)[0]){
                case "token":
                    Param.BOT_TOKEN=sortedList.get(i)[1];
                    break;

                case "owner":
                    Param.OWNER_ID=sortedList.get(i)[1];
                    break;

                case "prefix":
                    Param.PREFIX=sortedList.get(i)[1];
                    break;

                case "admins":
                    String[] admins = new String[sortedList.get(i).length-1];
                    for(int a=1;a<sortedList.get(i).length;a++){
                        admins[a-1]=sortedList.get(i)[a];
                    }
                    Param.ADMINS = admins;
                    break;

                case "guilds":
                    Param.ACCEPTED_GUILDS = new ArrayList<>();
                    for (int x=1;x<sortedList.get(i).length;x++)
                        Param.ACCEPTED_GUILDS.add(sortedList.get(i)[x]);
                    break;
                case "google_key":
                    Param.GOOGLE_API_KEY = sortedList.get(i)[1];
                    break;
                case "default_channels":
                    Param.DEFAULT_CHANNELS = new ArrayList<>();
                    for (int x = 1; x < sortedList.get(i).length; x++)
                        Param.DEFAULT_CHANNELS.add(sortedList.get(i)[x]);
                    break;
                case "mashape_application_key":
                    Param.MASHAPE_APP_KEY = sortedList.get(i)[1];
                    break;
                case "database_root":
                    Param.DATABASE_ROOT = sortedList.get(i)[1];
                case "":
                case " ":
                    break;
                default:
                    log.warn("Teile des Config Files konnten nicht gelesen werden! ("
                            + sortedList.get(i)[0] + ")");

            }
        }
        */
    }

    public static void saveCommands(){
        // TODO
    }

    public static void saveTmpCommands(){
        // TODO
    }

    public static String formatMillis(long millis, boolean shoeDays){
        if(!shoeDays){
            return formatMillis(millis);
        }

        int days = (int) ((millis / 1000) / 86400);
        int hours = (int) ((millis / 1000) / 3600) % 24;
        int minutes = (int) (((millis / 1000) / 60) % 60);
        int seconds = (int) ((millis / 1000) % 60);

        return String.format("%01d:%02d:%02d:%02d", days, hours, minutes, seconds);
    }
    public static String formatMillis(long millis){
        int hours = (int) ((millis / 1000) / 3600);
        int minutes = (int) (((millis / 1000) / 60) % 60);
        int seconds = (int) ((millis / 1000) % 60);
        return String.format("%01d:%02d:%02d", hours, minutes, seconds);
    }

    public static String progressBar(long current, long total, int length) {
        String bar = "";
        if(length <= 0) {
            return bar;
        }
        double percent = (double)current / (double)total;

        for(int i = 0; i <= length; i++) {
            if(percent < ((1 / (double)length) * i)) {
                bar += "□";
            }
            else {
                bar += "■";
            }
        }

        return bar;
    }


    public static void saveCommandsGeneric(HashMap<String, ArrayList<String[]>> map, String path){
        ArrayList<String> tmp = new ArrayList();

        Iterator it = map.entrySet().iterator();
        // Iterates through HashMap (Key: GuildID)
        while (it.hasNext()) {
            Map.Entry<String, ArrayList<String[]>> pair = (Map.Entry) it.next();
            for(String[] a : pair.getValue()){
                String joinedString = String.join(" ", a);
                if(!someStartsWith(joinedString + "=", tmp))
                    tmp.add(joinedString + "=");
                int index = 0;
                boolean found = false;
                while(index<tmp.size()){
                    if(tmp.get(index).startsWith(joinedString)) {
                        found = true;
                        break;
                    }
                    index++;
                }
                if(found) {
                    String newTmp = tmp.get(index);
                    newTmp += pair.getKey() + " ";
                    tmp.set(index, newTmp);
                }
            }
        }
        try {
            TextUtils.writeTxt(tmp, "", path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean someStartsWith(String prefix, List<String> list){
        for(String a:list)
            if (a.startsWith(prefix))
                return true;
        return false;
    }

    public static ArrayList<String[]> seperateList(String path) throws IOException {
        ArrayList<String> myList = readTxt(path);
        ArrayList<String[]> tmp = new ArrayList<>();
        for(String a:myList){
            String[] line = a.split("[= ]");
            if(line.length>=2)tmp.add(line);
        }
        tmp.removeAll(Collections.singleton(null));
        return tmp;
    }

    public static void saveConfigFile(String path) throws IOException {
        /* TODO
        ArrayList<String> newFile = new ArrayList();

        String admins = "";
        for(String a:Param.ADMINS)
            admins = admins + a + " ";
        String guilds = "";
        for(String a:Param.ACCEPTED_GUILDS)
            guilds += a;
        newFile.add("token="+Param.DISCORD_TOKEN);
        newFile.add("owner="+Param.OWNER_ID);
        newFile.add("prefix="+Param.PREFIX);
        newFile.add("admins="+admins.trim());
        newFile.add("admins="+guilds.trim());

        writeTxt(newFile, "", path);
        */
    }

    public static void applyBannedFile(String path) throws IOException {
        /* TODO
        ArrayList<String> list = readTxt(path);
        for(String a:list){
            char[] tmp = a.toCharArray();
            a="";
            for(char b:tmp){
                if(b!=' ')a += b;
            }
        }
        Param.BANNED=list;
        */
    }

    public static String readFirstFileLine(File file) throws IOException {
        String result = null;
        try {
            result = readTxt(file).get(0);
        } catch (IndexOutOfBoundsException e){

        }
        return result;
    }

    public static ArrayList<String> readTxt(File file) throws IOException{
        return readTxt(file.getAbsolutePath());
    }

    public static ArrayList<String> readTxt(String path) throws IOException{
        ArrayList<String> tmp = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;

        while((line = br.readLine()) != null){
            tmp.add(line);
        }
        br.close();
        return tmp;
    }

    public static synchronized void writeTxt(String content, String filename, String path) throws FileNotFoundException {
        ArrayList<String> tmp = new ArrayList<>();
        tmp.add(content);
        writeTxt(tmp, filename, path);
    }

    //Schreibt eine Datei aus einer ArrayList aus Strings
    public static synchronized void writeTxt(ArrayList<String> content, String fileName, String path) throws FileNotFoundException{
        PrintWriter outputFile = new PrintWriter(path+fileName);
        for(String a:content){
            outputFile.println(a);
        }
        outputFile.close();
    }

    public static HashMap<String, ArrayList<String[]>> handleCommandFile(String path) throws IOException {
        HashMap<String, ArrayList<String[]>> result = new HashMap<>();
        ArrayList<String> txtLines = readTxt(path);

        //Split at "="
        ArrayList<String[]> firstSplit = new ArrayList<>();
        for(String line:txtLines)
            firstSplit.add(line.split("="));

        for(int i = 0; i<txtLines.size();i++){
            for(String guildID:firstSplit.get(i)[1].trim().split(" ")) {
                if (!result.containsKey(guildID)) {
                    ArrayList<String[]> tmp = new ArrayList<>();
                    tmp.add(firstSplit.get(i)[0].trim().split(" "));
                    result.put(guildID, tmp);
                }
                else {
                    result.get(guildID).add(firstSplit.get(i)[0].trim().split(" "));
                }
            }
        }
        return result;
    }

    public static String replaceUmlauts(String input){

        //replace all lower Umlauts
        String output = input.replace("ü", "ue")
                .replace("ö", "oe")
                .replace("ä", "ae")
                .replace("ß", "ss");

        //first replace all capital umlaute in a non-capitalized context (e.g. Übung)
        output = output.replace("Ü(?=[a-zäöüß ])", "Ue")
                .replace("Ö(?=[a-zäöüß ])", "Oe")
                .replace("Ä(?=[a-zäöüß ])", "Ae");

        //now replace all the other capital umlaute
        output = output.replace("Ü", "UE")
                .replace("Ö", "OE")
                .replace("Ä", "AE");

        return output;
    }

    public static String joinStringList(List<String> list, String spacer){
        String result = "";
        for(String a : list){
            result += a + spacer;
        }
        return result.substring(0, result.length()-spacer.length());
    }

    public static String getStringFromURL(String urlString) throws IOException {
        String content = "";

        URL url = new URL(TextUtils.replaceUmlauts(urlString));
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

        String input;
        while (null != (input = br.readLine())){
            content += input;
        }

        return content;
    }


    public static String substringPreserveWords(String desc, int i) {
        String result = "";
        for(int a = 0; a < Math.min(desc.length(), i); a++) {
            result += desc.charAt(a);
        }
        return result;
    }

    public static String millisToDate(long millis, String infix){
        DateFormat formatter = new SimpleDateFormat("dd" + infix + "MM" + infix + "yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return (formatter.format(calendar.getTime()));
    }

    public static long getCurrentTimeMillisMinus(int x){
        //System.err.println(x*1000);
        return System.currentTimeMillis()-x*1000;
    }

    public static String getMillisFormatted(long millis){
        int h = (int) ((millis / 1000) / 3600);
        int m = (int) (((millis / 1000) / 60) % 60);
        int s = (int) ((millis / 1000) % 60);

        return h + " Std, " + m + " Min";
    }

    public static long daysBetwenDates(String inputString1, String inputString2, String infix) {
        SimpleDateFormat myFormat = new SimpleDateFormat("dd"+ infix + "MM" + infix + "yyyy");

        Date date1 = null;
        Date date2 = null;
        try {
            date1 = myFormat.parse(inputString1);
            date2 = myFormat.parse(inputString2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = date2.getTime() - date1.getTime();
        return (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

    }

    public static String dateToDayName(int d, int m, int y){
        int y0 = y -(14 - m )/12;
        int x = y0 - y0 /4 - y0 /100+ y0 /400;
        int m0 = m +12*((14 - m )/12) -2;
        int d0 = ( d + x +(31* m0 )/12)%7;
        switch (d0){
            case 0 : return "Sonntag";
            case 1 : return "Montag";
            case 2 : return "Dienstag";
            case 3 : return "Mittwoch";
            case 4 : return "Donnerstag";
            case 5 : return "Freitag";
            case 6 : return "Samstag";
        }
        return "*Fehler!*";
    }

    public static String[] reverseDate(String date, String currentDevider){
        String[] tmp = date.split(currentDevider);
        ArrayUtils.reverse(tmp);
        return tmp;
    }
}
