package com.mercury.gnusin.rssreader.rss;

import android.content.Intent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gnusin on 09.09.2016.
 */
public class Channel {

    private int id;
    private String title;
    private String uri;
    private List<News> newsList;

    public Channel(String uri) {
        this.uri = uri;
    }

    public Channel(int id, String title, String uri) {
        this.id = id;
        this.title = title;
        this.uri = uri;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public void setNewsList(List<News> newsList) {
        this.newsList = newsList;
    }

    public List<String> getAllNewsTitles() {
        if (newsList == null) {
            return Collections.emptyList();
        }

        List<String> titles = new ArrayList<>(newsList.size());
        for (News news : newsList) {
            titles.add(news.getTitle());
        }
        return titles;
    }

    public News getNewsByOrderPosition(int orderPosition) {
        return getNewsList().get(orderPosition);
    }
}
