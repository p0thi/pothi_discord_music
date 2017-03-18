package pothi_discord_music.commands.fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pothi_discord_music.commands.Command;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.utils.TextUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Pascal Pothmann on 27.02.2017.
 */
public class GifCommand extends GuildCommand {
    ArrayList<User> cooldown = new ArrayList<>();
    ArrayList<String> categories = new ArrayList<>();
    static Timer timer = new Timer();

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if(!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();

        if(cooldown.contains(user)) {
            channel.sendMessage(user.getAsMention() + " Du musst warten, bevor du weitere Befehle senden kannst.")
                    .queue(new MessageDeleter(10000));
        }
        else {
            String tag;
            if(args.length > 1) {
                tag = event.getMessage().getContent().substring(args[0].length() + 1);
            }
            else {
                tag = categories.get(new Random().nextInt(categories.size()));
            }

            try {
                channel.sendTyping().queue();

                String url = "http://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tag="
                        + URLEncoder.encode(tag, "UTF-8");
                String response = TextUtils.getStringFromURL(url);
                JSONObject obj = new JSONObject(response);
                if(obj.getJSONObject("meta").getInt("status") != 200) {
                    throw new RuntimeException("Status was not 200");
                }
                Object datas = obj.get("data");

                if (datas instanceof JSONObject)
                    channel.sendMessage(obj.getJSONObject("data").getString("image_url"))
                            .queue(new MessageDeleter(180000));
                else if (datas instanceof JSONArray)
                    channel.sendMessage("Zu diesem Begriff wurde nichts gefunden.").queue(new MessageDeleter());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                channel.sendMessage("Ein Fehler ist aufgetreten. Versuche es erneut.").queue(new MessageDeleter());
            }

            cooldown.add(user);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    cooldown.remove(user);
                }
            }, 4000);
        }

    }


    @Override
    public String helpString() {
        return null;
    }

    @Override
    public void prepare() {
        //TODO Guild specific tags
        addWithWeight("funny", 1);
        addWithWeight("cat", 1);
        addWithWeight("gaming", 3);
        addWithWeight("mlg", 1);
        addWithWeight("games", 3);
        addWithWeight("meme", 2);
        addWithWeight("memes", 2);
    }

    private void addWithWeight(String tag, int times) {
        if (times <= 0) {
            categories.add(tag);
            return;
        }
        for(int i = 0; i < times; i++) {
            categories.add(tag);
        }
    }
}
