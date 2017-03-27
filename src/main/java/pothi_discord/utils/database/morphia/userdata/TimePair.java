package pothi_discord.utils.database.morphia.userdata;

import org.mongodb.morphia.annotations.Embedded;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
@Embedded
public class TimePair {
    private String date;
    private Long duration;


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
