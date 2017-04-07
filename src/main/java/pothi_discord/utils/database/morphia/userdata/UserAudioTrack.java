package pothi_discord.utils.database.morphia.userdata;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by Pascal Pothmann on 06.04.2017.
 */
@Embedded
public class UserAudioTrack {

    private String title;
    private String author;
    private Long length;
    private String identifier;
    private String uri;

    public static UserAudioTrack convertAudioTrack(AudioTrack audioTrack) {
        AudioTrackInfo info = audioTrack.getInfo();

        if (info.isStream) {
            throw new RuntimeException("Cant handle Strams");
        }

        UserAudioTrack result = new UserAudioTrack();

        result.setTitle(info.title);
        result.setAuthor(info.author);
        result.setLength(info.length);
        result.setIdentifier(info.identifier);
        result.setUri(info.uri);

        return result;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
