package oriontech.com.musical.interfaces;


import java.util.List;

import oriontech.com.musical.model.YouTubeVideo;

public interface IYouTubeVideoUpdateListener
{
    void onYouTubeVideoChanged(YouTubeVideo youTubeVideo);

    void onYouTubeVideoRetrieveError();

    void onCurrentQueueIndexUpdated(int queueIndex);

    void onQueueUpdated(String title, List<YouTubeVideo> newQueue);
}