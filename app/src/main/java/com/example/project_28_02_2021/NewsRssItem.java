package com.example.project_28_02_2021;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class NewsRssItem {

    private static final ArrayList<NewsRssItem> news          = new ArrayList<>();
    private static final ArrayList<NewsRssItem> likedNews     = new ArrayList<>();

    private final Site                          site;
    private final String                        link;
    private final String                        title;
    private final String                        urlImage;
    private final HashSet<String>               categoryWords = new HashSet<>();
    private final Context                       context;
    private Date                                pubDate;

    public NewsRssItem(Site site, String link, String title,
                       String categoryData, String pubDate, String urlImage,
                       Context context) {
        this.site    = site;
        this.link    = link;
        this.title   = title;
        this.urlImage = urlImage;
        this.context = context;

        String letters = ",.:-[{}]&*@!?;\"'#$%^()+-=><|\\/~`";
        StringBuilder builder = new StringBuilder();
        char[] chars = categoryData.concat(title.toLowerCase()).trim().toCharArray();
        for (char letter : chars) {
            if (letter == ' ') {
                categoryWords.add(builder.toString().trim());
                builder = new StringBuilder();
            } else {
                boolean isValid = true;
                for (char ch : letters.toCharArray()) {
                    if (ch == letter) {
                        isValid = false;
                        break;
                    }
                }
                if (!isValid) { continue; }
            }
            builder.append(letter);
        }
        categoryWords.add(builder.toString().trim());
        categoryWords.add(site.getName().trim().toLowerCase());
        String s_format_simple = "E, d MMM yyyy H:m:s z";
        String s_format_updated = "E MMM dd HH:mm:ss z yyyy";
        try { this.pubDate = new SimpleDateFormat(s_format_simple, Locale.US).
                    parse(pubDate); } catch (ParseException ignored) {
            try { this.pubDate = new SimpleDateFormat(s_format_updated, Locale.US).
                        parse(pubDate); } catch (ParseException anIgnored) { this.pubDate = new Date(System.currentTimeMillis()); }
        }
    }

    public enum ItemComparators {
        SORT_BY_SIZE,
        SORT_BY_SITE,
        SORT_BY_DATE,
    }

    public static Comparator<NewsRssItem> getComparator(ItemComparators type) {
        Comparator<NewsRssItem> itemComparator = (n_1, n_2) -> 0;
        switch (type) {
            case SORT_BY_SIZE: itemComparator = (n_1, n_2) ->
                    Integer.compare(n_2.getTitle().length(), n_1.getTitle().length()); break;
            case SORT_BY_SITE: itemComparator = (n_1, n_2) ->
                    n_1.getSite().getName().compareToIgnoreCase(n_2.getSite().getName()); break;
            case SORT_BY_DATE: itemComparator = (n_1, n_2) ->
                    n_2.getDate().compareTo(n_1.getDate()); break;
        }
        return itemComparator;
    }

    public String getRegexDate() {
        long nowDate = Calendar.getInstance().getTimeInMillis();
        double time = (double) (nowDate - pubDate.getTime()) / (1000 * 60 * 60 * 24);
        String date = (int) time + " "
                + context.getResources().getQuantityString(R.plurals.day_plurals, (int) time);
        if (time < 1) {
            time = (double) (nowDate - pubDate.getTime()) / (1000 * 60 * 60);
            date = (int) time + " "
                    + context.getResources().getQuantityString(R.plurals.hour_plurals, (int) time);
            if (time < 1) {
                time = (double) (nowDate - pubDate.getTime()) / (1000 * 60);
                date = (int) time + " "
                        + context.getResources().getQuantityString(R.plurals.min_plurals, (int) time);
                if (time < 1) {
                    time = (double) (nowDate - pubDate.getTime()) / (1000);
                    date = (int) time + " "
                            + context.getResources().getQuantityString(R.plurals.sec_plurals, (int) time);
                }
            }
        }
        return date;
    }

    public boolean isEqual(NewsRssItem item) {
        return (item.getTitle().equals(title) && item.getSite().getName().equals(site.getName()));
    }

    public Date getDate()                               { return pubDate; }
    public String getUrlImage()                         { return urlImage; }
    public static ArrayList<NewsRssItem> getNews()      { return news; }
    public static ArrayList<NewsRssItem> getLikedNews() { return likedNews; }
    public Site getSite()                               { return site; }
    public HashSet<String> getCategoryWords()           { return categoryWords; }
    public String getLink()                             { return link; }
    public String getTitle()                            { return title; }
}
