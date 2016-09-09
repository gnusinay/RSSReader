package com.mercury.gnusin.rssreader.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mercury.gnusin.rssreader.R;
import com.mercury.gnusin.rssreader.activities.NewsDescriptionActivity;
import com.mercury.gnusin.rssreader.rss.NewsStorage;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;


public class NewsListFragment extends ListFragment {

    private static final int FINISH_PARSE_EVENT = 1;
    private static final int ERROR_PARSE_EVENT = 0;

    private Handler handler;
    private boolean dualPane;
    private int curPosition = 0;
    private String titleActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        handler =  new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ERROR_PARSE_EVENT:
                        getView().findViewById(R.id.loadIndicator).setVisibility(View.INVISIBLE);
                        getView().findViewById(R.id.emptyText).setVisibility(View.VISIBLE);
                        //if (getView() != null) {

                        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView();
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        //}

                        Toast.makeText(getContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
                        titleActivity = getString(R.string.app_name);
                        break;
                    case FINISH_PARSE_EVENT:
                        setListAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, NewsStorage.getAllNewsTitles()));
                        titleActivity = NewsStorage.getTitleChannel();
                        if (dualPane) {
                            showDescription(curPosition);
                        }

                        if (getView() != null) {
                            SwipeRefreshLayout swipeRefreshLayout2 = (SwipeRefreshLayout) getView();
                            if (swipeRefreshLayout2 != null) {
                                swipeRefreshLayout2.setRefreshing(false);
                            }
                        }
                        break;
                }
                getActivity().setTitle(titleActivity);
            }
        };

        fetchNews();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle(titleActivity);

        View descriptionFrame = getActivity().findViewById(R.id.newsDescriptionFrame);
        dualPane = descriptionFrame != null;

        if (dualPane && getListView().getAdapter() != null && getListView().getAdapter().getCount() > 0) {
            //descriptionFrame.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            showDescription(curPosition);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v_title_list, null);
        //SystemClock.sleep(10000);
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNews();
                //swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        v.setSelected(true);
        showDescription(position);
    }

    private  void fetchNews() {
        titleActivity = getString(R.string.reload_status);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String errorMessage = "";
                try {
                    // reload news from a RSS-channel
                    NewsStorage.reloadStorage(getContext());
                } catch (FileNotFoundException ef) {
                    errorMessage = String.format(getString(R.string.file_not_found_exception_message), getString(R.string.rss_uri));
                } catch (XmlPullParserException ex) {
                    errorMessage = String.format(getString(R.string.xml_parser_exception_message), getString(R.string.rss_uri), ex.getMessage());
                } catch (IOException ei) {
                    errorMessage = ei.getMessage();
                }

                if ("".equals(errorMessage)) {
                    handler.sendEmptyMessage(FINISH_PARSE_EVENT);
                } else {
                    Message msg = handler.obtainMessage(ERROR_PARSE_EVENT, String.format(getString(R.string.parse_error_message), errorMessage));
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void showDescription(int position) {
        curPosition = position;

        if (dualPane) {
            getListView().setItemChecked(curPosition, true);

            NewsDescriptionFragment descriptionFragment = (NewsDescriptionFragment) getFragmentManager().findFragmentByTag(NewsDescriptionFragment.TAG);
            if (descriptionFragment == null || descriptionFragment.getShownPosition() != curPosition) {
                descriptionFragment = NewsDescriptionFragment.newInstance(curPosition);
            }

            FragmentTransaction tran = getFragmentManager().beginTransaction();
            tran.replace(R.id.newsDescriptionFrame, descriptionFragment);
            tran.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            tran.commit();
        } else {
            Intent intent = new Intent();
            intent.setClass(getActivity(), NewsDescriptionActivity.class);
            intent.putExtra(NewsDescriptionActivity.SHOWN_POSITION_BUNDLE_KEY, curPosition);
            startActivity(intent);
        }

    }
}
