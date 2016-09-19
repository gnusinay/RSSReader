package com.mercury.gnusin.rssreader.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.mercury.gnusin.rssreader.fragments.NewsDescriptionFragment;


public class NewsDescriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            NewsDescriptionFragment fragment = new NewsDescriptionFragment();
            fragment.setArguments(getIntent().getExtras());
            FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
            tran.replace(android.R.id.content, fragment);
            tran.commit();
        }
    }
}
