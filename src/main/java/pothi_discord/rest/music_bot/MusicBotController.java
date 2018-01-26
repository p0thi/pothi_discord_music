package pothi_discord.rest.music_bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.jsonwebtoken.Jwts;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pothi_discord.Main;
import pothi_discord.bots.music.commands.audio.PauseCommand;
import pothi_discord.bots.music.commands.audio.SkipCommand;
import pothi_discord.managers.GuildAudioManager;
import pothi_discord.permissions.PermissionManager;
import pothi_discord.rest.auth.AuthController;
import pothi_discord.utils.Param;
import pothi_discord.utils.audio.YoutubeMusicGenre;
import pothi_discord.utils.database.morphia.autoplaylists.AutoPlaylist;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Pascal Pothmann on 03.07.2017.
 */
@RestController
@CrossOrigin
@RequestMapping("/music")
public class MusicBotController {

    public static class GenresHandler extends HttpServlet {
        public static final String PATH = "/genres";
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String exceptionString = AuthController.getAuthorizationErrorString(req);

            if(exceptionString != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(exceptionString);
                return;
            }

            JSONArray result = new JSONArray();

            for(YoutubeMusicGenre youtubeMusicGenre : YoutubeMusicGenre.values()) {
                JSONObject tmp = new JSONObject()
                        .put("name", youtubeMusicGenre.name())
                        .put("url", youtubeMusicGenre.getLink())
                        .put("readable_name", youtubeMusicGenre.getReadableName())
                        .put("ordinal", youtubeMusicGenre.ordinal());
                result.put(tmp);
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(result.toString());
            return;
        }
    }


