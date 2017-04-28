package pothi_discord.bots.music.commands.fun;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.permissions.PermissionManager;

import java.util.List;

/**
 * Created by Pascal Pothmann on 28.04.2017.
 */
public class ReactCommand extends GuildCommand{
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard botShard) {

        if (!checkPermission(event)){
            return;
        }

        Message message = event.getMessage();
        TextChannel channel = event.getChannel();
        User user = event.getAuthor();
        Guild guild = event.getGuild();
        List<Emote> emotes = message.getEmotes();

        List<Message> allMessages = channel.getHistoryAround(message, 30).complete().getRetrievedHistory();

        Message messageToEdit = null;
        boolean messageIdSubmitted = false;

        try {
            messageToEdit = channel.getMessageById(args[1]).complete();
            if (messageToEdit != null) {
                messageIdSubmitted = true;
            }
        } catch (Exception e) {
            for (Message msg : allMessages) {
                if (msg.getId().equals(message.getId())) {
                    continue;
                }

                if (msg.getAuthor().getId().equals(user.getId())) {
                    messageToEdit = msg;
                    break;
                }
            }
        }

        if (messageToEdit == null) {
            channel.sendMessage("Es wurde keine passende Nachricht zum reagieren gefunden.")
                    .queue(new MessageDeleter());
            return;
        }

        if (!messageToEdit.getAuthor().getId().equals(user.getId())) {
            if (!PermissionManager.checkUserPermission(guild, user, "manage-reactions")) {
                channel.sendMessage("Du darfst nur auf eigene Nachrichten reagieren lassen.")
                        .queue(new MessageDeleter());
                return;
            }
        }

        if (args.length > (messageIdSubmitted ? 3 : 2)) {
            channel.sendMessage("Es darf nur ein Emote auf einmal übermittelt werden.")
                    .queue(new MessageDeleter());
            return;
        }

        message.delete().queue();

        List<MessageReaction> reactions = messageToEdit.getReactions();

        boolean alreadyReacted = false;
        MessageReaction foundReaction = null;

        if (emotes.size() != 0) {
            Emote emote = emotes.get(0);

            for (MessageReaction reaction : reactions) {
                if (reaction.getEmote().isEmote()
                    && reaction.getEmote().getId().equals(emote.getId())) {

                    foundReaction = reaction;
                    alreadyReacted = true;
                    break;
                }
            }

            if (alreadyReacted) {
                foundReaction.removeReaction().queue();
            }
            else {
                messageToEdit.addReaction(emote).queue();
            }
        }
        else {
            String emote = args[
                    messageIdSubmitted ? 2 : 1
                    ];

            if (emote.equals("clear")) {
                messageToEdit.clearReactions().queue();
                return;
            }

            for (MessageReaction reaction : reactions) {
                if (!reaction.getEmote().isEmote()
                    && reaction.getEmote().getName().equals(emote)) {

                    foundReaction = reaction;
                    alreadyReacted = true;
                    break;
                }
            }

            if (alreadyReacted) {
                foundReaction.removeReaction().queue();
            }
            else {
                messageToEdit.addReaction(emote).queue();
            }
        }
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }
}
