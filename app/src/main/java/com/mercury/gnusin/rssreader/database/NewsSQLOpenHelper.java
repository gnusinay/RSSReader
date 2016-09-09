package com.mercury.gnusin.rssreader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mercury.gnusin.rssreader.rss.Channel;
import com.mercury.gnusin.rssreader.rss.News;


public class RSSChannelSQLOpenHelper extends SQLiteOpenHelper {

    private static final String COMMA_SEP = ",";
    private static final String TEXT_TYPE = " TEXT";
    private static final String DATABASE_NAME = "RSSReader.db";
    public static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_NEWS_TABLE =
            "CREATE TABLE " + RSSChannelContract.News.TABLE_NAME + " (" +
            RSSChannelContract.News._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            RSSChannelContract.News.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
            RSSChannelContract.News.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + " )";

    private static final String SQL_CREATE_CHANNEL_TABLE =
            "CREATE TABLE " + RSSChannelContract.Channel.TABLE_NAME + " (" +
                    RSSChannelContract.Channel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    RSSChannelContract.Channel.COLUMN_NAME_TITLE + TEXT_TYPE + " )";

    private static final String SQL_DROP_NEWS_TABLE =
            "DROP TABLE IF EXISTS " + RSSChannelContract.News.TABLE_NAME;

    private static final String SQL_DROP_CHANNEL_TABLE =
            "DROP TABLE IF EXISTS " + RSSChannelContract.Channel.TABLE_NAME;

    public RSSChannelSQLOpenHelper(Context context) {
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

    public void addChannel(String uri) {


    }

    public String getChannelURI(int channelID) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(
                            RSSChannelContract.Channel.TABLE_NAME,
                            new String[] { RSSChannelContract.Channel.COLUMN_NAME_URI },
                            RSSChannelContract.Channel._ID + " = ?",
                            new String[] { String.valueOf(channelID) },
                            null,
                            null,
                            null);
        if (cursor.getCount() > 0) {
            return cursor.getString(cursor.getColumnIndex(RSSChannelContract.Channel.COLUMN_NAME_URI));
        }
        return "";
    }

    public void updateChannelTitle(String title) {
        SQLiteDatabase db = getWritableDatabase();
        db.update()

    }

    public void addNews(News news) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RSSChannelContract.News.COLUMN_NAME_TITLE, news.getTitle());
        values.put(RSSChannelContract.News.COLUMN_NAME_DESCRIPTION, news.getDescription());
        db.insert(RSSChannelContract.News.TABLE_NAME, null, values);
        db.close();
    }

    public void updateChannel(Channel channel) {
        SQLiteDatabase db = getWritableDatabase();
        db.query(
                RSSChannelContract.Channel.TABLE_NAME,
                new String[] {RSSChannelContract.Channel._ID},
                null,

        )

        deleteAllNews();

        for (News news : newsList) {
            ContentValues values = new ContentValues();
            values.put(RSSChannelContract.News.COLUMN_NAME_TITLE, news.getTitle());
            values.put(RSSChannelContract.News.COLUMN_NAME_DESCRIPTION, news.getDescription());
            db.insert(RSSChannelContract.News.TABLE_NAME, null, values);
        }
        db.close();
    }

    public void deleteAllNews() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(
                RSSChannelContract.News.TABLE_NAME,
                null,
                null
        );
        db.close();
    }

    public Cursor getAllNews() {
        String[] projection = {
                RSSChannelContract.News._ID,
                RSSChannelContract.News.COLUMN_NAME_TITLE,
                RSSChannelContract.News.COLUMN_NAME_DESCRIPTION};

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                RSSChannelContract.News.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                RSSChannelContract.News._ID
        );
        return cursor;
    }
}
