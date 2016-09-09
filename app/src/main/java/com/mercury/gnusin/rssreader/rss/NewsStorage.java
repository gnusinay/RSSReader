package com.mercury.gnusin.rssreader.rss;

import android.content.Context;

import com.mercury.gnusin.rssreader.R;
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


public class NewsStorage {

    static private String titleChannel;
    static private List<News> loadedNewsList = new ArrayList<>();

    static public List<String> getAllNewsTitles() {
        List<String> titles = new ArrayList<>(loadedNewsList.size());
        for (News news : loadedNewsList) {
            titles.add(news.getTitle());
        }
        return titles;
    }

    static public String getNewsDescription(int position) {
        return loadedNewsList.get(position).getDescription();
    }

    static public String getTitleChannel() {
        return titleChannel;
    }

    static public String getNewsTitle(int position) {
        return loadedNewsList.get(position).getTitle();
    }

    static private void setDefaultTitleChannel(Context context) {
        titleChannel = context.getString(R.string.default_title_channel);
    }

    static public void reloadStorage(Context context) throws FileNotFoundException, XmlPullParserException, IOException  {
        //SystemClock.sleep(2000);
        setDefaultTitleChannel(context);

        InputStream in = null;
        List<News> newLoadedNewsList = new ArrayList<>();
        try {
            URL url = new URL(context.getString(R.string.rss_uri));
            URLConnection connection = url.openConnection();
            in = connection.getInputStream();

            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xmlPullParserFactory.newPullParser();
            parser.setInput(in, null);

            int eventType = parser.getEventType();
            News tNews = null;
            String tagName = "";
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
                            newLoadedNewsList.add(tNews);
                        }
                        eventType = parser.next();
                        break;
                    default:
                        eventType = parser.next();
                }
                throw new FileNotFoundException();
            }

            loadedNewsList = newLoadedNewsList;
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
