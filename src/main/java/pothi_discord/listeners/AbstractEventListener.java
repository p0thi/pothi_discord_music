package pothi_discord.listeners;

import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import pothi_discord.bots.BotShard;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public abstract class AbstractEventListener extends ListenerAdapter{

    protected static final Pattern PATTERN = Pattern.compile("([\\p{L}\\p{Digit}_&])+");
    public static final AtomicLong TOTAL_MESSAGES = new AtomicLong();
    public static final AtomicLong TOTAL_COMMANDS = new AtomicLong();

    @Override
    public void onReady(ReadyEvent event) {
        getShard().getMyBot().onInit(event);
    }

    public abstract BotShard getShard();
}
