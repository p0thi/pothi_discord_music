package pothi_discord.bots.music.commands.controll;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import oshi.util.FormatUtil;
import pothi_discord.bots.BotShard;
import pothi_discord.Main;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.bots.music.listeners.MusicBotMessageListener;
import pothi_discord.utils.Param;
import pothi_discord.utils.TextUtils;

import java.awt.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Pascal Pothmann on 27.02.2017.
 */
public class StatusCommand extends GuildCommand {
    private static OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();


    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard shard) {

        if (!checkPermission(event)) {
            return;
        }

        User user = event.getAuthor();

        TextChannel channel = event.getChannel();

        channel.sendTyping().queue();

        List<Guild> allGuilds = shard.getMyBot().getAllGuilds();
        int voiceConnections = 0;

        HashMap<String, User> allUsers = new HashMap<>();

        for(Guild myGuild : shard.getMyBot().getAllGuilds()) {
            if (myGuild.getAudioManager().isConnected()) {
                voiceConnections++;
            }
            for(Member member : myGuild.getMembers()) {
                User tmpUser = member.getUser();
                allUsers.put(tmpUser.getId(), tmpUser);
            }
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.decode(Param.BOT_COLOR_HEX));
        eb.setFooter("Requested by " + user.getName(), user.getAvatarUrl());
        eb.setAuthor(event.getJDA().getSelfUser().getName(), "http://glowtrap.de", event.getJDA().getSelfUser().getAvatarUrl()); //TODO url
        eb.setThumbnail("https://a.dryicons.com/images/icon_sets/polygon_icons/png/256x256/servers.png");

        eb.addField("Status", ":ok:", true);
        eb.addField("Guilds", allGuilds.size()+"", true);
        eb.addField("User", allUsers.size() + "", true);
        eb.addField("Nachrichten", MusicBotMessageListener.TOTAL_MESSAGES.toString(), true);
        eb.addField("Befehle", MusicBotMessageListener.TOTAL_COMMANDS.toString(), true);
        eb.addField("Audio Connections", voiceConnections+"", true);
        long totalMemory = getTotalMemoryInBytes();
        long freeMemory = getFreeMemoryInBytes();
        eb.addField("Threads", getThreadCount() + "", true);
        eb.addField("RAM", "[" + FormatUtil.formatBytes(totalMemory-freeMemory) + "/"
                + FormatUtil.formatBytes(totalMemory) + "]", true);
        eb.addField("Uptime", TextUtils.formatMillis(System.currentTimeMillis()-Main.START_TIME, true), true);

        channel.sendMessage(eb.build()).queue(new MessageDeleter());
    }

    public static int getThreadCount() {
        return threadMXBean.getThreadCount();
    }

    public static String getSystemCpuLoad() {
        return String.format("%.1f%%", operatingSystemMXBean.getSystemCpuLoad() * 100);
    }

    public static long getTotalMemoryInBytes() {
        return operatingSystemMXBean.getTotalPhysicalMemorySize();
    }

    public static long getFreeMemoryInBytes(){
        return operatingSystemMXBean.getFreePhysicalMemorySize();
    }

    @Override
    public void prepare() {

    }


    @Override
    public String helpString() {
        //TODO
        return null;
    }
}
