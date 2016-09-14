package com.mercury.gnusin.rssreader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.mercury.gnusin.rssreader.rss.Channel;
import com.mercury.gnusin.rssreader.rss.News;
import java.util.ArrayList;
import java.util.List;


public class RSSChannelSQLOpenHelper extends SQLiteOpenHelper {

    private static RSSChannelSQLOpenHelper sqlOpenHelper;

    private static final String COMMA_SEP = ",";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String DATABASE_NAME = "RSSReader.db";
    public static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_NEWS_TABLE =
            "CREATE TABLE " + RSSChannelContract.News.TABLE_NAME + " (" +
            RSSChannelContract.News._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            RSSChannelContract.News.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
            RSSChannelContract.News.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    RSSChannelContract.News.COLUMN_NAME_CHANNEL_ID + INT_TYPE + ")";

    private static final String SQL_CREATE_CHANNEL_TABLE =
            "CREATE TABLE " + RSSChannelContract.Channel.TABLE_NAME + " (" +
                    RSSChannelContract.Channel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RSSChannelContract.Channel.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    RSSChannelContract.Channel.COLUMN_NAME_URI + TEXT_TYPE + ")";

    private static final String SQL_DROP_NEWS_TABLE =
            "DROP TABLE IF EXISTS " + RSSChannelContract.News.TABLE_NAME;

    private static final String SQL_DROP_CHANNEL_TABLE =
            "DROP TABLE IF EXISTS " + RSSChannelContract.Channel.TABLE_NAME;

    public static RSSChannelSQLOpenHelper getInstance(Context context) {
        if (sqlOpenHelper == null) {
            sqlOpenHelper = new RSSChannelSQLOpenHelper(context);
        }
        return sqlOpenHelper;
    }

    private RSSChannelSQLOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NEWS_TABLE);
        db.execSQL(SQL_CREATE_CHANNEL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DROP_NEWS_TABLE);
        db.execSQL(SQL_DROP_CHANNEL_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public int addChannel(Channel channel) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(RSSChannelContract.Channel.COLUMN_NAME_TITLE, channel.getTitle());
            values.put(RSSChannelContract.Channel.COLUMN_NAME_URI, channel.getUri());

            int id = (int) db.insert(RSSChannelContract.Channel.TABLE_NAME,
                    null,
                    values);

            return id;
        } finally {
            db.close();
        }
    }

    public Channel getChannel(int channelID) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor cursor = db.query(
                    RSSChannelContract.Channel.TABLE_NAME,
                    new String[]{RSSChannelContract.Channel._ID, RSSChannelContract.Channel.COLUMN_NAME_TITLE, RSSChannelContract.Channel.COLUMN_NAME_URI},
                    RSSChannelContract.Channel._ID + " = ?",
                    new String[]{String.valueOf(channelID)},
                    null,
                    null,
                    null);

            Channel channel = null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                channel = new Channel(cursor.getInt(cursor.getColumnIndex(RSSChannelContract.Channel._ID)),
                        cursor.getString(cursor.getColumnIndex(RSSChannelContract.Channel.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndex(RSSChannelContract.Channel.COLUMN_NAME_URI)));

                cursor.close();
                cursor = db.query(
                        RSSChannelContract.News.TABLE_NAME,
                        new String[]{RSSChannelContract.News._ID, RSSChannelContract.News.COLUMN_NAME_TITLE, RSSChannelContract.News.COLUMN_NAME_DESCRIPTION},
                        RSSChannelContract.News.COLUMN_NAME_CHANNEL_ID + " = ?",
                        new String[]{String.valueOf(channel.getId())},
                        null,
                        null,
                        null);

                List<News> channelNews = new ArrayList<>(cursor.getCount());

                while (cursor.moveToNext()) {
                    News news = new News(
                            cursor.getInt(cursor.getColumnIndex(RSSChannelContract.News._ID)),
                            cursor.getString(cursor.getColumnIndex(RSSChannelContract.News.COLUMN_NAME_TITLE)),
                            cursor.getString(cursor.getColumnIndex(RSSChannelContract.News.COLUMN_NAME_DESCRIPTION)));
                    channelNews.add(news);
                }

                channel.setNewsList(channelNews);
            }

            cursor.close();
            return channel;
        } finally {
            db.close();
        }
    }


    public void saveChannel(Channel channel) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(RSSChannelContract.Channel._ID, channel.getId());
            values.put(RSSChannelContract.Channel.COLUMN_NAME_TITLE, channel.getTitle());
            values.put(RSSChannelContract.Channel.COLUMN_NAME_URI, channel.getUri());

            db.update(RSSChannelContract.Channel.TABLE_NAME,
                    values,
                    RSSChannelContract.Channel._ID + " = ?",
                    new String[]{String.valueOf(channel.getId())});

            db.delete(RSSChannelContract.News.TABLE_NAME,
                    RSSChannelContract.News.COLUMN_NAME_CHANNEL_ID + " = ?",
                    new String[]{ String.valueOf(channel.getId()) });

            for (News news : channel.getNewsList()) {
                values = new ContentValues();
                values.put(RSSChannelContract.News.COLUMN_NAME_TITLE, news.getTitle());
                values.put(RSSChannelContract.News.COLUMN_NAME_DESCRIPTION, news.getDescription());
                values.put(RSSChannelContract.News.COLUMN_NAME_CHANNEL_ID, channel.getId());

                db.insert(RSSChannelContract.News.TABLE_NAME,
                        null,
                        values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }


    public boolean hasNews(Channel channel) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            Cursor cursor = db.query(
                    RSSChannelContract.News.TABLE_NAME,
                    new String[] { RSSChannelContract.News.COLUMN_NAME_CHANNEL_ID },
                    RSSChannelContract.News.COLUMN_NAME_CHANNEL_ID + " = ?",
                    new String[] { String.valueOf(channel.getId()) },
                    null,
                    null,
                    null
            );

            boolean result = cursor.getCount() > 0;

            cursor.close();
            return result;
        } finally {
            db.close();
        }
    }


    public News getNewsById(int newsID) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor cursor = db.query(
                    RSSChannelContract.News.TABLE_NAME,
                    new String[] { RSSChannelContract.News._ID, RSSChannelContract.News.COLUMN_NAME_TITLE, RSSChannelContract.News.COLUMN_NAME_DESCRIPTION },
                    RSSChannelContract.News._ID + " = ?",
                    new String[] { String.valueOf(newsID) },
                    null,
                    null,
                    null
            );

            News news = null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                news = new News(cursor.getInt(cursor.getColumnIndex(RSSChannelContract.News._ID)),
                        cursor.getString(cursor.getColumnIndex(RSSChannelContract.News.COLUMN_NAME_TITLE)),
                        cursor.getString(cursor.getColumnIndex(RSSChannelContract.News.COLUMN_NAME_DESCRIPTION)));
            }

            cursor.close();
            return news;
        } finally {
            db.close();
        }


    }
}
