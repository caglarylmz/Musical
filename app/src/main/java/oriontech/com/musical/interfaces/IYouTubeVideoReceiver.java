package oriontech.com.musical.interfaces;

import java.util.List;

import oriontech.com.musical.model.YouTubeVideo;

/**
 * Interface which enables passing videos to the fragments
 * Created by Teocci on 10.3.16..
 */
public interface IYouTubeVideoReceiver
{
    void onVideosReceived(List<YouTubeVideo> youTubeVideos, String currentPageToken, String nextPageToken);
}