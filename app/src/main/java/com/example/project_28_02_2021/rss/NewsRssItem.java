package com.example.project_28_02_2021.rss;

import android.content.Context;

import com.example.project_28_02_2021.R;
import com.example.project_28_02_2021.site.Site;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class NewsRssItem implements Comparable<NewsRssItem> {
    private final Site site;
    private final String link;
    private final String title;
    private final HashSet<String> categoryWords = new HashSet<>();
    private final Context context;
    private Date pubDate;

    public NewsRssItem(Site site,
                       String link,
                       String title,
                       String categoryData,
                       String pubDate,
                       Context context) {
        this.site = site;
        this.link = link;
        this.title = title;
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
                for (char ch : letters.toCharArray())
                    if (ch == letter) {
                        isValid = false;
                        break;
                    }
                if (!isValid)
                    continue;
            }
            builder.append(letter);
        }

        categoryWords.add(builder.toString().trim());
        categoryWords.add(site.getName().trim().toLowerCase());
        String[] patterns = new String[]{ "E, d MMM yyyy H:m:s z", "E MMM dd HH:mm:ss z yyyy" };
        this.pubDate = new Date(System.currentTimeMillis());
        for (String pattern : patterns) {
            try { this.pubDate = new SimpleDateFormat(pattern, Locale.US).parse(pubDate); }
            catch (ParseException ignored) {}
        }
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

    @Override
    public int compareTo(NewsRssItem n) { return n.title.compareToIgnoreCase(title); }
    public boolean isEqual(NewsRssItem n) { return title.equalsIgnoreCase(n.title); }
    public Date getDate() { return pubDate; }
    public Site getSite() { return site; }
    public HashSet<String> getCategoryWords() { return categoryWords; }
    public String getLink() { return link; }
    public String getTitle() { return title; }
}
