package com.mercury.gnusin.rssreader.fragments;

import android.content.Context;
import android.widget.TextView;
import android.widget.Checkable;

/**
 * Created by gnusin on 11.10.2016.
 */

public class TitleListItem extends TextView implements Checkable {

    private boolean isChecked = false;

    public TitleListItem(Context conext) {
        super(conext);

    }

    @Override
    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        isChecked = !isChecked;
    }
}
