package pothi_discord.rest;

import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by Pascal Pothmann on 26.01.2018.
 */
public class RestUtils {

    public static JSONObject getResquestBody(HttpServletRequest req) throws IOException {
        String bodyString = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        if (bodyString == null || bodyString.trim().equals("")) {
            return new JSONObject();
        }
        try {
            return new JSONObject(bodyString);
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
