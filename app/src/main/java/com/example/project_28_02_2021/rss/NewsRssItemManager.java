package com.example.project_28_02_2021.rss;

import java.util.ArrayList;
import java.util.Comparator;

public class NewsRssItemManager {

    private static final ArrayList<NewsRssItem> news = new ArrayList<>();
    private static final ArrayList<NewsRssItem> likedNews = new ArrayList<>();
    private static NewsRssItemManager manager = null;

    public static NewsRssItemManager getInstance() {
        if (manager == null) { manager = new NewsRssItemManager(); }
        return manager;
    }

    public static Comparator<NewsRssItem> getComparator(ItemComparators type) {
        Comparator<NewsRssItem> itemComparator = (n_1, n_2) -> 0;
        switch (type) {
            case SORT_BY_SIZE:
                itemComparator = (n_1, n_2) ->
                        Integer.compare(n_2.getTitle().length(), n_1.getTitle().length());
                break;
            case SORT_BY_SITE:
                itemComparator = (n_1, n_2) ->
                        n_1.getSite().getName().compareToIgnoreCase(n_2.getSite().getName());
                break;
            case SORT_BY_DATE:
                itemComparator = (n_1, n_2) ->
                        n_2.getDate().compareTo(n_1.getDate());
                break;
        }
        return itemComparator;
    }

    private NewsRssItemManager() {}
    public void clearNews() { news.clear(); }
    public void clearLikedNews() { likedNews.clear(); }
    public ArrayList<NewsRssItem> getNews() { return news; }
    public ArrayList<NewsRssItem> getLikedNews() { return likedNews; }
    public void addNews(NewsRssItem item) { news.add(item); }
    public void addLikedNews(NewsRssItem item) { likedNews.add(item); }
}