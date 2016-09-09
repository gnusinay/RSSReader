package com.mercury.gnusin.rssreader.rss;

import android.content.Intent;

import java.util.List;

/**
 * Created by gnusin on 09.09.2016.
 */
public class Channel {

    private Intent id;
    private String title;
    private List<News> newsList;

    public Intent getId() {
        return id;
    }

    public void setId(Intent id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public void setNewsList(List<News> newsList) {
        this.newsList = newsList;
    }
}
