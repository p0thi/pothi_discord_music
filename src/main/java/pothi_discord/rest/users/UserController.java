package pothi_discord.rest.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pothi_discord.bots.music.commands.audio.PlaylistCommand;
import pothi_discord.rest.auth.AuthController;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.MongoAudioTrack;
import pothi_discord.utils.database.morphia.userdata.UserPlaylist;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

/**
 * Created by Pascal Pothmann on 30.06.2017.
 */
@RestController
@CrossOrigin
public class UserController {

    public static class UserPlaylistHandler extends HttpServlet {
        public static final String PATH = "/userplaylist";
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String exceptionString = AuthController.getAuthorizationErrorString(req);

            if(exceptionString != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(new JSONObject().put("message", exceptionString).toString());
                return;
            }

            String token = AuthController.getToken(req);
            String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

            Userdata userdata = Userdata.getUserdata(userId);
            if (userdata == null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print("[]");
                return;
            }

            List<UserPlaylist> userPlaylists = userdata.getPlaylists();
            if (userPlaylists == null || userPlaylists.size() == 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print("[]");
                return;
            }

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
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("Internal error.");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(result.toString());
            return;
        }
    }


    @RequestMapping(value = "/userplaylist", method = RequestMethod.GET)
    public Callable<ResponseEntity> getUserplaylists(@RequestParam(value = "id") String id,
                                     @RequestParam Map<String, String> requestParams,
                                     @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message", exceptionString).toString());
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        Userdata userdata = Userdata.getUserdata(userId);
        if (userdata == null) {
            return () -> ResponseEntity.ok("[]");
        }

        List<UserPlaylist> userPlaylists = userdata.getPlaylists();
        if (userPlaylists == null || userPlaylists.size() == 0) {
            return () -> ResponseEntity.ok("[]");
        }

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
            return () -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
        }

        return () -> ResponseEntity.ok(result.toString());
    }

    public static class UserPlaylistTrackAddHandler extends HttpServlet {
        public static final String PATH = "/userplaylist/track/add";
        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String exceptionString = AuthController.getAuthorizationErrorString(req);

            if (exceptionString != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(new JSONObject().put("message", exceptionString).toString());
                return;
            }

            String token = AuthController.getToken(req);
            String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

            Userdata userdata = Userdata.getUserdata(userId);
            if (userdata == null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print("[]");
                return;
            }

            if (req.getParameter("playlistId") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing playlistId query string.");
                return;
            }

            String playlistId = req.getParameter("playlistId");

            List<UserPlaylist> userPlaylists = userdata.getPlaylists();
            UserPlaylist myPlaylist = null;
            for (UserPlaylist userPlaylist : userPlaylists) {
                if (userPlaylist.getId().toHexString().equals(playlistId)) {
                    myPlaylist = userPlaylist;
                    break;
                }
            }
            if (myPlaylist == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("No playlist with this id.");
                return;
            }

            if (req.getParameter("identifier") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing identifier query string.");
                return;
            }

            String identifier = req.getParameter("identifier");

            if (myPlaylist.containsIdentifier(identifier)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Identifier already in this list.");
                return;
            }

            MongoAudioTrack mongoAudioTrack = MongoAudioTrack.getTrackFromIdentifier(identifier);

            if (mongoAudioTrack == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Could not handle this identifier.");
                return;
            }

            myPlaylist.getTracks().add(mongoAudioTrack);
            if(myPlaylist.saveInstance() == null) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("Server error. Please try again.");
                return;
            }
            else {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(new JSONObject().put("message", "New Entry was saved").toString());
                return;
            }
        }
    }


    @RequestMapping(value = "/userplaylist/track/add", method = RequestMethod.PUT)
    public Callable<ResponseEntity> addUserplaylistsEntry(@RequestParam(value = "id") String playlistId,
                                        @RequestParam(value = "identifier") String identifier,
                                @RequestParam Map<String, String> requestParams,
                                @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if (exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message", exceptionString).toString());
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        Userdata userdata = Userdata.getUserdata(userId);
        if (userdata == null) {
            return () -> ResponseEntity.ok("[]");
        }

        List<UserPlaylist> userPlaylists = userdata.getPlaylists();
        UserPlaylist myPlaylist = null;
        for (UserPlaylist userPlaylist : userPlaylists) {
            if (userPlaylist.getId().toHexString().equals(playlistId)) {
                myPlaylist = userPlaylist;
                break;
            }
        }
        if (myPlaylist == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No playlist with this id");
        }

        if (myPlaylist.containsIdentifier(identifier)) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Identifier already in this list");
        }

        MongoAudioTrack mongoAudioTrack = MongoAudioTrack.getTrackFromIdentifier(identifier);

        if (mongoAudioTrack == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not handle this identifier");
        }

        myPlaylist.getTracks().add(mongoAudioTrack);
        if(myPlaylist.saveInstance() == null) {
            return () -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error. Please try again");
        }
        else {
            return () -> ResponseEntity.ok(new JSONObject().put("message", "New Entry was saved").toString());
        }
    }

    public static class UserPlaylistTrackDeleteHandler extends HttpServlet {
        public static final String PATH = "/userplaylist/track/delete";
        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String exceptionString = AuthController.getAuthorizationErrorString(req);

            if (exceptionString != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(new JSONObject().put("message", exceptionString).toString());
                return;
            }

            String token = AuthController.getToken(req);
            String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

            Userdata userdata = Userdata.getUserdata(userId);
            if (userdata == null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print("[]");
                return;
            }

            if (req.getParameter("playlistId") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing playlistId query string.");
                return;
            }

            String playlistId = req.getParameter("playlistId");

            List<UserPlaylist> userPlaylists = userdata.getPlaylists();
            UserPlaylist myPlaylist = null;
            for (UserPlaylist userPlaylist : userPlaylists) {
                if (userPlaylist.getId().toHexString().equals(playlistId)) {
                    myPlaylist = userPlaylist;
                    break;
                }
            }
            if (myPlaylist == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("No playlist with this id.");
                return;
            }

            if (req.getParameter("identifier") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing identifier quwery string.");
                return;
            }
            String identifier = req.getParameter("identifier");

            if (!myPlaylist.containsIdentifier(identifier)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Identifier not in this playlist.");
                return;
            }

            myPlaylist.removeTrackByIdentifier(identifier);
            if(myPlaylist.saveInstance() == null) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("Server error. Please try again.");
                return;
            }
            else {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(new JSONObject().put("message", "All matching entries removed").toString());
                return;
            }
        }
    }


    @RequestMapping(value = "/userplaylist/track/delete", method = RequestMethod.DELETE)
    public Callable<ResponseEntity> deleteUserplaylistsEntry(@RequestParam(value = "id") String playlistId,
                                        @RequestParam(value = "identifier") String identifier,
                                        @RequestParam Map<String, String> requestParams,
                                        @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if (exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message", exceptionString).toString());
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        Userdata userdata = Userdata.getUserdata(userId);
        if (userdata == null) {
            return () -> ResponseEntity.ok("[]");
        }

        List<UserPlaylist> userPlaylists = userdata.getPlaylists();
        UserPlaylist myPlaylist = null;
        for (UserPlaylist userPlaylist : userPlaylists) {
            if (userPlaylist.getId().toHexString().equals(playlistId)) {
                myPlaylist = userPlaylist;
                break;
            }
        }
        if (myPlaylist == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No playlist with this id");
        }

        if (!myPlaylist.containsIdentifier(identifier)) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Identifier not in this playlist");
        }

        myPlaylist.removeTrackByIdentifier(identifier);
        if(myPlaylist.saveInstance() == null) {
            return () -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error. Please try again");
        }
        else {
            return () -> ResponseEntity.ok(new JSONObject().put("message", "All matching entries removed").toString());
        }
    }

    public static class UserPlaylistRenameHandler extends HttpServlet {
        public static final String PATH = "/userplaylist/rename";

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String exceptionString = AuthController.getAuthorizationErrorString(req);

            if (exceptionString != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(new JSONObject().put("message", exceptionString).toString());
                return;
            }

            if (req.getParameter("name") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing name query string.");
                return;
            }
            String name = req.getParameter("name");

            Matcher matcher = PlaylistCommand.PLAYLIST_NAME_PATTERN.matcher(name);
            if (!matcher.find()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print(String.format("Invalid name. It has to be between %d and %d letters",
                        PlaylistCommand.MIN_PLAYLIST_NAME_LENGTH,
                        PlaylistCommand.MAX_PLAYLIST_NAME_LENGTH));
                return;
            }

            String token = AuthController.getToken(req);
            String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

            Userdata userdata = Userdata.getUserdata(userId);
            if (userdata == null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print("[]");
                return;
            }

            if (req.getParameter("playlistId") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing playlistId query string.");
                return;
            }
            String playlistId = req.getParameter("playlistId");

            List<UserPlaylist> userPlaylists = userdata.getPlaylists();
            UserPlaylist myPlaylist = null;
            boolean nameALreadyTaken = false;
            for (UserPlaylist userPlaylist : userPlaylists) {
                if (userPlaylist.getId().toHexString().equals(playlistId)) {
                    myPlaylist = userPlaylist;
                    nameALreadyTaken = myPlaylist.getName().equals(name);
                    break;
                }
            }
            if (myPlaylist == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("No playlist with this id.");
                return;
            }

            if (nameALreadyTaken) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Name already taken.");
                return;
            }

            String oldName = myPlaylist.getName();
            myPlaylist.setName(name);

            if(myPlaylist.saveInstance() == null) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("Server error. Please try again.");
                return;
            }
            else {
                final String playlistName = myPlaylist.getName();
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(new JSONObject()
                        .put("message", String.format("Playlist %s renamed to %s", oldName, playlistName))
                        .toString());
                return;
            }
        }
    }


    @RequestMapping(value = "/userplaylist/rename", method = RequestMethod.PATCH)
    public Callable<ResponseEntity> renameUserplaylists(@RequestParam(value = "id") String playlistId,
                                           @RequestParam(value = "name") String name,
                                           @RequestParam Map<String, String> requestParams,
                                           @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if (exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JSONObject().put("message", exceptionString).toString());
        }

        Matcher matcher = PlaylistCommand.PLAYLIST_NAME_PATTERN.matcher(name);
        if (!matcher.find()) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(String.format("Invalid name. It has to be between %d and %d letters",
                            PlaylistCommand.MIN_PLAYLIST_NAME_LENGTH,
                            PlaylistCommand.MAX_PLAYLIST_NAME_LENGTH));
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        Userdata userdata = Userdata.getUserdata(userId);
        if (userdata == null) {
            return () -> ResponseEntity.ok("[]");
        }

        List<UserPlaylist> userPlaylists = userdata.getPlaylists();
        UserPlaylist myPlaylist = null;
        boolean nameALreadyTaken = false;
        for (UserPlaylist userPlaylist : userPlaylists) {
            if (userPlaylist.getId().toHexString().equals(playlistId)) {
                myPlaylist = userPlaylist;
                nameALreadyTaken = myPlaylist.getName().equals(name);
                break;
            }
        }
        if (myPlaylist == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No playlist with this id");
        }

        if (nameALreadyTaken) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name already taken");
        }

        String oldName = myPlaylist.getName();
        myPlaylist.setName(name);

        if(myPlaylist.saveInstance() == null) {
            return () -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error. Please try again");
        }
        else {
            final String playlistName = myPlaylist.getName();
            return () -> ResponseEntity.ok(new JSONObject()
                    .put("message", String.format("Playlist %s renamed to %s", oldName, playlistName))
                    .toString());
        }
    }
}
