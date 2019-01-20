package oriontech.com.musical.interfaces;

import java.util.List;

import oriontech.com.musical.model.YouTubePlaylist;
import oriontech.com.musical.model.YouTubeVideo;

public interface IYouTubePlaylistReceiver
{
    void onPlaylistReceived(List<YouTubePlaylist> youTubePlaylistList);

    void onPlaylistNotFound(String playlistId, int errorCode);

    void onPlaylistVideoReceived(List<YouTubeVideo> youTubeVideos);
}