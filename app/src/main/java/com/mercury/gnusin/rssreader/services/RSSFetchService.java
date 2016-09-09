package com.mercury.gnusin.rssreader.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.mercury.gnusin.rssreader.R;
import com.mercury.gnusin.rssreader.database.RSSChannelSQLOpenHelper;
import com.mercury.gnusin.rssreader.rss.News;
import com.mercury.gnusin.rssreader.database.NewsSQLOpenHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gnusin on 09.09.2016.
 */
public class RSSFetchService extends IntentService {

    public static final String ERROR_FETCH_EVENT = "ERROR_FETCH";
    public static final String FINISH_FETCH_EVENT = "FINISH_FETCH";

    //private final String RSS_CHANNEL_URI = getApplicationContext().getString(R.string.rss_uri);

    public RSSFetchService() {
        super("RSSFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int channelId = intent.getIntExtra("channelID", 0);


        if (channelId > 0) {
            String errorMessage = "";
            try {
                fetch(channelId);
            } catch (FileNotFoundException ef) {
                errorMessage = String.format(getString(R.string.file_not_found_exception_message), getString(R.string.rss_uri));
            } catch (XmlPullParserException ex) {
                errorMessage = String.format(getString(R.string.xml_parser_exception_message), getString(R.string.rss_uri), ex.getMessage());
            } catch (IOException ei) {
                errorMessage = ei.getMessage();
            }
            if ("".equals(errorMessage)) {
                NewsSQLOpenHelper sqlHelper = new NewsSQLOpenHelper(getApplicationContext());
                sqlHelper.updateNews(loadedNewsList);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(FINISH_FETCH_EVENT));
            } else {
                Intent broadcastIntent = new Intent(ERROR_FETCH_EVENT);
                broadcastIntent.putExtra("value", errorMessage);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
            }

        }


    }

    public void fetch(int channelId) throws FileNotFoundException, XmlPullParserException, IOException {
        RSSChannelSQLOpenHelper sqlHelper = new RSSChannelSQLOpenHelper(getApplicationContext());
        String channelURI = sqlHelper.getChannelURI(channelId);

        InputStream in = null;
        List<News> loadedNewsList = new ArrayList<>();
        try {
            URL url = new URL(channelURI);
            URLConnection connection = url.openConnection();
            in = connection.getInputStream();

            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlPullParserFactory.newPullParser();
            parser.setInput(in, null);

            int eventType = parser.getEventType();
            News tNews = null;
            String tagName = "";
            String titleChannel = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        while (true) {
                            if (eventType == XmlPullParser.START_TAG) {
                                tagName = parser.getName().toLowerCase();
                                if ("title".equals(tagName)) {
                                    titleChannel = parser.nextText();
                                    eventType = parser.next();
                                    break;
                                } else if ("items".equals(tagName)) {    // if no channel title
                                    break;
                                }
                            }
                            eventType = parser.next();
                        }
                        break;
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName().toLowerCase();
                        if ("item".equals(tagName)) {
                            tNews = new News();
                        } else if (tNews != null) {
                            if ("title".equals(tagName)) {
                                tNews.setTitle(parser.nextText());
                            } else if ("description".equals(tagName)) {
                                tNews.setDescription(parser.nextText());
                            }
                        }
                        eventType = parser.next();
                        break;
                    case XmlPullParser.END_TAG:
                        tagName = parser.getName().toLowerCase();
                        if ("item".equals(tagName) && tNews != null) {
                            loadedNewsList.add(tNews);
                        }
                        eventType = parser.next();
                        break;
                    default:
                        eventType = parser.next();
                }
                throw new FileNotFoundException();
            }

            sqlHelper.

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
