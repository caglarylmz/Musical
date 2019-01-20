package oriontech.com.musical.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import oriontech.com.musical.R;
import oriontech.com.musical.adapters.VideosAdapter;
import oriontech.com.musical.interfaces.IOnLoadMoreListener;
import oriontech.com.musical.interfaces.IYouTubeVideoReceiver;
import oriontech.com.musical.model.YouTubeVideo;
import oriontech.com.musical.services.BackgroundExoAudioService;
import oriontech.com.musical.utils.Config;
import oriontech.com.musical.utils.DividerDecoration;
import oriontech.com.musical.utils.LogHelper;
import oriontech.com.musical.utils.YouTubeSqlDb;
import oriontech.com.musical.youtube.YouTubeVideoLoader;

import static oriontech.com.musical.utils.Config.MAX_VIDEOS_RETURNED;

public class SearchFragment extends RecyclerFragment implements IYouTubeVideoReceiver, IOnLoadMoreListener
{
    private static final String TAG = LogHelper.makeLogTag(SearchFragment.class);

    private Handler handler;
    private ProgressBar loadingProgressBar;

    private String currentQuery;
    private String nextPageToken;
    private int visibleThreshold = 4;
    private int lastVisibleItem, totalItemCount;

    private boolean isLoading;

    public static SearchFragment newInstance()
    {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        isLoading = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        loadingProgressBar = rootView.findViewById(R.id.progressBar);

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                if (totalItemCount > MAX_VIDEOS_RETURNED - visibleThreshold) {
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
//                Log.e(TAG, "totalItemCount: " + totalItemCount + " lastVisibleItem: " + lastVisibleItem);
                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        videoListAdapter.onItemHolderOnLoadMore();
                        isLoading = true;
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager()
    {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration()
    {
        //We must draw dividers ourselves if we want them in a list
        return new DividerDecoration(getActivity());
    }

    @Override
    protected VideosAdapter getAdapter()
    {
        return new VideosAdapter(getActivity(), false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        // Check network connectivity
        if (!networkConf.isNetworkAvailable(getActivity())) {
            networkConf.createNetErrorDialog();
            return;
        }

        Toast.makeText(
                getContext(),
                getResources().getString(R.string.toast_message_loading),
                Toast.LENGTH_SHORT
        ).show();

        YouTubeSqlDb.getInstance().videos(YouTubeSqlDb.VIDEOS_TYPE.RECENTLY_WATCHED)
                .create(videoListAdapter.getYouTubeVideo(position));

        Intent serviceIntent = new Intent(getContext(), BackgroundExoAudioService.class);
        serviceIntent.setAction(Config.ACTION_PLAY);
        serviceIntent.putExtra(Config.KEY_YOUTUBE_TYPE, Config.YOUTUBE_MEDIA_TYPE_VIDEO);
        serviceIntent.putExtra(Config.KEY_YOUTUBE_TYPE_VIDEO, videoListAdapter.getYouTubeVideo(position));
        getActivity().startService(serviceIntent);
    }

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);

        if (visible && isResumed()) {
            // Only manually call onResume if fragment is already visible
            // Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!getUserVisibleHint()) {
            // Do nothing for now
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
//        videosFoundListView = (DynamicListView) getListView();
    }

    /**
     * Search for query on youTube by using YouTube Data API V3
     *
     * @param query the keyword for the search
     */
    public void searchQuery(String query)
    {
        currentQuery = query;
        try {
            // Check network connectivity
            if (!networkConf.isNetworkAvailable(getActivity())) {
                networkConf.createNetErrorDialog();
                return;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        videoListAdapter.clearYouTubeVideos();
        loadingProgressBar.setVisibility(View.VISIBLE);

        YouTubeVideoLoader youTubeVideoLoader = new YouTubeVideoLoader(getActivity());
        youTubeVideoLoader.setYouTubeVideoReceiver(this);
        youTubeVideoLoader.search(currentQuery);
    }

    /**
     * Called when video items are received
     *
     * @param ytVideos         - videos to be shown in list view
     * @param currentPageToken - videos to be shown in list view
     * @param nextPageToken    - videos to be shown in list view
     */
    @Override
    public void onVideosReceived(final List<YouTubeVideo> ytVideos,
                                 final String currentPageToken,
                                 final String nextPageToken)
    {
        if (videoListAdapter != null && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (currentPageToken == null || currentPageToken.equals("")) {
                    Log.e(TAG, "Adding First Page Videos");
                    videoListAdapter.setYouTubeVideos(ytVideos);
                    recyclerView.smoothScrollToPosition(0);
                } else {
                    Log.e(TAG, "Adding Next Page Videos");
//                        int prevLastPosition = videoListAdapter.getItemCount();
                    videoListAdapter.removeLoader();
                    videoListAdapter.addMoreYouTubeVideos(ytVideos);
                    isLoading = false;
//                        recyclerView.smoothScrollToPosition(prevLastPosition + 1);
                }
            });

            this.nextPageToken = nextPageToken;
            if (nextPageToken != null) {
                videoListAdapter.setIOnLoadMoreListener(this);
            } else {
                videoListAdapter.removeOnLoadMoreListener();
            }
        }

        handler.post(() -> loadingProgressBar.setVisibility(View.INVISIBLE));
    }
//
//    /**
//     * Called when playlist cannot be found
//     * NOT USED in this fragment
//     *
//     * @param playlistId the playlist ID
//     * @param errorCode  the error code obtained
//     */
//    @Override
//    public void onPlaylistNotFound(String playlistId, int errorCode) { }

    @Override
    public void onLoadMore()
    {
        Log.e(TAG, "onLoadMore called");
        videoListAdapter.addLoader();
        new Handler().postDelayed(
                () -> {
                    YouTubeVideoLoader youTubeVideoLoader = new YouTubeVideoLoader(getActivity());
                    youTubeVideoLoader.setYouTubeVideoReceiver(SearchFragment.this);
                    youTubeVideoLoader.search(currentQuery, nextPageToken);
                },
                200
        );
    }
}
