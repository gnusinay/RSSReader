package com.mercury.gnusin.rssreader.database;

import android.provider.BaseColumns;


public final class RSSChannelContract {

    public RSSChannelContract() {}

    public static abstract class News implements BaseColumns {
        public static final String TABLE_NAME = "news";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_CHANNEL_ID = "channel_id";
    }

    public static abstract class Channel implements BaseColumns {
        public static final String TABLE_NAME = "channel";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_URI = "uri";
    }
}
