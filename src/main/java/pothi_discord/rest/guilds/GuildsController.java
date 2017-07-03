package pothi_discord.rest.guilds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pothi_discord.Main;
import pothi_discord.bots.BotShard;
import pothi_discord.rest.auth.AuthController;
import pothi_discord.utils.Param;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Pascal Pothmann on 30.06.2017.
 */
@RestController
@CrossOrigin(origins = "*")
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

        return "";
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
