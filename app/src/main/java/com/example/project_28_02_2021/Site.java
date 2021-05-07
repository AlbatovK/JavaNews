package com.example.project_28_02_2021;

import android.content.Context;

import java.util.ArrayList;

public class Site {
    private static final ArrayList<Site>        sites = new ArrayList<>();
    private final String                        name;
    private final String                        url;
    private String                              imageLink = "";
    private SiteStatusStates                    status = SiteStatusStates.UNFILLED_STATE;
    private final Context                       context;

    public Site(String name, String url, Context context) {
        this.name    = name;
        this.url     = url;
        this.context = context;
    }

    public enum SiteStatusStates {
        FILLED_STATE,
        UNFILLED_STATE,
        UNKNOWN_STATE,
    }

    public static class SiteFactory {
        public static Site getSiteByName(String name, Context context) {
            for (Site site : sites) { if (site.name.equals(name)) { return site; } }
            return new Site(name, "Неизвестно", context);
        }
    }

    public String asString() {
        return context.getString(R.string.str_site_data, name, url)
                .replaceAll(".xml", "")
                .replaceAll("://", "")
                .replaceAll("https", "")
                .replaceAll("http", "")
                .replaceAll("ftp", "")
                .replaceAll("ftps", "");
    }

    public int getItemsCount() {
        int newsCount = 0;
        for (NewsRssItem item : NewsRssItem.getNews()) { if (item.getSite() == this) { newsCount++; } }
        return newsCount;
    }

    public static ArrayList<Site> getSites()        { return sites; }
    public void setStatus(SiteStatusStates status)  { this.status = status; }
    public String getName()                         { return name; }
    public String getUrl()                          { return url; }
    public void setImageLink(String imageLink)      { this.imageLink = imageLink; }
    public String getImageLink()                    { return imageLink; }
    public SiteStatusStates getStatus()             { return status; }
}
