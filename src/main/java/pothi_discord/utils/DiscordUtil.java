package pothi_discord.utils;


import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.requests.*;
import org.json.JSONObject;

public class DiscordUtil {

    private DiscordUtil() {
    }

    /* TODO...
     public static boolean isMainBot() {
        return (PothiBot.getScopes() & 0x100) != 0;
    }

    public static boolean isMusicBot() {
        return (PothiBot.getScopes() & 0x010) != 0;
    }

    public static boolean isSelfBot() {
        return (PothiBot.getScopes() & 0x001) != 0;
    }

    public static boolean isMainBotPresent(Guild guild) {
        JDA jda = guild.getJDA();
        User other = jda.getUserById(BotConstants.MAIN_BOT_ID);
        return other != null && guild.getMember(other) != null;
    }

    public static boolean isMusicBotPresent(Guild guild) {
        JDA jda = guild.getJDA();
        User other = jda.getUserById(BotConstants.MUSIC_BOT_ID);
        return other != null && guild.getMember(other) != null;
    }

    public static boolean isPatronBotPresentAndOnline(Guild guild) {
        JDA jda = guild.getJDA();
        User other = jda.getUserById(BotConstants.PATRON_BOT_ID);
        return other != null && guild.getMember(other) != null && guild.getMember(other).getOnlineStatus() == OnlineStatus.ONLINE;
    }

    public static boolean isUserBotCommander(Guild guild, User user) {
        List<Role> roles = guild.getMember(user).getRoles();

        for (Role r : roles) {
            if (r.getName().equals("Bot Commander")) {
                return true;
            }
        }

        return false;
    }
    */

    public static void sendShardlessMessage(String channel, Message msg) {
        sendShardlessMessage(msg.getJDA(), channel, msg.getRawContent());
    }

    public static void sendShardlessMessage(JDA jda, String channel, String content) {
        JSONObject body = new JSONObject();
        body.put("content", content);
        new RestAction<Void>(jda, Route.Messages.SEND_MESSAGE.compile(channel), body) {
            @Override
            protected void handleResponse(Response response, Request request) {
                if (response.isOk())
                    request.onSuccess(null);
                else
                    request.onFailure(response);
            }
        }.queue();
    }

    public static int getRecommendedShardCount(String token) throws UnirestException {
        return Unirest.get(Requester.DISCORD_API_PREFIX + "gateway/bot")
                .header("Authorization", "Bot " + token)
                .header("User-agent", "PothiBot DiscordMusicBotShard, " + JDAInfo.VERSION + ")")
                .asJson()
                .getBody()
                .getObject()
                .getInt("shards");
    }

}
