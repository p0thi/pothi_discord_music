package pothi_discord.rest.music_bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.jsonwebtoken.Jwts;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
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

import java.util.Map;

/**
 * Created by Pascal Pothmann on 03.07.2017.
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/music")
public class MusicBotController {

    @RequestMapping(value = "/pause", method = RequestMethod.POST)
    public Object pause(@RequestParam Map<String, String> requestParams,
                        @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionString);
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        Member member = Main.musicBot.getMemberOfActiveVoiceChannel(userId);

        String voiceStatusError = getVoiceStatusErrorString(member);

        if(voiceStatusError != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(voiceStatusError);
        }

        Guild guild = member.getGuild();

        String permissionName = new PauseCommand().getName();
        boolean hasCommandPermission = PermissionManager.checkUserPermission(guild, member.getUser(), permissionName);

        if(!hasCommandPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format("You don't have the %s permission.", permissionName));
        }
        AudioPlayer player = Main.musicBot.getGuildAudioPlayer(guild).getPlayer();
        player.setPaused(!player.isPaused());

        return ResponseEntity.ok(new JSONObject().put("message", player.isPaused() ? "paused" : "unpaused").toString());
    }

    @RequestMapping(value = "/skip", method = RequestMethod.POST)
    public Object skip(@RequestParam Map<String, String> requestParams,
                        @RequestHeader Map<String, String> headers) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if(exceptionString != null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionString);
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        Member member = Main.musicBot.getMemberOfActiveVoiceChannel(userId);

        String voiceStatusError = getVoiceStatusErrorString(member);
        if (voiceStatusError != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(voiceStatusError);
        }

        Guild guild = member.getGuild();

        GuildAudioManager musicManager = Main.musicBot.getGuildAudioPlayer(guild);

        boolean canInstaSkip = PermissionManager.checkUserPermission(guild, member.getUser(), "can-instant-skip");
        if (canInstaSkip) {

            JSONObject response = getSkipResponseObject(musicManager);
            return ResponseEntity.ok(response.toString());
        }

        TextChannel textChannel = guild.getPublicChannel();
        boolean skipCountReached = SkipCommand.checkSkipCount(musicManager, guild, textChannel);
        if (skipCountReached) {
            JSONObject response = getSkipResponseObject(musicManager);
            return ResponseEntity.ok(response.toString());
        }
        else {
            return ResponseEntity.ok(new JSONObject().put("message", "skip request queued"). toString());
        }
    }

    private JSONObject getSkipResponseObject(GuildAudioManager musicManager) {
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

    private String getVoiceStatusErrorString(Member member) {
        String error = new JSONObject().put("message", "You have to be in a voice channel with the bot").toString();
        if (member == null) {
            return error;
        }

        VoiceChannel voiceChannel = member.getVoiceState().getChannel();
        Guild guild = member.getGuild();

        if (!voiceChannel.equals(guild.getMember(guild.getJDA().getSelfUser()).getVoiceState().getChannel())) {
            return error;
        }

        return null;
    }
}
