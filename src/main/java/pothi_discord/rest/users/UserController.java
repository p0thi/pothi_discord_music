package pothi_discord.rest.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pothi_discord.rest.auth.AuthController;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.userdata.UserPlaylist;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Pascal Pothmann on 30.06.2017.
 */
@RestController
@CrossOrigin(origins = "*")
public class UserController {

    @RequestMapping(value = "/userplaylist", method = RequestMethod.GET)
    public Object userplaylists(@RequestParam(value = "id") String id,
                                @RequestParam Map<String, String> requestParams,
                                @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message", exceptionString).toString());
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        if(!userId.equals(id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message", "Not allowed").toString());
        }

        Userdata userdata = Userdata.getUserdata(userId);
        List<UserPlaylist> userPlaylists = userdata.getPlaylists();

        ObjectMapper mapper = new ObjectMapper();
        JSONArray result = new JSONArray();

        try {
            for (UserPlaylist userPlaylist : userPlaylists) {
                    JSONObject tmp = new JSONObject(mapper.writeValueAsString(userPlaylist));
                    tmp.put("id", userPlaylist.getId().toHexString());
                    tmp.remove("v");
                    tmp.put("active", userdata.getActivePlaylist().getId().equals(userPlaylist.getId()));

                    result.put(tmp);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(result.toString());

    }
}
