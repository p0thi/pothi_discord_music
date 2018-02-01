package pothi_discord.rest.guilds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pothi_discord.Main;
import pothi_discord.bots.BotShard;
import pothi_discord.bots.sound.commands.audio.PlayerCommand;
import pothi_discord.bots.sound.commands.controll.CommandsCommand;
import pothi_discord.listeners.TrackScheduler;
import pothi_discord.permissions.PermissionManager;
import pothi_discord.rest.RestUtils;
import pothi_discord.rest.auth.AuthController;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommandEntry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Pascal Pothmann on 30.06.2017.
 */
@RestController
@CrossOrigin
public class GuildsController {

    public static class GuildsHandler extends HttpServlet {
        public static final String PATH = "/guilds";
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String exceptionString = AuthController.getAuthorizationErrorString(req);

            if (exceptionString != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print("{\"message\":\"" + exceptionString + "\"}");
                return;
            }

            String token = AuthController.getToken(req);
            String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

            ArrayList<String> guildIds = new ArrayList<>();
            if (req.getParameter("id") == null) {
                for(BotShard shard : Main.musicBot.shards) {
                    User user = shard.getJDA().getUserById(userId);
                    if(user != null) {
                        for(Guild guild : shard.getJDA().getMutualGuilds(user)) {
                            guildIds.add(guild.getId());
                        }
                    }
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(new JSONArray(guildIds).toString());
                return;
            }
            else {
                JSONObject guildData = new JSONObject();
                for (BotShard shard : Main.musicBot.shards) {
                    Guild guild = shard.getJDA().getGuildById(req.getParameter("id"));
                    if(guild != null) {
                        if (guild.getMemberById(userId) == null) {
                            continue;
                        }
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            guildData = new JSONObject(mapper.writeValueAsString(GuildData.getGuildDataByGuildId(guild.getId())));
                            guildData.remove("autoplaylistId");
                            guildData.remove("soundCommands");
                            guildData.remove("autoplaylist");
                            guildData.remove("id");
                            guildData.remove("v");
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            resp.getWriter().print("Internal error.");
                        }
                        break;
                    }
                }
                final String response = guildData.toString();
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().print(response);
                return;
            }
        }
    }

    @RequestMapping(value = "/guilds", method = RequestMethod.GET)
    public Callable<ResponseEntity> guilds(@RequestParam Map<String, String> requestParams,
                           @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionString);
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        ArrayList<String> guildIds = new ArrayList<>();
        JSONArray guildObjects = new JSONArray();
        if (!requestParams.containsKey("id")) {
            for(BotShard shard : Main.musicBot.shards) {
                User user = shard.getJDA().getUserById(userId);
                if(user != null) {
                    for(Guild guild : shard.getJDA().getMutualGuilds(user)) {
//                        guildIds.add(guild.getId());
                        JSONObject currentGuild = new JSONObject();
                        currentGuild.put("id", guild.getId())
                                .put("name", guild.getName())
                                .put("iconUrl", guild.getIconUrl() == null ? "" : guild.getIconUrl());
                        soundBotChecker:
                        for (BotShard soundShard : Main.soundBot.shards) {
                            for (Guild tempSoundGuild : soundShard.getJDA().getSelfUser().getMutualGuilds()) {
                                if (tempSoundGuild.getId().equals(guild.getId())) {
                                    currentGuild.put("soundBot", true);
                                    break soundBotChecker;
                                }
                            }
                        }
                        guildObjects.put(currentGuild);
                    }
                }
            }

            return () -> ResponseEntity.ok(guildObjects.toString());
        }
        else {
            JSONObject guildData = new JSONObject();
            for (BotShard shard : Main.musicBot.shards) {
                Guild guild = shard.getJDA().getGuildById(requestParams.get("id"));
                if(guild != null) {
                    if (guild.getMemberById(userId) == null) {
                        continue;
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        guildData = new JSONObject(mapper.writeValueAsString(GuildData.getGuildDataByGuildId(guild.getId())));
                        guildData.remove("autoplaylistId");
                        guildData.remove("soundCommands");
                        guildData.remove("autoplaylist");
                        guildData.remove("id");
                        guildData.remove("v");
                        } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return () -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error.");
                    }
                    break;
                }
            }
            final String response = guildData.toString();
            return () -> ResponseEntity.ok(response);
        }
    }

