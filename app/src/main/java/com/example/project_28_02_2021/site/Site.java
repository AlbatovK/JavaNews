package com.example.project_28_02_2021.site;

import android.content.Context;

import com.example.project_28_02_2021.rss.NewsRssItem;
import com.example.project_28_02_2021.R;
import com.example.project_28_02_2021.rss.NewsRssItemManager;

public class Site {
    private final String name;
    private final String url;
    private String imageLink = "no_link";
    private SiteStatusStates status = SiteStatusStates.UNFILLED_STATE;
    private final Context context;

    public Site(String name, String url, Context context) {
        this.name = name;
        this.url = url;
        this.context = context;
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
        for (NewsRssItem item : NewsRssItemManager.getInstance().getNews())
            if (item.getSite().getName().equals(name))
                newsCount++;
            return newsCount;
    }

    public void setStatus(SiteStatusStates status) { this.status = status; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public void setImageLink(String imageLink) { this.imageLink = imageLink; }
    public String getImageLink() { return imageLink; }
    public SiteStatusStates getStatus() { return status; }
}
