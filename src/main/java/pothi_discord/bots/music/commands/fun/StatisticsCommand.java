package pothi_discord.bots.music.commands.fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.utils.HugeMessageSender;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.morphia.userdata.Gametime;
import pothi_discord.utils.database.morphia.userdata.TimePair;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import java.util.Collections;

/**
 * Created by Pascal Pothmann on 27.03.2017.
 */
public class StatisticsCommand extends GuildCommand{
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard botShard) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }
        event.getChannel().sendTyping().queue();

        Userdata userdata = Userdata.getUserdata(user.getId());


        int days = -1;
        String currentModifiedDate = TextUtils.millisToDate(System.currentTimeMillis(), "-");

        HugeMessageSender hugeMessageSender = new HugeMessageSender();
        hugeMessageSender.setHeader("Alle registrierten Spielzeiten von " + user.getName()
                + (days > -1 ? (days == 1 ? " von heute$MULTIPAGE$:" : " in den letzten " + days + " Tagen$MULTIPAGE$:") : "$MULTIPAGE$:"));
        hugeMessageSender.setTag(HugeMessageSender.MessageTag.INFO);
        hugeMessageSender.setMpInfix(". Seite $CURRENTPAGE$/$TOTALPAGES$");

        long totalSum = 0;

        Collections.sort(userdata.getGametime());

        for (Gametime gametime : userdata.getGametime()) {

            long sum = 0;

            for (TimePair timePair : gametime.getTimePairs()) {
                long tmpDays = TextUtils.daysBetwenDates(timePair.getDate(), currentModifiedDate, "-");

                if (tmpDays < days || days < 0) {
                    sum += timePair.getDuration();
                    totalSum += timePair.getDuration();
                }
            }

            if (sum > 0) {

                hugeMessageSender.append(gametime.getGameName() + " "
                        + "    ................................."
                        .substring(0, Math.max(0, 38 - gametime.getGameName().length()))
                        + ".... " + TextUtils.getMillisFormatted(sum) + "\n");

            }

        }

        hugeMessageSender.append("\nGesamtspielzeit: " + TextUtils.getMillisFormatted(totalSum) + "\n");
        hugeMessageSender.append("Alle Angaben ohne Gewähr." + " ;)");

        hugeMessageSender.send(event.getChannel());
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
