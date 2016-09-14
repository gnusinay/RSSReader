package com.mercury.gnusin.rssreader.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.ArrayList;
import java.util.List;


public class NewsListFragment extends ListFragment {

    private boolean dualPane;
    private int curPosition = 0;
    private String titleActivity;
    private Channel channel;
    private List<BroadcastReceiver> receiverList = new ArrayList<>(2);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        channel = new Channel(getActivity().getPreferences(Context.MODE_PRIVATE).getInt(ReaderActivity.DEFAULT_CHANNEL_ID, 0), "", "");

        BroadcastReceiver errorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getView().findViewById(R.id.loadIndicator).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.emptyText).setVisibility(View.VISIBLE);

                ((SwipeRefreshLayout) getView().findViewById(R.id.swipeContainer)).setRefreshing(false);

                Toast.makeText(getActivity().getApplicationContext(), intent.getStringExtra("value"), Toast.LENGTH_LONG).show();
                if (getListAdapter() == null) {
                    setListAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, channel.getAllNewsTitles()));
                }
                titleActivity = getString(R.string.app_name);
                getActivity().setTitle(titleActivity);
            }
        };
        receiverList.add(errorReceiver);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(errorReceiver, new IntentFilter(RSSFetchService.ERROR_FETCH_EVENT));


        BroadcastReceiver finishReceiver = new BroadcastReceiver() {
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
        receiverList.add(finishReceiver);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(finishReceiver, new IntentFilter(RSSFetchService.FINISH_FETCH_EVENT));

        fetchChannel();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(titleActivity);

        View descriptionFrame = getActivity().findViewById(R.id.newsDescriptionFrame);
        dualPane = descriptionFrame != null;

        if (dualPane && getListView().getAdapter() != null && getListView().getAdapter().getCount() > 0) {
            descriptionFrame.setVisibility(View.VISIBLE);
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

        if (getListAdapter() != null && getListAdapter().getCount() == 0) {
            view.findViewById(R.id.loadIndicator).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.emptyText).setVisibility(View.VISIBLE);
        }

        return view;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        v.setSelected(true);
        curPosition = position;
        showDescription(channel.getNewsByOrderPosition(curPosition));
    }


    @Override
    public void onDestroy() {
        for (BroadcastReceiver receiver : receiverList) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        }
        super.onDestroy();
    }


    private  void fetchChannel() {
        titleActivity = getString(R.string.reload_status);
        new Thread(new Runnable() {
            @Override
            public void run() {
                RSSChannelSQLOpenHelper sqlOpenHelper = RSSChannelSQLOpenHelper.getInstance(getContext());
                if (sqlOpenHelper.hasNews(channel)) {
                    Intent finishIntent = new Intent(RSSFetchService.FINISH_FETCH_EVENT);
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
            tran.replace(R.id.newsDescriptionFrame, descriptionFragment);
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
