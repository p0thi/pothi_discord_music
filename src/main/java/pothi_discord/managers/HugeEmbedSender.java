package pothi_discord.managers;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import pothi_discord.handlers.MessageDeleter;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Pascal Pothmann on 06.04.2017.
 */
public class HugeEmbedSender {
    private MessageEmbed coreEmbed;
    private ArrayList<MessageEmbed> allEmbeds = new ArrayList<>();
    private EmbedBuilder buffer;

    public HugeEmbedSender(MessageEmbed coreEmbed){
        this.coreEmbed = coreEmbed;
        buffer = new EmbedBuilder(coreEmbed);
        if (buffer.isEmpty() || !coreEmbed.isSendable(AccountType.BOT)) {
            throw new RuntimeException("CoreEmbed not valid");
        }
    }

    public void addField(String name, String content, boolean inline) {
        MessageEmbed tmpEmbed = buffer.build();
        if (tmpEmbed.getLength() + name.length() + content.length() > 4000) {
            allEmbeds.add(tmpEmbed);
            buffer = new EmbedBuilder(coreEmbed);
        }

        buffer.addField(name, content, inline);
    }

    public void send(TextChannel channel) {
        allEmbeds.add(buffer.build());

        for (MessageEmbed embed : allEmbeds) {
            channel.sendMessage(embed).queue(new MessageDeleter());
        }
    }
}
