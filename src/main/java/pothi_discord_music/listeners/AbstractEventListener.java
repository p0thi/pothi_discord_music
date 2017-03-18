package pothi_discord_music.listeners;

import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import pothi_discord_music.Main;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public abstract class AbstractEventListener extends ListenerAdapter{

    @Override
    public void onReady(ReadyEvent event) {
        Main.onInit(event);
    }
}
