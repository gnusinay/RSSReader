package com.mercury.gnusin.rssreader.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mercury.gnusin.rssreader.R;
import com.mercury.gnusin.rssreader.database.RSSChannelSQLOpenHelper;
import com.mercury.gnusin.rssreader.rss.Channel;

public class ReaderActivity extends AppCompatActivity {

    public static final String DEFAULT_CHANNEL_ID = "DefaultChannelId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*deleteDatabase("RSSReader.db");
        getPreferences(MODE_PRIVATE).edit().clear().commit();*/
        if (getPreferences(Context.MODE_PRIVATE).getInt(DEFAULT_CHANNEL_ID, 0) == 0) {
            Channel channel = new Channel(getString(R.string.rss_uri));
            RSSChannelSQLOpenHelper sqlHelper = RSSChannelSQLOpenHelper.getInstance(this);
            int channelId = sqlHelper.addChannel(channel);
            if (channelId > -1) {
                getPreferences(Context.MODE_PRIVATE).edit().putInt(DEFAULT_CHANNEL_ID, channelId).commit();
                setContentView(R.layout.a_reader);
            } else {
                Toast.makeText(this, getString(R.string.no_add_default_channel_message), Toast.LENGTH_LONG).show();
            }
        } else {
            setContentView(R.layout.a_reader);
        }
    }
}
