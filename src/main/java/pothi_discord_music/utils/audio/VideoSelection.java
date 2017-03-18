package pothi_discord_music.utils.audio;

import net.dv8tion.jda.core.entities.Message;
import pothi_discord_music.utils.youtube.YoutubeVideo;

import java.util.ArrayList;

public class VideoSelection {

    public final ArrayList<YoutubeVideo> choices;
    public final Message outMsg;

    public VideoSelection(ArrayList<YoutubeVideo> choices, Message outMsg) {
        this.choices = choices;
        this.outMsg = outMsg;
    }

    public ArrayList<YoutubeVideo> getChoices() {
        return choices;
    }

    public Message getOutMsg() {
        return outMsg;
    }

}
