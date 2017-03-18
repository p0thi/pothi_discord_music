package pothi_discord_music.handlers;

import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pothi_discord_music.utils.GuildData;
import pothi_discord_music.utils.audio.AudioUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Pascal Pothmann on 27.01.2017.
 */
public class GuildReceiveHandler extends AbstractReceiveHandler {
    private static final Logger log = LoggerFactory.getLogger(GuildReceiveHandler.class);

    long count = 0;
    boolean listenerStarted = false;
    public final Guild GUILD;

    public GuildReceiveHandler (Guild guild) {
        this.GUILD = guild;
        log.info("Instance created");
    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public boolean canReceiveUser() {
        return false;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        GuildData.ALL_GUILD_DATAS.get(GUILD.getId()).getAudioUtils().addBytes(combinedAudio.getAudioData(1.0));
        //log.info(Arrays.toString(combinedAudio.getAudioData(1.0)));
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {

    }
}
