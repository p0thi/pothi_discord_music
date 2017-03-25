package pothi_discord_music.handlers;


import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

import java.util.ArrayList;

/**
 * Created by Pascal Pothmann on 28.01.2017.
 */
public class StaticSchedulePool {
    private static final Logger log = MorphiaLoggerFactory.get(StaticSchedulePool.class);


    private static final ArrayList<Schedule> ALL_SCHEDULES = new ArrayList<>();

    public synchronized static void register(Schedule deleter) {
        ALL_SCHEDULES.add(deleter);
    }

    public synchronized static void unregister(Schedule deleter) {
        ALL_SCHEDULES.remove(deleter);
    }

    public synchronized static void executeAllTasks() {
        log.info("Executing SchedulePool");
        for (Schedule schedule : ALL_SCHEDULES) {
            schedule.execute();
        }
        ALL_SCHEDULES.clear();
    }
}