/*
    public static class GuildsSoundCommandsHandler extends HttpServlet {
        public static final String PATH = "/guilds/soundcommands";
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String error = AuthController.getAuthorizationErrorString(req);

            if (error != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(error);
            }

            if(req.getParameter("guildId") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing id query string.");
                return;
            }

            String token = AuthController.getToken(req);

            String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();
            String guildId = req.getParameter("guildId");

            Guild guild = Main.soundBot.getGuildById(guildId);

            if (guild == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Could not find this guild.");
                return;
            }
            User user = guild.getMemberById(userId).getUser();

            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("You have to be in this guild.");
                return;
            }

            boolean canUserShowCommands = PermissionManager.checkUserPermission(guild, user, new CommandsCommand().getName());

            if (!canUserShowCommands) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("You don't have the permissions to see the commands.");
                return;
            }

            GuildData guildData = GuildData.getGuildDataByGuildId(guildId);
            List<SoundCommandEntry> commands = guildData.getSoundCommands().getSoundCommandEntries();

            JSONArray jsonArray = new JSONArray(commands);

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonArray.getJSONObject(i).remove("fileId");
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(jsonArray.toString(2));
            return;
        }
    }
*/

    @RequestMapping(value = "/guilds/soundcommands", method = RequestMethod.GET)
    public Callable<ResponseEntity> getSoundoundcommands(@RequestParam Map<String, String> requestParams,
                                       @RequestHeader Map<String, String> headers) {

        String error = AuthController.getAuthorizationErrorString(headers, requestParams);

        if (error != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        if(!requestParams.containsKey("guildId")) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing id query string");
        }

        String token = AuthController.getToken(headers, requestParams);

        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();
        String guildId = requestParams.get("guildId");

        Guild guild = Main.soundBot.getGuildById(guildId);

        if (guild == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not find this guild");
        }

        User user = guild.getMemberById(userId).getUser();

        if (user == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST). body("You have to be in this guild.");
        }

        boolean canUserShowCommands = PermissionManager.checkUserPermission(guild, user, new CommandsCommand().getName());

        if (!canUserShowCommands) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You don't have the permissions to see the commands");
        }

        GuildData guildData = GuildData.getGuildDataByGuildId(guildId);
        List<SoundCommandEntry> commands = guildData.getSoundCommands().getSoundCommandEntries();

        JSONArray jsonArray = new JSONArray(commands);

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonArray.getJSONObject(i).remove("fileId");
        }

        return () -> ResponseEntity.ok(jsonArray.toString(2));
    }

    public static class GuildsSoundCommandsPlayHandler extends HttpServlet {
        public static final String PATH = "/guilds/soundcommands/play";
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String error = AuthController.getAuthorizationErrorString(req);


            if (error != null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().print(error);
                return;
            }


            if (req.getParameter("guildId") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing guildId query string");
                return;
            }

            if (req.getParameter("soundcommand") == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Missing soundcommand query string");
                return;
            }
            JSONObject body = RestUtils.getResquestBody(req);

            String token = AuthController.getToken(req);

            String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();
            String guildId = req.getParameter("guildId");

            Guild guild = Main.soundBot.getGuildById(guildId);

            if (guild == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("Could not find this guild.");
                return;
            }

            User user = guild.getMemberById(userId).getUser();

            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("You have to be in this guild.");
                return;
            }

            VoiceChannel voiceChannel = guild.getMember(user).getVoiceState().getChannel();

            if (voiceChannel == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("You have to be in a channel.");
                return;
            }

            if (!PlayerCommand.cooldownAllowed(guild, user)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("You have to wait before you can trigger more commands.");
                return;
            }

            String soundcommand = req.getParameter("soundcommand");

            TrackScheduler scheduler = Main.soundBot.getGuildAudioPlayer(guild).getScheduler();
            PlayerCommand playerCommand = new PlayerCommand(soundcommand, false);
            playerCommand.action(guild, user, voiceChannel, scheduler);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print(new JSONObject().put("message", "playing...").toString());
            return;
        }
    }


    @RequestMapping(value = "/guilds/soundcommands/play", method = RequestMethod.POST)
    public Callable<ResponseEntity> playSoundcommand(@RequestParam Map<String, String> requestParams,
                                        @RequestHeader Map<String, String> headers) {

        String error = AuthController.getAuthorizationErrorString(headers, requestParams);

        if (error != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        if (!requestParams.containsKey("guildId")) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing id query string");
        }

        if (!requestParams.containsKey("soundcommand")) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing soundcommand query string");
        }
        String soundcommand = null;
        try {
            soundcommand = URLDecoder
                    .decode(requestParams.get("soundcommand")
                            .replace("+", "%2B"), "UTF-8")
                    .replace("%2B", "+");
        } catch (UnsupportedEncodingException e) {
            return () -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }


        String token = AuthController.getToken(headers, requestParams);

        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();
        String guildId = requestParams.get("guildId");

        Guild guild = Main.soundBot.getGuildById(guildId);

        if (guild == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not find this guild.");
        }

        User user = guild.getMemberById(userId).getUser();

        if (user == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST). body("You have to be in this guild.");
        }

        VoiceChannel voiceChannel = guild.getMember(user).getVoiceState().getChannel();

        if (voiceChannel == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have to be in a channel.");
        }

        if (!PlayerCommand.cooldownAllowed(guild, user)) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have to wait before you can trigger more commands.");
        }

        TrackScheduler scheduler = Main.soundBot.getGuildAudioPlayer(guild).getScheduler();
        PlayerCommand playerCommand = new PlayerCommand(soundcommand, false);
        playerCommand.action(guild, user, voiceChannel, scheduler);

        return () -> ResponseEntity.ok(new JSONObject().put("message", "playing...").toString());
    }

    public static boolean isUserInGuild(String guildId, String userId) {
        boolean result = isUserInGuildsOfShards(guildId, userId, Main.musicBot.shards);
        result = result || isUserInGuildsOfShards(guildId, userId, Main.soundBot.shards);
        return result;
    }


    private static boolean isUserInGuildsOfShards(String guildId, String userId, ArrayList<BotShard> shards) {
        for (BotShard shard : shards) {
            Guild guild = shard.getJDA().getGuildById(guildId);
            if(guild == null) {
                continue;
            }
            Member member = guild.getMemberById(userId);
            if (member == null) {
                continue;
            }
            return true;
        }
        return false;
    }
}
