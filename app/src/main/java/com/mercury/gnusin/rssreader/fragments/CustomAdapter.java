package com.mercury.gnusin.rssreader.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mercury.gnusin.rssreader.R;

import java.util.List;

/**
 * Created by gnusin on 10.10.2016.
 */

public class CustomAdapter<T> extends ArrayAdapter<String> {

    private int selectedItem = -1;

    public CustomAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);

        int backgroundColor = view.getResources().getColor(R.color.item_background_default);
        int textColor = view.getResources().getColor(R.color.item_text_default);
        if (position == selectedItem) {
            backgroundColor = view.getResources().getColor(R.color.item_background_selected);
            textColor = view.getResources().getColor(R.color.item_background_default);
        }

        view.setBackgroundColor(backgroundColor);
        view.setTextColor(textColor);

        return view;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;

    }
}
