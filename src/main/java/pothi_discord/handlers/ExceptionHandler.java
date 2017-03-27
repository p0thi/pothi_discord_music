package pothi_discord.handlers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;

/**
 * Created by Pascal Pothmann on 20.02.2017.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler{
    private static final Logger log = MorphiaLoggerFactory.get(ExceptionHandler.class);

    private static User owner = null;

    private JDA jda;
    private Throwable e;

    public ExceptionHandler(Throwable e) {
        this.e = e;

        try {
            sendException();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    public ExceptionHandler() {

    }


    public void handleException(Throwable e){
        setException(e);
        try {
            sendException();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    private synchronized void sendException() throws InterruptedException {
        if (owner == null) {
            return;
        }

        while (e == null) {
            wait();
        }

        MessageBuilder builder = new MessageBuilder();
        builder.append(e.toString());

        for (StackTraceElement elem : e.getStackTrace()) {
            builder.append("\n" + elem.toString());
        }
        owner.getPrivateChannel().sendMessage(builder.build()).queue();
        e = null;
    }

    public synchronized void setException(Throwable e) {
        this.e = e;
        notifyAll();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught Exception in Thread " + t.getName() + ".", e);
        handleException(e);
    }

    public static void setOwner(User user) {
        if (owner != null) {
            return;
        }

        owner = user;
    }
    public static boolean isOwnerSet(){
        return owner != null;
    }
}
