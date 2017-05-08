package pothi_discord.bots.music.commands.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.managers.MessageManager;
import pothi_discord.utils.Param;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.morphia.MongoAudioTrack;
import pothi_discord.utils.database.morphia.userdata.UserPlaylist;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pascal Pothmann on 06.04.2017.
 */
public class PlaylistCommand extends GuildCommand {

    private static final int MAX_PLAYLIST_LENGTH = 75;
    private static final int MAX_PLAYLIST_AMOUNT = 5;
    private static final int MAX_PLAYLIST_NAME_LENGTH = 25;
    private static final int MIN_PLAYLIST_NAME_LENGTH = 4;
    private static final Pattern PLAYLIST_NAME_PATTERN = Pattern.compile("^.{" + MIN_PLAYLIST_NAME_LENGTH
            + "," + MAX_PLAYLIST_NAME_LENGTH + "}$");


    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard botShard) {
        TextChannel channel = event.getChannel();

        channel.sendTyping();

        if (!checkPermission(event)) {
            return;
        }

        User user = event.getAuthor();
        Userdata userdata = Userdata.getUserdata(user.getId());

        if (args.length <= 1) {
            MessageManager mm = new MessageManager();
            if (userdata.getPlaylists().size() <= 0){
                mm.append("Du hast keine eigenen Playlists.");
            }
            else {
                mm.append("__**" + "Hier sind alle Deine Playlists:" + "**__" + "\n\n");

                List<UserPlaylist> userPlaylists = userdata.getPlaylists();

                UserPlaylist activePlaylist = userdata.getActivePlaylist();

                for (int i = 0; i < userPlaylists.size(); i++) {
                    UserPlaylist tmp = userPlaylists.get(i);

                    boolean isActive = activePlaylist != null && activePlaylist.getId().toHexString()
                            .equals(tmp.getId().toHexString());

                    mm.append(String.format("%d.  **%s**   (%d Einträge) %s\n",
                            i + 1,
                            tmp.getName(),
                            tmp.getTracks().size(),
                            isActive ? "   :white_check_mark: *Active*" : ""));
                }
            }


            VoiceChannel botVoiceChannel = event.getGuild().getAudioManager().getConnectedChannel();

            if (botVoiceChannel != null) {
                mm.append("\n\n" + "__**" + "Momentan aktive Playlists in diesem Channel:" + "**__" + "\n\n");
                for (Member member : botVoiceChannel.getMembers()) {
                    User myUser = member.getUser();
                    if (myUser.equals(botShard.getJDA().getSelfUser().getId())) {
                        continue;
                    }

                    Userdata memberdata = Userdata.getUserdata(myUser.getId());
                    UserPlaylist myUserPlaylist = memberdata.getActivePlaylist();

                    if (myUserPlaylist == null) {
                        continue;
                    }
                    mm.append(String.format("**%s** von %s (%d Einträge).",
                            myUserPlaylist.getName(),
                            member.getEffectiveName(),
                            myUserPlaylist.getTracks().size()) + "\n");
                }
            }

            for (String messageString : mm.complete()) {
                channel.sendMessage(messageString).queue(new MessageDeleter());
            }

            return;
        }
        boolean hasIdentifier = false;
        String identifier = null;

        if (args.length > 3) {
            hasIdentifier = true;
            identifier = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        }

        if (args[1].toLowerCase().equals("create")) {
            if (userdata.getPlaylists().size() >= MAX_PLAYLIST_AMOUNT) {
                channel.sendMessage(String.format("Du kannst keine weiteren Playlists mehr erstellen. " +
                        "(Maximal %d Playlists)", MAX_PLAYLIST_AMOUNT)).queue(new MessageDeleter());
                return;
            }

            if (args.length < 3) {
                channel.sendMessage(String.format("Du muss einen Namen für die Playlist angeben.\n" +
                        "(%splaylist create <Name>)",
                        Param.PREFIX()))
                        .queue(new MessageDeleter());
                return;
            }

            Matcher matcher = PLAYLIST_NAME_PATTERN.matcher(identifier);
            if (matcher.find()) {
                channel.sendMessage(String.format("Der Name der Playlist ist zu lang. Zwischen %d und %d Zeichen.",
                        MIN_PLAYLIST_NAME_LENGTH,
                        MAX_PLAYLIST_NAME_LENGTH))
                        .queue(new MessageDeleter());
                return;
            }

            UserPlaylist newPlaylist = new UserPlaylist();
            newPlaylist.setName(identifier);
            userdata.getPlaylists().add(newPlaylist);

            newPlaylist.saveInstance();
            userdata.saveInstance();

            channel.sendMessage(String.format("Playlist **%s** erfolgreich erstellt.",
                    identifier))
                    .queue(new MessageDeleter());
        }
        else if (args[1].toLowerCase().equals("deactivate")) {
            userdata.setActivePlaylist(null);
            userdata.saveInstance();

            channel.sendMessage("Alle Playlists sind jetzt deaktiviert.").queue(new MessageDeleter());
        }
        else if (args[1].matches("\\d+")){

            int playlistIndex = Integer.parseInt(args[1]) - 1;

            if (playlistIndex < 0
                    || userdata.getPlaylists().size() - 1 < playlistIndex) {
                channel.sendMessage(String.format("Die Playlist Nr. **%d** existiert nicht. ", playlistIndex + 1))
                        .queue(new MessageDeleter());
                return;
            }

            UserPlaylist userPlaylist = userdata.getPlaylists().get(playlistIndex);



            if (args.length <= 2) {
                //show selected playlist
                MessageManager mm = new MessageManager();
                mm.append(String.format("Hier die Playlist **%s**:\n", userPlaylist.getName()));

                List<MongoAudioTrack> allPlaylistTracks = userPlaylist.getTracks();

                for (int i = 0; i < allPlaylistTracks.size(); i++) {
                    mm.append((i + 1) + ". **" + allPlaylistTracks.get(i).getTitle() + "** (" +
                            TextUtils.getMillisFormattedMS(allPlaylistTracks.get(i).getLength()) + ")\n");
                }

                for (String messageString : mm.complete()) {
                    channel.sendMessage(messageString).queue(new MessageDeleter());
                }
                return;
            }

            switch (args[2].toLowerCase().trim()) {
                case "add": {
                    if (hasIdentifier) {
                        AudioLoader loader = new AudioLoader();
                        try {
                            botShard.getPlayerManager().loadItem(identifier, loader).get();
                            ArrayList<AudioTrack> results = loader.tracks;

                            if (results == null) {
                                channel.sendMessage(user.getAsMention()
                                        + " Das konnte nicht hinzugefügt werden. " +
                                        "Bitte direkt einen Link angeben.").queue(new MessageDeleter());
                                return;
                            }

                            if (userPlaylist.getTracks().size() + results.size() <= MAX_PLAYLIST_LENGTH) {
                                for (AudioTrack audioTrack : results) {
                                    userPlaylist.getTracks().add(MongoAudioTrack.convertAudioTrack(audioTrack));
                                }
                                userPlaylist.saveInstance();

                                channel.sendMessage(String.format("Erfolgreich %d Einträge hinzugefügt.%s",
                                        results.size(),
                                        results.size() == 1 ? "\n**" + results.get(0).getInfo().title + "**." : ""))
                                        .queue(new MessageDeleter());
                            }
                            else {
                                if (userPlaylist.getTracks().size() >= 150) {
                                    channel.sendMessage(String.format(user.getAsMention()
                                            + "Die Playlist ist voll. Maximal %d Einträge.", MAX_PLAYLIST_LENGTH))
                                            .queue(new MessageDeleter());
                                }
                                else {
                                    channel.sendMessage(String.format(user.getAsMention()
                                                    + "Es können keine %d Einträge hinzugefügt werden." +
                                            " (Aktuelle Anzahl an Einträgen in der Playlist: %d/%d)",
                                            results.size(),
                                            userPlaylist.getTracks().size(),
                                            MAX_PLAYLIST_LENGTH))
                                            .queue(new MessageDeleter());
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        channel.sendMessage(String.format("Mit **%splaylist <Nummer> add <Link>** kannst du einen " +
                                "Eintrag zu der Playlist mit der Nummer <Nummer> hinzufügen. " +
                                "<Link> ist z.B. ein YouTube Link.",
                                Param.PREFIX()))
                                .queue(new MessageDeleter());
                    }
                    break;
                }
                case "search": {
                    if (hasIdentifier) {
                        ArrayList<String> titles = new ArrayList<>();
                        for (MongoAudioTrack mongoAudioTrack : userPlaylist.getTracks()) {
                            titles.add(mongoAudioTrack.getTitle());
                        }

                        List<String> tmpResults = new ArrayList<>();

                        for (String title : titles) {
                            if (FuzzySearch.partialRatio(identifier, title) >= 70) {
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
                            int index = userPlaylist.getIndexOfExactTitle(title);
                            MongoAudioTrack track = userPlaylist.getTracks().get(index);
                            mm.append(String.format("%d.  **%s**\n\t\tLink: <%s>\n",
                                    (index + 1),
                                    title,
                                    track.getUri()));
                        }

                        for (String message : mm.complete()) {
                            channel.sendMessage(message).queue(new MessageDeleter());
                        }
                    }
                    else {
                        channel.sendMessage(String.format("Mit **%splaylist <Nummer> search <Begriff>** kannst du " +
                                "Einträge in der Playlist mit der Nummer <Nummer> anzeigen lassen, die zu dem " +
                                "Suchbegriff <Begriff> passen.",
                                Param.PREFIX()))
                                .queue();
                    }

                    break;
                }
                case "remove": {
                    if (hasIdentifier) {
                        ArrayList<String> parts = new ArrayList<>(Arrays
                                .asList(identifier.split(" ")));

                        ArrayList<MongoAudioTrack> toRemove = new ArrayList<>();

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
                                        if (!toRemove.contains(x)){
                                            toRemove.add(userPlaylist.getTracks().get(x));
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
                                    if (!toRemove.contains(index)) {
                                        toRemove.add(userPlaylist.getTracks().get(index));
                                    }
                                } catch (Exception e) {
                                    // Silent
                                }
                            }
                        }

                        ArrayList<MongoAudioTrack> removed = new ArrayList<>();

                        for (int i = 0; i < toRemove.size(); i++){

                            MongoAudioTrack old = toRemove.get(i);
                            userPlaylist.getTracks().remove(old);
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

                        userPlaylist.saveInstance();
                    }
                    else {
                        channel.sendMessage(String.format("Mit **%splaylist <Nummer1> remove <Nummer2>** " +
                                "kannst du den Eintrag mit der Nummer <Nummer2> aus der Playlist mit der Nummer " +
                                "<Nummer1> entfernen.",
                                Param.PREFIX()))
                                .queue(new MessageDeleter());
                    }
                    break;
                }
                case "delete": {
                    break;
                }
                case "active":
                case "activate": {
                    UserPlaylist old = userdata.getActivePlaylist();
                    userdata.setActivePlaylist(userPlaylist);
                    userdata.saveInstance();
                    if (old != null) {
                        channel.sendMessage(String.format("Die Playlist **%s** ist jetzt dekativiert.",
                                old.getName()))
                                .queue(new MessageDeleter());
                    }
                    channel.sendMessage(String.format("Die Playlist **%s** ist jetzt aktiviert.",
                            userPlaylist.getName()))
                            .queue(new MessageDeleter());
                    break;
                }
                case "deactive":
                case "deactivate": {
                    userdata.setActivePlaylist(null);
                    userdata.saveInstance();

                    channel.sendMessage("Alle Playlists sind jetzt deaktiviert.").queue(new MessageDeleter());
                    break;
                }
                case "rename": {
                    if (hasIdentifier) {
                        Matcher matcher = PLAYLIST_NAME_PATTERN.matcher(identifier);
                        if (matcher.find()) {
                            String old = userPlaylist.getName();
                            userPlaylist.setName(identifier);
                            userPlaylist.saveInstance();

                            channel.sendMessage(String.format("Die Playlist **%s** wurde erfolgreich " +
                                    "in **%s** umbenannt.",
                                    old,
                                    userPlaylist.getName()))
                                    .queue(new MessageDeleter());
                        }
                        else {
                            channel.sendMessage(String.format("Der Name ist ungültig. Zwischen %d und %d Zeichen.",
                                    MIN_PLAYLIST_NAME_LENGTH,
                                    MAX_PLAYLIST_NAME_LENGTH))
                                    .queue(new MessageDeleter());
                        }
                    }
                    else {
                        //TODO description message
                    }
                    break;
                }
                //TODO clear
                //TODO rename
            }
        }
        else {
            channel.sendMessage(user.getAsMention()
                    + "Ungültiges zweites Argument.").queue(new MessageDeleter());
        }

    }

    @Override
    public void prepare() {

    }

    @Override
    public String helpString() {
        return null;
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
