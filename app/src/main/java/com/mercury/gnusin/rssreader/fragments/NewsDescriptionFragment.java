package com.mercury.gnusin.rssreader.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mercury.gnusin.rssreader.activities.NewsDescriptionActivity;
import com.mercury.gnusin.rssreader.database.RSSChannelSQLOpenHelper;
import com.mercury.gnusin.rssreader.rss.News;


public class NewsDescriptionFragment extends Fragment {

    public static final String TAG = "NewsDescriptionFragment";
    public static final String SHOWN_NEWS_ID_BUNDLE_KEY = "newsID";

    public int getShownNewsId() {
        return getArguments().getInt(NewsDescriptionFragment.SHOWN_NEWS_ID_BUNDLE_KEY, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RSSChannelSQLOpenHelper sqlOpenHelper = RSSChannelSQLOpenHelper.getInstance(getContext());

        News news = sqlOpenHelper.getNewsById(getShownNewsId());

        if (news != null) {
            if (getActivity().getClass().equals(NewsDescriptionActivity.class)) {
                getActivity().setTitle(news.getTitle());
            }

            ScrollView scroller = new ScrollView(getActivity());
            TextView text = new TextView(getActivity());
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getActivity().getResources().getDisplayMetrics());
            text.setPadding(padding, padding, padding, padding);
            text.setTextSize(16);
            text.setText(news.getDescription());
            scroller.addView(text);
            return scroller;
        }
        return null;
    }
}