    @RequestMapping(value = "/genres", method = RequestMethod.GET)
    public Callable<ResponseEntity> getAllGenres(@RequestParam Map<String, String> requestParams,
                                 @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionString);
        }

        JSONArray result = new JSONArray();

        for(YoutubeMusicGenre youtubeMusicGenre : YoutubeMusicGenre.values()) {
            JSONObject tmp = new JSONObject()
                    .put("name", youtubeMusicGenre.name())
                    .put("url", youtubeMusicGenre.getLink())
                    .put("readable_name", youtubeMusicGenre.getReadableName())
                    .put("ordinal", youtubeMusicGenre.ordinal());
            result.put(tmp);
        }
        return () -> ResponseEntity.ok(result.toString());
    }

    public static class GenreHandler extends HttpServlet {
        public static final String PATH = "/genre";
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String exceptionString = AuthController.getAuthorizationErrorString(req);

            if(exceptionString != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(exceptionString);
                return;
            }

            if (req.getParameter("genreId") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing genreId query string");
                return;
            }

            YoutubeMusicGenre youtubeMusicGenre = YoutubeMusicGenre.getGenreById(req.getParameter("genreId"));

            if (youtubeMusicGenre == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Invalid genre id.");
                return;
            }

            AutoPlaylist autoPlaylist = youtubeMusicGenre.getMongoPlaylist();
            ObjectMapper mapper = new ObjectMapper();
            JSONArray tmp;
            try {
                tmp = new JSONArray(mapper.writeValueAsString(autoPlaylist.getContent()));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("Could not parse data.");
                return;
            }
            JSONObject result = new JSONObject()
                    .put("content", tmp)
                    .put("name", youtubeMusicGenre.name())
                    .put("url", youtubeMusicGenre.getLink())
                    .put("readable_name", youtubeMusicGenre.getReadableName())
                    .put("ordinal", youtubeMusicGenre.ordinal());
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(result.toString());
            return;
        }
    }

    @RequestMapping(value = "/genre/{genreId}", method = RequestMethod.GET)
    public Callable<ResponseEntity> getSingleGenre(@RequestParam Map<String, String> requestParams,
                                 @RequestHeader Map<String, String> headers,
                                 @PathVariable("genreId") String genreId) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionString);
        }

        YoutubeMusicGenre youtubeMusicGenre = YoutubeMusicGenre.getGenreById(genreId);

        if (youtubeMusicGenre == null) {
            return () -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
        }

        AutoPlaylist autoPlaylist = youtubeMusicGenre.getMongoPlaylist();
        ObjectMapper mapper = new ObjectMapper();
        JSONArray tmp;
        try {
            tmp = new JSONArray(mapper.writeValueAsString(autoPlaylist.getContent()));
        } catch (Exception e) {
            return () -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("could not parse data");
        }
        JSONObject result = new JSONObject()
                .put("content", tmp)
                .put("name", youtubeMusicGenre.name())
                .put("url", youtubeMusicGenre.getLink())
                .put("readable_name", youtubeMusicGenre.getReadableName())
                .put("ordinal", youtubeMusicGenre.ordinal());
        return () -> ResponseEntity.ok(result.toString());
    }

    public static class PauseHandler extends HttpServlet {
        public static final String PATH = "/pause";
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String exceptionString = AuthController.getAuthorizationErrorString(req);

            if(exceptionString != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(exceptionString);
                return;
            }

            String token = AuthController.getToken(req);
            String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

            Member member = Main.musicBot.getMemberOfActiveVoiceChannel(userId);

            String voiceStatusError = getVoiceStatusErrorString(member);

            if(voiceStatusError != null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print(voiceStatusError);
                return;
            }

            Guild guild = member.getGuild();

            String permissionName = new PauseCommand().getName();
            boolean hasCommandPermission = PermissionManager.checkUserPermission(guild, member.getUser(), permissionName);

            if(!hasCommandPermission) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print(String.format("You don't have the %s permission.", permissionName));
                return;
            }
            AudioPlayer player = Main.musicBot.getGuildAudioPlayer(guild).getPlayer();
            player.setPaused(!player.isPaused());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(new JSONObject().put("message", player.isPaused() ? "paused" : "unpaused").toString());
            return;
        }
    }


    @RequestMapping(value = "/pause", method = RequestMethod.POST)
    public Callable<ResponseEntity> pause(@RequestParam Map<String, String> requestParams,
                        @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionString);
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        Member member = Main.musicBot.getMemberOfActiveVoiceChannel(userId);

        String voiceStatusError = getVoiceStatusErrorString(member);

        if(voiceStatusError != null) {
            return () -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(voiceStatusError);
        }

        Guild guild = member.getGuild();

        String permissionName = new PauseCommand().getName();
        boolean hasCommandPermission = PermissionManager.checkUserPermission(guild, member.getUser(), permissionName);

        if(!hasCommandPermission) {
            return () -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format("You don't have the %s permission.", permissionName));
        }
        AudioPlayer player = Main.musicBot.getGuildAudioPlayer(guild).getPlayer();
        player.setPaused(!player.isPaused());

        return () -> ResponseEntity.ok(new JSONObject().put("message", player.isPaused() ? "paused" : "unpaused").toString());
    }

    public static class SkipHandler extends HttpServlet {
        public static final String PATH = "/skip";
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String exceptionString = AuthController.getAuthorizationErrorString(req);

            if(exceptionString != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(exceptionString);
                return;
            }

            String token = AuthController.getToken(req);
            String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

            Member member = Main.musicBot.getMemberOfActiveVoiceChannel(userId);

            String voiceStatusError = getVoiceStatusErrorString(member);
            if (voiceStatusError != null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().print(voiceStatusError);
                return;
            }

            Guild guild = member.getGuild();

            GuildAudioManager musicManager = Main.musicBot.getGuildAudioPlayer(guild);

            boolean canInstaSkip = PermissionManager.checkUserPermission(guild, member.getUser(), "can-instant-skip");
            if (canInstaSkip) {

                JSONObject response = getSkipResponseObject(musicManager);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(response.toString());
                return;
            }

            TextChannel textChannel = guild.getDefaultChannel();
            boolean skipCountReached = SkipCommand.checkSkipCount(musicManager, guild, textChannel);
            if (skipCountReached) {
                JSONObject response = getSkipResponseObject(musicManager);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(response.toString());
                return;
            }
            else {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(new JSONObject().put("message", "skip request queued"). toString());
                return;
            }
        }
    }


    @RequestMapping(value = "/skip", method = RequestMethod.POST)
    public Callable<ResponseEntity> skip(@RequestParam Map<String, String> requestParams,
                        @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionString);
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        Member member = Main.musicBot.getMemberOfActiveVoiceChannel(userId);

        String voiceStatusError = getVoiceStatusErrorString(member);
        if (voiceStatusError != null) {
            return () -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(voiceStatusError);
        }

        Guild guild = member.getGuild();

        GuildAudioManager musicManager = Main.musicBot.getGuildAudioPlayer(guild);

        boolean canInstaSkip = PermissionManager.checkUserPermission(guild, member.getUser(), "can-instant-skip");
        if (canInstaSkip) {

            JSONObject response = getSkipResponseObject(musicManager);
            return () -> ResponseEntity.ok(response.toString());
        }

        TextChannel textChannel = guild.getDefaultChannel();
        boolean skipCountReached = SkipCommand.checkSkipCount(musicManager, guild, textChannel);
        if (skipCountReached) {
            JSONObject response = getSkipResponseObject(musicManager);
            return () -> ResponseEntity.ok(response.toString());
        }
        else {
            return () -> ResponseEntity.ok(new JSONObject().put("message", "skip request queued"). toString());
        }
    }

    private static JSONObject getSkipResponseObject(GuildAudioManager musicManager) {
        AudioTrack oldTrack = musicManager.getPlayer().getPlayingTrack();

        String newTitle = SkipCommand.nextTrack(musicManager);
        String oldTitle;

        String message;
        if (oldTrack != null) {
            message = "skipped";
            oldTitle = oldTrack.getInfo().title;
        }
        else {
            message = "started";
            oldTitle = "none";
        }

        return new JSONObject()
                .put("message", message)
                .put("old_track", oldTitle)
                .put("new_track", newTitle);
    }

    public static String getVoiceStatusErrorString(Member member) {
        String error = "You have to be in a voice channel with the bot";
        if (member == null) {
            System.out.println("Member = null");
            return error;
        }

        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        Guild guild = member.getGuild();

        if (!voiceChannel.getId().equals(guild.getMember(guild.getJDA().getSelfUser()).getVoiceState().getChannel().getId())) {
            System.out.println("Cannel is not matching");
            return error;
        }

        return null;
    }
}
