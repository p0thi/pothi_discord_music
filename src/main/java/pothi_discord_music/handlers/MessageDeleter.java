package pothi_discord_music.handlers;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Created by Pascal Pothmann on 27.01.2017.
 */
public class MessageDeleter implements Consumer<Message>, Schedule {
    private static final Logger log = MorphiaLoggerFactory.get(MessageDeleter.class);

    static Timer timer = new Timer();
    private long millis;
    private Message message;

    public MessageDeleter() {
        StaticSchedulePool.register(this);
        this.millis = 50000;
    }

    public MessageDeleter(long millis) {
        StaticSchedulePool.register(this);
        this.millis = millis;
    }

    public MessageDeleter(Message message, long millis) {
        StaticSchedulePool.register(this);
        this.millis = millis;
        this.message = message;
        accept(message);
    }

    @Override
    public void accept(Message message) {
        this.message = message;
        log.info("Message successfully queued, in "
                + message.getGuild().getName() + ". (" + message.getId() + ")" + " (\"" + message.getContent() + "\")");

        if (millis >= 0) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    delete();
                    finish();
                }
            }, millis);
        }
    }

    @Override
    public void execute() {
        delete();
    }

    public void delete() {
        try {
            String content = this.message.getContent();
            String id = this.message.getId();
            Guild guild = this.message.getGuild();

            this.message.delete().complete();

            log.info("Message successfully deleted, in "
                    + guild.getName() + ". (" + id + ")" + " (\"" + content + "\")");
        } catch (NullPointerException ignore) {
        } catch (Exception e) {
            log.warning("Could not delete Message! Already deleted?! (\"" + message.getContent() + "\")");
        }
    }

    private void finish() {
        StaticSchedulePool.unregister(this);
    }


    /*
    //  STATIC METHODS
     */

    public static MessageDeleter deleteMessage(Message message, long millis) {
        return new MessageDeleter(message, millis);
    }

    public static MessageDeleter deleteMessage(Message message) {
        return deleteMessage(message, 0);
    }
}
