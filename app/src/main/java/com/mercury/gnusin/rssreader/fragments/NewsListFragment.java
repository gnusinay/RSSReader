package com.mercury.gnusin.rssreader.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.mercury.gnusin.rssreader.R;
import com.mercury.gnusin.rssreader.activities.NewsDescriptionActivity;
import com.mercury.gnusin.rssreader.activities.ReaderActivity;
import com.mercury.gnusin.rssreader.database.RSSChannelSQLOpenHelper;
import com.mercury.gnusin.rssreader.rss.Channel;
import com.mercury.gnusin.rssreader.rss.News;
import com.mercury.gnusin.rssreader.services.RSSFetchService;


public class NewsListFragment extends ListFragment {

    private boolean dualPane;
    private int curPosition = -1;
    private String titleActivity;
    private Channel channel;
    private BroadcastReceiver failedReceiver;
    private BroadcastReceiver successfulReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        channel = new Channel(getActivity().getPreferences(Context.MODE_PRIVATE).getInt(ReaderActivity.DEFAULT_CHANNEL_ID, 0), "", "");

        failedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((SwipeRefreshLayout) getView().findViewById(R.id.swipeContainer)).setRefreshing(false);

                Toast.makeText(getActivity().getApplicationContext(), intent.getStringExtra("value"), Toast.LENGTH_LONG).show();
                if (getListAdapter() == null) {
                    setListAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, channel.getAllNewsTitles()));
                }
                titleActivity = getString(R.string.app_name);
                getActivity().setTitle(titleActivity);
            }
        };
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(failedReceiver, new IntentFilter(RSSFetchService.FAILED_FETCH_EVENT));


        successfulReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                RSSChannelSQLOpenHelper sqlOpenHelper = RSSChannelSQLOpenHelper.getInstance(getContext());
                int iChannelId = intent.getIntExtra("value", -1);

                if (channel.getId() == iChannelId) {
                    channel = sqlOpenHelper.getChannel(channel.getId());

                    setListAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, channel.getAllNewsTitles()));

                    ((SwipeRefreshLayout) getView().findViewById(R.id.swipeContainer)).setRefreshing(false);

                    if (dualPane) {
                        showDescription(channel.getNewsByOrderPosition(curPosition));
                    }

                    titleActivity = channel.getTitle();
                    getActivity().setTitle(titleActivity);
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(successfulReceiver, new IntentFilter(RSSFetchService.SUCCESSFUL_FETCH_EVENT));

        fetchChannel();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(titleActivity);

        dualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (dualPane && getListView().getAdapter() != null && getListView().getAdapter().getCount() > 0) {
            if (curPosition == -1) {
                curPosition = 0;
            }
            View descriptionFrame = getActivity().findViewById(R.id.newsDescriptionFrame);
            if (descriptionFrame != null) {
                descriptionFrame.setVisibility(View.VISIBLE);
                showDescription(channel.getNewsByOrderPosition(curPosition));
            }
        } else if (!dualPane && curPosition > -1) {
            showDescription(channel.getNewsByOrderPosition(curPosition));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v_title_list, null);
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fetchNewsByService(channel.getId());
                    }
                }).start();
            }
        });

        return view;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        v.setSelected(true);
        curPosition = position;
        showDescription(channel.getNewsByOrderPosition(curPosition));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(failedReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(successfulReceiver);
        super.onDestroy();
    }


    private  void fetchChannel() {
        titleActivity = getString(R.string.reload_status);
        new Thread(new Runnable() {
            @Override
            public void run() {
                RSSChannelSQLOpenHelper sqlOpenHelper = RSSChannelSQLOpenHelper.getInstance(getContext());
                if (sqlOpenHelper.hasNews(channel)) {
                    Intent finishIntent = new Intent(RSSFetchService.SUCCESSFUL_FETCH_EVENT);
                    finishIntent.putExtra("value", channel.getId());
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(finishIntent);
                } else {
                    fetchNewsByService(channel.getId());
                }
            }
        }).start();
    }


    private void fetchNewsByService(int channelId) {
        Intent intent = new Intent(getContext(), RSSFetchService.class);
        intent.putExtra("value", channelId);
        getContext().startService(intent);
    }


    private void showDescription(News news) {
        if (dualPane) {
            getListView().setItemChecked(curPosition, true);

            NewsDescriptionFragment descriptionFragment = (NewsDescriptionFragment) getFragmentManager().findFragmentByTag(NewsDescriptionFragment.TAG);
            if (descriptionFragment == null || descriptionFragment.getShownNewsId() != news.getId()) {
                Bundle bundle = new Bundle();
                bundle.putInt(NewsDescriptionFragment.SHOWN_NEWS_ID_BUNDLE_KEY, news.getId());
                descriptionFragment = new NewsDescriptionFragment();
                descriptionFragment.setArguments(bundle);
            }

            FragmentTransaction tran = getFragmentManager().beginTransaction();
            tran.replace(R.id.newsDescriptionFrame, descriptionFragment, NewsDescriptionFragment.TAG);
            tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tran.commit();
        } else {
            Intent intent = new Intent();
            intent.setClass(getActivity(), NewsDescriptionActivity.class);
            intent.putExtra(NewsDescriptionFragment.SHOWN_NEWS_ID_BUNDLE_KEY, news.getId());
            startActivity(intent);
        }

    }
}
