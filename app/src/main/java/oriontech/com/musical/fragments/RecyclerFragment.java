package oriontech.com.musical.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import oriontech.com.musical.R;
import oriontech.com.musical.adapters.VideosAdapter;
import oriontech.com.musical.utils.NetworkHelper;

public abstract class RecyclerFragment extends Fragment implements AdapterView.OnItemClickListener
{
    protected RecyclerView recyclerView;
    protected VideosAdapter videoListAdapter;

    protected NetworkHelper networkConf;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = rootView.findViewById(R.id.recently_played);
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.addItemDecoration(getItemDecoration());

        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setChangeDuration(500);
        recyclerView.getItemAnimator().setMoveDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);

        videoListAdapter = getAdapter();
        videoListAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(videoListAdapter);

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        networkConf = new NetworkHelper(getActivity());
    }

    /**
     * Required Overrides for Sample Fragments
     */

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    protected abstract RecyclerView.ItemDecoration getItemDecoration();

    protected abstract VideosAdapter getAdapter();
}