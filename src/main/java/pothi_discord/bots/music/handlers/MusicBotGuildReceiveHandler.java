package pothi_discord.bots.music.handlers;

import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import net.dv8tion.jda.core.entities.Guild;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.handlers.AbstractReceiveHandler;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

/**
 * Created by Pascal Pothmann on 27.01.2017.
 */
public class MusicBotGuildReceiveHandler extends AbstractReceiveHandler {
    private static final Logger log = MorphiaLoggerFactory.get(MusicBotGuildReceiveHandler.class);

    long count = 0;
    boolean listenerStarted = false;
    public final Guild GUILD;

    public MusicBotGuildReceiveHandler(Guild guild) {
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
        GuildData.getGuildDataByGuildId(GUILD.getId()).getAudioUtils().addBytes(combinedAudio.getAudioData(1.0));
        //log.info(Arrays.toString(combinedAudio.getAudioData(1.0)));
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {

    }
}
