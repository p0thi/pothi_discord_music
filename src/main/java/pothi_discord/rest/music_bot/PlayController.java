package pothi_discord.rest.music_bot;

import io.jsonwebtoken.Jwts;
import net.dv8tion.jda.core.entities.Member;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pothi_discord.Main;
import pothi_discord.bots.music.commands.audio.PlayCommand;
import pothi_discord.bots.music.managers.audio.GuildMusicManager;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.permissions.PermissionManager;
import pothi_discord.rest.auth.AuthController;
import pothi_discord.utils.Param;
import pothi_discord.utils.audio.YoutubeMusicGenre;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Pascal Pothmann on 08.07.2017.
 */
@RestController
@CrossOrigin
@RequestMapping("/play")
public class PlayController {

    @RequestMapping(value = "/genre/{genreId}", method = RequestMethod.POST)
    public Callable<ResponseEntity> getAllGenres(@RequestParam Map<String, String> requestParams,
                                 @RequestHeader Map<String, String> headers,
                                 @PathVariable("genreId") String genreId) {
        String exceptionString = AuthController.getAuthorizationErrorString(headers, requestParams);

        if (exceptionString != null) {
            return () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionString);
        }

        String token = AuthController.getToken(headers, requestParams);
        String userId = Jwts.parser().setSigningKey(Param.SECRET_KEY()).parseClaimsJws(token).getBody().getSubject();

        Member member = Main.musicBot.getMemberOfActiveVoiceChannel(userId);

        String voiceStatusErrorString = MusicBotController.getVoiceStatusErrorString(member);
        if(voiceStatusErrorString != null) {
            return () -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(voiceStatusErrorString);
        }

        boolean permission = PermissionManager.checkUserPermission(member.getGuild(), member.getUser(), new PlayCommand().getName());
        if(!permission) {
            return () -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have the permission to play stuff on that server.");
        }

        GuildMusicManager manager = (GuildMusicManager) Main.musicBot.getGuildAudioPlayer(member.getGuild());
        YoutubeMusicGenre genre = YoutubeMusicGenre.getGenreById(genreId);

        if (genre == null) {
            return () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not a valid genre id");
        }
        manager.setGenrePlaylist(genre);

        member.getGuild().getPublicChannel().sendMessage(String.format(
                member.getAsMention() + " Das genre **%s** wird jetzt gespielt.\n" +
                "Die standart Playlist kann mit **%splay default** wieder aktiviert werden.",
                genre.getReadableName(), Param.PREFIX())).queue(new MessageDeleter());
        return () -> ResponseEntity.ok(new JSONObject()
                .put("message", String.format("Genre no %s (%d) is now selected",
                        genre.getReadableName(),
                        genre.ordinal() + 1)).toString());
    }
}
