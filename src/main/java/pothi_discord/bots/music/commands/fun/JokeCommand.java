package pothi_discord.bots.music.commands.fun;


import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.utils.TextUtils;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Pascal Pothmann on 29.01.2017.
 */
public class JokeCommand extends GuildCommand {
    private static ArrayList<String> jokes = new ArrayList<>();
    private static HashMap<String, Long> channelTimeLimitStorage = new HashMap<>();
    private static final long rateLimitDuration = 10000;

    public JokeCommand() {
        try {
            //TODO move jokes to database
            jokes = TextUtils.readTxt("pothi_discord/config/jokes.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();

        if(!channelTimeLimitStorage.containsKey(channel.getId())) {
            channelTimeLimitStorage.put(channel.getId(), System.currentTimeMillis() - (rateLimitDuration + 1));
        }
        long lastCalledTime = channelTimeLimitStorage.get(channel.getId());

        if(System.currentTimeMillis() - lastCalledTime < rateLimitDuration) {
            channel.sendMessage(user.getAsMention() + " Es kann nur alle "
                    + rateLimitDuration/1000 + " Sekunden dieser Befehl in diesem Channel gesendet werden.").queue(new MessageDeleter());
            return;
        }

        channelTimeLimitStorage.put(channel.getId(), System.currentTimeMillis());

        MessageBuilder builder = new MessageBuilder();

        builder.append(jokes.get(new Random().nextInt(jokes.size())));
        builder.setTTS(true);
        channel.sendMessage(builder.build()).queue(new MessageDeleter());
    }

    @Override
    public String helpString() {
        return null;
    }
}
