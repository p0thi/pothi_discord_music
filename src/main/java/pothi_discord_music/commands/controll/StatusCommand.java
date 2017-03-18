package pothi_discord_music.commands.controll;

import net.dv8tion.jda.core.entities.Member;
import oshi.SystemInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.Sensors;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import pothi_discord_music.Main;
import pothi_discord_music.commands.GuildCommand;
import pothi_discord_music.handlers.MessageDeleter;
import pothi_discord_music.listeners.MessageListener;
import pothi_discord_music.utils.Param;
import pothi_discord_music.utils.TextUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Pascal Pothmann on 27.02.2017.
 */
public class StatusCommand extends GuildCommand {
    private static SystemInfo si = new SystemInfo();
    private static HardwareAbstractionLayer hal = si.getHardware();
    private static OperatingSystem os = si.getOperatingSystem();
    private static GlobalMemory memory = hal.getMemory();
    private static Sensors sensors = hal.getSensors();

    @Override
    public void action(GuildMessageReceivedEvent event, String[] args) {
        User user = event.getAuthor();
        Guild guild = event.getGuild();

        if (!checkPermission(guild, user)) {
            return;
        }

        TextChannel channel = event.getChannel();

        channel.sendTyping().queue();

        List<Guild> allGuilds = Main.getAllGuilds();
        int voiceConnections = 0;

        HashMap<String, User> allUsers = new HashMap<>();

        for(Guild myGuild : Main.getAllGuilds()) {
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
        eb.addField("Nachrichten", MessageListener.TOTAL_MESSAGES.toString(), true);
        eb.addField("Befehle", MessageListener.TOTAL_COMMANDS.toString(), true);
        eb.addField("Audio Connections", voiceConnections+"", true);
        eb.addField("CPU", getCpuInfo() + "\n" + getSystemCpuLoad(), true);
        long totalMemory = getTotalMemoryInBytes();
        long freeMemory = getFreeMemoryInBytes();
        eb.addField("CPU Temperatur", String.format("%.1f°C", getCpuTemperature()), true);
        eb.addField("Threads", getThreadCount() + "", true);
        eb.addField("Prozesse", getProcessCount() + "", true);
        eb.addField("RAM", "[" + FormatUtil.formatBytes(totalMemory-freeMemory) + "/"
                + FormatUtil.formatBytes(totalMemory) + "]", true);
        eb.addField("Uptime", TextUtils.formatMillis(System.currentTimeMillis()-Main.START_TIME, true), true);

        channel.sendMessage(eb.build()).queue(new MessageDeleter());
    }

    public static int getThreadCount() {
        return os.getThreadCount();
    }

    public static int getProcessCount() {
        return os.getProcessCount();
    }

    public static double getCpuTemperature() {
        return sensors.getCpuTemperature();
    }

    public static String getSystemCpuLoad() {
        return String.format("%.1f%%", hal.getProcessor().getSystemCpuLoad() * 100);
    }

    public static long getTotalMemoryInBytes() {
        return memory.getTotal();
    }

    public static long getFreeMemoryInBytes(){
        return memory.getAvailable();
    }

    public static String getCpuInfo(){
        return hal.getProcessor().toString();
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
