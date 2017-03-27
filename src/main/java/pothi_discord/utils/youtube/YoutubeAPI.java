package pothi_discord.utils.youtube;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mongodb.morphia.logging.Logger;
import org.mongodb.morphia.logging.MorphiaLoggerFactory;
import pothi_discord.utils.Param;

import java.net.URLEncoder;
import java.util.ArrayList;

public class YoutubeAPI {
    private static final Logger log = MorphiaLoggerFactory.get(YoutubeAPI.class);

    private YoutubeAPI() {
    }

    public static ArrayList<YoutubeVideo> searchForVideos(String query) {
        JSONObject data = null;
        try {
            data = Unirest.get("https://www.googleapis.com/youtube/v3/search?part=id&type=video&maxResults=5&regionCode=US&fields=items(id/videoId)")
                    .queryString("q", URLEncoder.encode(query, "UTF-8"))
                    .queryString("key", Param.GOOGLE_API_KEY())
                    .asJson()
                    .getBody()
                    .getObject();

            ArrayList<YoutubeVideo> vids = new ArrayList<>();

            data.getJSONArray("items").forEach((Object t) -> {
                JSONObject item = (JSONObject) t;
                vids.add(getVideoFromID(item.getJSONObject("id").getString("videoId")));
            });

            return vids;
        } catch (JSONException ex) {
            log.error(data.toString());
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static YoutubeVideo getVideoFromID(String id) {
        JSONObject data = null;
        try {
            data = Unirest.get("https://www.googleapis.com/youtube/v3/videos?part=contentDetails,snippet&fields=items(id,snippet/title,contentDetails/duration)")
                    .queryString("id", id)
                    .queryString("key", Param.GOOGLE_API_KEY())

                    .asJson()
                    .getBody()
                    .getObject();

            YoutubeVideo vid = new YoutubeVideo();
            vid.id = data.getJSONArray("items").getJSONObject(0).getString("id");
            vid.name = data.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getString("title");
            vid.duration = data.getJSONArray("items").getJSONObject(0).getJSONObject("contentDetails").getString("duration");

            return vid;
        } catch (JSONException ex) {
            log.error(data.toString());
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static YoutubeVideo getVideoFromID(String id, boolean verbose) {
        if(verbose){
            JSONObject data = null;
            try {
                data = Unirest.get("https://www.googleapis.com/youtube/v3/videos?part=contentDetails,snippet")
                        .queryString("id", id)
                        .queryString("key", Param.GOOGLE_API_KEY())
                        .asJson()
                        .getBody()
                        .getObject();

                YoutubeVideo vid = new YoutubeVideo();
                vid.id = data.getJSONArray("items").getJSONObject(0).getString("id");
                vid.name = data.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getString("title");
                vid.duration = data.getJSONArray("items").getJSONObject(0).getJSONObject("contentDetails").getString("duration");
                vid.description = data.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getString("description");
                vid.channelId = data.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getString("channelId");
                vid.channelTitle = data.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getString("channelTitle");

                return vid;
            } catch (JSONException ex) {
                log.error(data.toString());
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            return getVideoFromID(id);
        }
    }

    public static ArrayList<YoutubePlaylist> getPlaylistsFromChannel(String channelId) {
        return getPlaylistsFromChannel(channelId, Integer.MAX_VALUE);
    }
    public static ArrayList<YoutubePlaylist> getPlaylistsFromChannel(String channelId, int max) {
        ArrayList<YoutubePlaylist> result = new ArrayList<>();
        int videoCount = 0;

        JSONObject data = null;

        try {
            data = Unirest.get("https://www.googleapis.com/youtube/v3/playlists?part=contentDetails,id,snippet")
                    .queryString("channelId", channelId)
                    .queryString("maxResults", 50)
                    .queryString("key", Param.GOOGLE_API_KEY())
                    .asJson()
                    .getBody()
                    .getObject();
            try {
                ArrayList<YoutubePlaylist> list = iterateOverResultPlaylists(data.getJSONArray("items"));
                result.addAll(list);

                for (YoutubePlaylist tmp : list) {
                    videoCount += tmp.itemCount;
                }
            } catch (JSONException e) {
                log.error("Error: ", e);
                log.error(data.toString());
            }

        } catch (UnirestException e) {
            throw new RuntimeException("Could not load Data from Youtube.");
        }

        while (data.has("nextPageToken") && videoCount > max) {
            String pageToken = data.getString("nextPageToken");

            try {
                data = Unirest.get("https://www.googleapis.com/youtube/v3/playlists?part=contentDetails,id,snippet")
                        .queryString("pageToken", pageToken)
                        .queryString("channelId", channelId)
                        .queryString("maxResults", 50)
                        .queryString("key", Param.GOOGLE_API_KEY())
                        .asJson()
                        .getBody()
                        .getObject();
                try {
                    ArrayList<YoutubePlaylist> list = iterateOverResultPlaylists(data.getJSONArray("items"));
                    result.addAll(list);

                    for (YoutubePlaylist tmp : list) {
                        videoCount += tmp.itemCount;
                    }
                } catch (JSONException e) {
                    log.error("Error: ", e);
                    log.error(data.toString());
                }
            } catch (UnirestException e) {
                throw new RuntimeException("Could not load Data from Youtube.");
            }
        }
        return result;
    }

    private static ArrayList<YoutubePlaylist> iterateOverResultPlaylists(JSONArray arr) {
        ArrayList<YoutubePlaylist> result = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            YoutubePlaylist playlist = new YoutubePlaylist();

            playlist.id = obj.getString("id");
            playlist.title = obj.getJSONObject("snippet").getString("title");
            playlist.description = obj.getJSONObject("snippet").getString("description");
            playlist.itemCount = obj.getJSONObject("contentDetails").getInt("itemCount");

            result.add(playlist);
        }
        return result;
    }


    public static ArrayList<YoutubeVideo> getVideosFromPlaylist(YoutubePlaylist playlist) {
        return getVideosFromPlaylist(playlist.id, Integer.MAX_VALUE);
    }

    public static ArrayList<YoutubeVideo> getVideosFromPlaylist(YoutubePlaylist playlist, int estimatedSize) {
        return getVideosFromPlaylist(playlist.id, estimatedSize);
    }

    public static ArrayList<YoutubeVideo> getVideosFromPlaylist(String playlistId) {
        return getVideosFromPlaylist(playlistId, Integer.MAX_VALUE);
    }

    public static ArrayList<YoutubeVideo> getVideosFromPlaylist(String playlistId, int estimatedSize) {
        ArrayList<YoutubeVideo> result = new ArrayList<>();

        JSONObject data = null;

        try {
            data = Unirest.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet")
                    .queryString("playlistId", playlistId)
                    .queryString("maxResults", 50)
                    .queryString("key", Param.GOOGLE_API_KEY())
                    .asJson()
                    .getBody()
                    .getObject();

        } catch (UnirestException e) {
            throw new RuntimeException("Could not load Data from Youtube.");
        }

        try {
            result.addAll(iterateOverResultVideos(data.getJSONArray("items")));
        } catch (JSONException e) {
            log.error("Error: ", e);
            log.error(data.toString());
        }

        while (data.has("nextPageToken") && (estimatedSize > result.size())) {
            String pageToken = data.getString("nextPageToken");

            try {
                data = Unirest.get("https://www.googleapis.com/youtube/v3/playlistItems?part=snippet")
                        .queryString("playlistId", playlistId)
                        .queryString("maxResults", 50)
                        .queryString("pageToken", pageToken)
                        .queryString("key", Param.GOOGLE_API_KEY())
                        .asJson()
                        .getBody()
                        .getObject();

            } catch (UnirestException e) {
                throw new RuntimeException("Could not load Data from Youtube.");
            }

            try {
                result.addAll(iterateOverResultVideos(data.getJSONArray("items")));
            } catch (JSONException e) {
                log.error("Error: ", e);
                log.error(data.toString());
            }
        }
        return result;
    }

    private static ArrayList<YoutubeVideo> iterateOverResultVideos(JSONArray arr) {
        ArrayList<YoutubeVideo> result = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            YoutubeVideo vid = new YoutubeVideo();

            vid.id = obj.getJSONObject("snippet").getJSONObject("resourceId").getString("videoId");
            vid.name = obj.getJSONObject("snippet").getString("title");
            vid.duration = null;
            vid.description = obj.getJSONObject("snippet").getString("description");
            vid.channelId = obj.getJSONObject("snippet").getString("channelId");
            vid.channelTitle = obj.getJSONObject("snippet").getString("channelTitle");

            result.add(vid);
        }
        return result;
    }

}