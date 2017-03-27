package pothi_discord.bots.music.managers.audio;

import pothi_discord.utils.audio.YoutubeMusicGenre;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Pascal Pothmann on 24.02.2017.
 */
public class AutoPlaylist {
    private List<String> content;
    private String source;
    private String name;

    public AutoPlaylist(YoutubeMusicGenre genre) {
        this.source = null;
        this.content = genre.getAllEntries();
        this.name = genre.getReadableName() + " (Nummer: " + (genre.ordinal() + 1) + ")";

    }

    public AutoPlaylist(List<String> content) {
        this(content, null);
    }

    public AutoPlaylist(List<String> content, String source) {
        this.content = content;
        if (content == null) {
            this.content = new ArrayList<>();
        }
        this.source = source;
        this.name = "Standard Playlist";
    }

    public int size() {
        return content.size();
    }

    public String getRandomElement() {
        return this.content.get(new Random().nextInt(size()));
    }

    public String get(int i) {
        if(i >= size() || i < 0) {
            throw new IndexOutOfBoundsException("Index " + i + " not in scope.");
        }
        return this.content.get(i);
    }

    public void refresh() {
        if(this.source == null) {
            return;
        }

        //TODO refresh the content by using the source String
    }

    public String getName() {
        return this.name + " (" + size() + " Einträge)";
    }
}
