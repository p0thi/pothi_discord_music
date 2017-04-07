package pothi_discord.bots.music.commands.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pothi_discord.bots.BotShard;
import pothi_discord.commands.GuildCommand;
import pothi_discord.handlers.MessageDeleter;
import pothi_discord.managers.MessageManager;
import pothi_discord.utils.Param;
import pothi_discord.utils.TextUtils;
import pothi_discord.utils.database.morphia.userdata.UserAudioTrack;
import pothi_discord.utils.database.morphia.userdata.UserPlaylist;
import pothi_discord.utils.database.morphia.userdata.Userdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Pascal Pothmann on 06.04.2017.
 */
public class PlaylistCommand extends GuildCommand {

    private static final int MAX_PLAYLIST_LENGTH = 150;
    private static final int MAX_PLAYLIST_AMOUNT = 5;
    private static final int MAX_PLAYLIST_NAME_LENGTH = 20;


    @Override
    public void action(GuildMessageReceivedEvent event, String[] args, BotShard botShard) {

        if (!checkPermission(event)) {
            return;
        }

        TextChannel channel = event.getChannel();
        User user = event.getAuthor();
        Userdata userdata = Userdata.getUserdata(user.getId());

        if (args.length <= 1) {
            if (userdata.getPlaylists().size() <= 0){
                channel.sendMessage("Keine Playlists vorhanden").queue(new MessageDeleter());
            }
            else {
                MessageManager mm = new MessageManager();
                mm.append("Hier sind alle Deine Playlists:\n\n");

                List<UserPlaylist> userPlaylists = userdata.getPlaylists();

                for (int i = 0; i < userPlaylists.size(); i++) {
                    UserPlaylist tmp = userPlaylists.get(i);
                    mm.append(String.format("%d.  **%s**   (%d Einträge)\n",
                            i + 1,
                            tmp.getName(),
                            tmp.getTracks().size()));
                }

                for (String messageString : mm.complete()) {
                    channel.sendMessage(messageString).queue(new MessageDeleter());
                }
            }


            return;
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

            String identifier = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();

            if (identifier.length() > MAX_PLAYLIST_NAME_LENGTH) {
                channel.sendMessage(String.format("Der Name der Playlist ist zu lang. Maximal %d Zeichen",
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
        else if (args[1].matches("\\d+")){

            int playlistIndex = Integer.parseInt(args[1]) - 1;

            if (playlistIndex < 0
                    || userdata.getPlaylists().size() - 1 < playlistIndex) {
                channel.sendMessage(String.format("Die Playlist Nr. **%d** existiert nicht. ", playlistIndex + 1))
                        .queue(new MessageDeleter());
                return;
            }

            UserPlaylist userPlaylist = userdata.getPlaylists().get(playlistIndex);

            boolean hasIdentifier = false;
            String identifier = null;

            if (args.length > 3) {
                hasIdentifier = true;
                identifier = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            }

            if (args.length <= 2) {
                //show selected playlist
                MessageManager mm = new MessageManager();
                mm.append(String.format("Hier die Playlist **%s**:\n", userPlaylist.getName()));

                List<UserAudioTrack> allPlaylistTracks = userPlaylist.getTracks();

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
                                channel.sendMessage("Das konnte nicht hinzugefügt werden. " +
                                        "Bitte direkt einen Link angeben.").queue(new MessageDeleter());
                                return;
                            }

                            if (userPlaylist.getTracks().size() + results.size() <= MAX_PLAYLIST_LENGTH) {
                                for (AudioTrack audioTrack : results) {
                                    userPlaylist.getTracks().add(UserAudioTrack.convertAudioTrack(audioTrack));
                                }
                                userPlaylist.saveInstance();

                                channel.sendMessage(String.format("Erfolgreich %d Einträge hinzugefügt.",
                                        results.size()))
                                        .queue(new MessageDeleter());
                            }
                            else {
                                if (userPlaylist.getTracks().size() >= 150) {
                                    channel.sendMessage(String.format("Die Playlist ist voll. Maximal %d Einträge.", MAX_PLAYLIST_LENGTH))
                                            .queue(new MessageDeleter());
                                }
                                else {
                                    channel.sendMessage(String.format("Es können keine %d Einträge hinzugefügt werden." +
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
                        for (UserAudioTrack userAudioTrack : userPlaylist.getTracks()) {
                            titles.add(userAudioTrack.getTitle());
                        }

                        List<String> tmpResults = new ArrayList<>();

                        for (String title : titles) {
                            if (FuzzySearch.partialRatio(identifier, title) >= 70) {
                                tmpResults.add(title);
                            }
                        }

                        MessageManager mm = new MessageManager();
                        mm.append("Alle Einträge, die zu deiner Suchanfrage gefunden wurden:\n\n");

                        for (String title : tmpResults) {
                            int index = userPlaylist.getIndexOfExactTitle(title);
                            UserAudioTrack track = userPlaylist.getTracks().get(index);
                            mm.append(String.format("%d.  **%s**\n\t\tLink: <%s>\n\n",
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
                        if (identifier.trim().matches("\\d+")) {
                            int trackIndex = Integer.parseInt(identifier.trim()) - 1;

                            if (trackIndex < 0 || trackIndex >= userPlaylist.getTracks().size()) {
                                channel.sendMessage(String.format("Es existiert kein Eintrag an der Stelle %s.",
                                        identifier))
                                        .queue(new MessageDeleter());
                                return;
                            }

                            UserAudioTrack trackToRemove = userPlaylist.getTracks().get(trackIndex);
                            userPlaylist.getTracks().remove(trackIndex);

                            userPlaylist.saveInstance();

                            channel.sendMessage(String.format("Der Eintrag **%s** wurde erfolgreich gelöscht.",
                                    trackToRemove.getTitle()))
                                    .queue(new MessageDeleter());
                        }
                        else {
                            channel.sendMessage(String.format("Mit **%splaylist <Nummer1> remove <Nummer2>** " +
                                    "kannst du den Eintrag mit der Nummer <Nummer2> aus der Playlist mit der Nummer " +
                                    "<Nummer1> entfernen.",
                                    Param.PREFIX()))
                                    .queue(new MessageDeleter());
                        }
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
            }
        }
        else {
            channel.sendMessage("Ungültiges zweites Argument").queue(new MessageDeleter());
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
