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
import pothi_discord.rest.auth.AuthController;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;
import pothi_discord.utils.database.morphia.guilddatas.SoundCommandEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Pascal Pothmann on 30.06.2017.
 */
@RestController
@CrossOrigin
public class GuildsController {

    @RequestMapping(value = "/guilds", method = RequestMethod.GET)
    public Object guilds(@RequestParam Map<String, String> requestParams,
                         @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionString);
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        ArrayList<String> guildIds = new ArrayList<>();
        if (!requestParams.containsKey("id")) {
            for(BotShard shard : Main.musicBot.shards) {
                User user = shard.getJDA().getUserById(userId);
                if(user != null) {
                    for(Guild guild : shard.getJDA().getMutualGuilds(user)) {
                        guildIds.add(guild.getId());
                    }
                }
            }

            return new JSONArray(guildIds).toString();
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
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    break;
                }
            }
            return ResponseEntity.ok(guildData.toString());
        }
    }

    @RequestMapping(value = "/guilds/soundcommands", method = RequestMethod.GET)
    public Object getSoundoundcommands(@RequestParam Map<String, String> requestParams,
                                       @RequestHeader Map<String, String> headers) {

        String error = AuthController.getAuthorizationErrorString(headers, requestParams);

        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        if(!requestParams.containsKey("guildId")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing id query string");
        }

        String token = AuthController.getToken(headers, requestParams);

        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();
        String guildId = requestParams.get("guildId");

        Guild guild = Main.soundBot.getGuildById(guildId);

        if (guild == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not find this guild");
        }

        User user = guild.getMemberById(userId).getUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST). body("You have to be in this guild.");
        }

        boolean canUserShowCommands = PermissionManager.checkUserPermission(guild, user, new CommandsCommand().getName());

        if (!canUserShowCommands) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You don't have the permissions to see the commands");
        }

        GuildData guildData = GuildData.getGuildDataByGuildId(guildId);
        List<SoundCommandEntry> commands = guildData.getSoundCommands().getSoundCommandEntries();

        JSONArray jsonArray = new JSONArray(commands);

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonArray.getJSONObject(i).remove("fileId");
        }

        return ResponseEntity.ok(jsonArray.toString(2));
    }

    @RequestMapping(value = "/guilds/soundcommands/play/{soundcommand}", method = RequestMethod.POST)
    public Object playSoundcommand(@RequestParam Map<String, String> requestParams,
                                        @RequestHeader Map<String, String> headers,
                                        @PathVariable("soundcommand") String soundcommand) {

        String error = AuthController.getAuthorizationErrorString(headers, requestParams);

        if (error != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        if (!requestParams.containsKey("guildId")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing id query string");
        }

        String token = AuthController.getToken(headers, requestParams);

        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();
        String guildId = requestParams.get("guildId");

        Guild guild = Main.soundBot.getGuildById(guildId);

        if (guild == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not find this guild");
        }

        User user = guild.getMemberById(userId).getUser();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST). body("You have to be in this guild.");
        }

        VoiceChannel voiceChannel = guild.getMember(user).getVoiceState().getChannel();

        if (voiceChannel == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have to be in a channel.");
        }

        if (!PlayerCommand.cooldownAllowed(guild, user)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have to wait before you can trigger more commands.");
        }

        TrackScheduler scheduler = Main.soundBot.getGuildAudioPlayer(guild).getScheduler();
        PlayerCommand playerCommand = new PlayerCommand(soundcommand, false);
        playerCommand.action(guild, user, voiceChannel, scheduler);

        return ResponseEntity.ok(new JSONObject().put("message", "playing...").toString());
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
