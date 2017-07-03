package pothi_discord.utils;

import net.dv8tion.jda.core.entities.Guild;
import pothi_discord.Main;

/**
 * Created by Pascal Pothmann on 03.07.2017.
 */
public class ErrorLogger {

    public static void log(String error) {
        for (Guild guild : Main.musicBot.getAllGuilds()) {
            if(guild.getId().equals("273812217445744640")) {
                guild.getTextChannelById("331524199883603968").sendMessage(error).queue();
            }
        }
    }
}
