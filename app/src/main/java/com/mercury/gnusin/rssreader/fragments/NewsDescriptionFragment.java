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
import com.mercury.gnusin.rssreader.rss.NewsStorage;


public class NewsDescriptionFragment extends Fragment {

    public static final String TAG = "NewsDescriptionFragment";

    public static NewsDescriptionFragment newInstance(int position) {
        NewsDescriptionFragment fragment = new NewsDescriptionFragment();

        Bundle bundle =  new Bundle();
        bundle.putInt(NewsDescriptionActivity.SHOWN_POSITION_BUNDLE_KEY, position);
        fragment.setArguments(bundle);

        return fragment;
    }

    public int getShownPosition() {
        return getArguments().getInt(NewsDescriptionActivity.SHOWN_POSITION_BUNDLE_KEY, -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int newsPosition = getShownPosition();
        if (newsPosition != -1) {
            if (getActivity().getClass().equals(NewsDescriptionActivity.class)) {
                getActivity().setTitle(NewsStorage.getNewsTitle(newsPosition));
            }

            ScrollView scroller = new ScrollView(getActivity());
            TextView text = new TextView(getActivity());
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getActivity().getResources().getDisplayMetrics());
            text.setPadding(padding, padding, padding, padding);
            text.setTextSize(16);
            text.setText(NewsStorage.getNewsDescription(newsPosition));
            scroller.addView(text);
            return scroller;
        }
        return null;
    }
}
