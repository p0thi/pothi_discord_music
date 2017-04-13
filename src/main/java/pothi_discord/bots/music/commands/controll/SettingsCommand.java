package pothi_discord.bots.music.commands.controll;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.managers.MessageManager;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.morphia.MongoAudioTrack;
import pothi_discord.utils.database.morphia.autoplaylists.AutoPlaylist;
import pothi_discord.utils.database.morphia.guilddatas.GuildData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Pascal Pothmann on 09.04.2017.
 */
public class SettingsCommand extends GuildCommand {
    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard botShard) {

        if (!checkPermission(event)) {
            return;
        }

        if (args.length <= 1) {
            //TODO help?
            return;
        }

        switch (args[1].trim().toLowerCase()) {
            case "playlist":
                playlist(event, args, botShard);
                break;
            default:
                //TODO
                break;
        }
    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
    }

    private void playlist(GuildMessageReceivedEvent event, String[] args, BotShard botShard) {
        Guild guild = event.getGuild();
        TextChannel channel = event.getChannel();

        GuildData guildData = GuildData.getGuildDataByGuildId(guild.getId());
        AutoPlaylist autoPlaylist = guildData.getAutoplaylist();

        // shows the playlist
        if (args.length <= 2) {
            MessageManager mm = new MessageManager();

            mm.append(String.format("Die Autoplaylist **%s** für diesen Server:\n\n", autoPlaylist.getTitle()));

            int position = 0;
            for (MongoAudioTrack track : autoPlaylist.getContent()) {
                position++;
                mm.append(position + "  **" + track.getTitle() + "** _("
                        + TextUtils.getMillisFormattedMS(track.getLength()) + ")\n");
            }

            for (String elem : mm.complete()) {
                channel.sendTyping().queue();
                channel.sendMessage(elem).queue(new MessageDeleter());
            }

            return;
        }

        String identifier = "";
        boolean hasIdentifier = false;
        if (args.length > 3) {
            identifier = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            hasIdentifier = true;
        }

        switch (args[2].trim().toLowerCase()) {
            case "add": {
                if (args.length <= 3) {
                    //TODO description message
                    return;
                }

                AudioLoader loader = new AudioLoader();
                try {
                    botShard.getPlayerManager().loadItem(args[3], loader).get();

                    if (loader.tracks != null) {
                        for (AudioTrack track : loader.tracks) {
                            autoPlaylist.getContent().add(MongoAudioTrack.convertAudioTrack(track));
                        }

                        autoPlaylist.saveInstance();

                        channel.sendMessage(String.format("Erfolgreich %d Einträge zu **%s** hinzugefügt.%s",
                                loader.tracks.size(),
                                autoPlaylist.getTitle(),
                                loader.tracks.size() == 1 ? "\n_(" + loader.tracks.get(0).getInfo().title + ")_" : ""))
                                .queue(new MessageDeleter());
                    } else {
                        channel.sendMessage("Das kann nicht hinzugefügt werden.").queue(new MessageDeleter());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                break;
            }
            case "remove": {
                if (!hasIdentifier) {
                    //TODO description message
                    return;
                }

                ArrayList<String> parts = new ArrayList<>(Arrays
                        .asList(identifier.split(" ")));

                ArrayList<MongoAudioTrack> indexes = new ArrayList<>();

                for (int i = 0; i < parts.size(); i++) {
                    try {
                        if (parts.get(i).equals("to")) {
                            String last = parts.get(i - 1);
                            String next = parts.get(i + 1);

                            int lastInt = Integer.parseInt(last);
                            int nextInt = Integer.parseInt(next);

                            if (lastInt >= nextInt) {
                                throw new Exception("The first number has to be smaller then the second.");
                            }

                            for (int x = lastInt; x <= nextInt; x++) {
                                if (!indexes.contains(x)) {
                                    indexes.add(autoPlaylist.getContent().get(x));
                                }
                            }
                        }

                    } catch (Exception e) {
                        // Silent
                    }
                }

                for (int i = 0; i < parts.size(); i++) {
                    int indexBefore = Math.max(0, i);
                    int indexAfter = Math.min(i, parts.size() - 1);

                    if (!(parts.get(indexBefore).equals("to") || parts.get(indexAfter).equals("to"))) {
                        try {
                            int index = Integer.parseInt(parts.get(i)) - 1;
                            if (!indexes.contains(index)) {
                                indexes.add(autoPlaylist.getContent().get(index));
                            }
                        } catch (Exception e) {
                            // Silent
                        }
                    }
                }

                ArrayList<MongoAudioTrack> removed = new ArrayList<>();
                for (int i = 0; i < indexes.size(); i++) {

                    MongoAudioTrack old = indexes.get(i);
                    autoPlaylist.getContent().remove(old);
                    if (!removed.contains(old))
                        removed.add(old);
                    System.out.println("removes " + old.getTitle());
                }

                if (removed.size() == 0) {
                    channel.sendMessage("Es wurden keine Einträge gelöscht").queue(new MessageDeleter());
                }

                MessageManager mm = new MessageManager();
                mm.append(String.format("Erfolgreich **%d** Einträge aus der Autoplaylist entfernt.", removed.size()));

                if (removed.size() == 1) {
                    mm.append("\n**" + removed.get(0).getTitle() + "**");
                }

                for (String message : mm.complete()) {
                    channel.sendMessage(message).queue(new MessageDeleter());
                }

                autoPlaylist.saveInstance();

                break;
            }
            case "search": {

                if (!hasIdentifier) {
                    //TODO send explaination
                    return;
                }

                ArrayList<String> titles = new ArrayList<>();
                for (MongoAudioTrack mongoAudioTrack : autoPlaylist.getContent()) {
                    titles.add(mongoAudioTrack.getTitle());
                }

                List<String> tmpResults = new ArrayList<>();

                for (String title : titles) {
                    if (FuzzySearch.partialRatio(identifier, title) >= 70) {
                        System.out.println(title);
                        tmpResults.add(title);
                    }
                }

                if (tmpResults.size() <= 0) {
                    channel.sendMessage("Zu Deiner Suchanfrage wurde nichts gefunden.")
                            .queue(new MessageDeleter());
                    return;
                }

                MessageManager mm = new MessageManager();
                mm.append("Alle Einträge, die zu deiner Suchanfrage gefunden wurden:\n\n");

                for (String title : tmpResults) {
                    int index = autoPlaylist.getIndexOfExactTitle(title);
                    MongoAudioTrack track = autoPlaylist.getContent().get(index);
                    mm.append(String.format("%d.  **%s**\n\t\tLink: <%s>\n",
                            (index + 1),
                            title,
                            track.getUri()));
                }

                for (String message : mm.complete()) {
                    channel.sendMessage(message).queue(new MessageDeleter());
                }
                //TODO send explaination
            }
            break;
        }

    }

    private class AudioLoader implements AudioLoadResultHandler {
        ArrayList<AudioTrack> tracks = null;
        int resultCode = 0;

        @Override
        public void trackLoaded(AudioTrack track) {
            this.tracks = new ArrayList<>();
            this.resultCode = 1;
            this.tracks.add(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            this.tracks = new ArrayList<>();
            this.resultCode = 2;
            this.tracks.addAll(playlist.getTracks());
        }

        @Override
        public void noMatches() {
            this.resultCode = 3;
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            this.resultCode = 4;
        }
    }
}
