package com.example.project_28_02_2021;

import android.content.Context;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.util.Scanner;

class RssParser extends Thread {
    private final Site site;
    private final Context context;

    public RssParser(Site site, Context context) {
        this.site = site;
        this.context = context;
    }

    @Override
    public void run() {

        Thread loadThread = new Thread() {
            @Override
            public void run() {
                try {
                    String xmlString = Jsoup.connect(site.getUrl()).get().toString();
                    Document doc = Jsoup.parse(xmlString, "", Parser.xmlParser());
                    context.openFileOutput("read.xml", Context.MODE_APPEND);
                    InputStream stream = context.openFileInput("read.xml");
                    StringBuilder gotXml = new StringBuilder();
                    Scanner scanner = new Scanner(stream);
                    while (scanner.hasNext()) {
                        gotXml.append(scanner.next());
                    }
                    Document readDoc = Jsoup.parse(gotXml.toString(), "", Parser.xmlParser());
                    site.setImageLink(doc.select("image").select("url").text());
                    for (Element item : doc.select("item")) {
                        NewsRssItem newsRssItem = new NewsRssItem(
                                site, item.select("link").text(),
                                item.select("title").text(),
                                item.select("category").text().toLowerCase() + " " + item.select("description").text().toLowerCase() + " " + site.getName().toLowerCase(),
                                item.select("pubDate").text(),
                                doc.select("url").text(),
                                context);
                        boolean notRead = true;
                        Elements items = readDoc.select("item");
                        for (Element elem : items) {
                            if (elem.select("title").text().equalsIgnoreCase(newsRssItem.getTitle().replace(" ", ""))) {
                                notRead = false;
                            }
                        }
                        boolean notAgain = true;
                        for (NewsRssItem rssItem : NewsRssItem.getNews()) {
                            if (rssItem.getTitle().equals(newsRssItem.getTitle())) {
                                notAgain = false;
                                break;
                            }
                        }
                        if (notRead && notAgain) {
                            NewsRssItem.getNews().add(newsRssItem);
                        }
                    }
                } catch (Exception ignored) {
                }
                site.setStatus(Site.SiteStatusStates.FILLED_STATE);
            }
        };
        loadThread.run();
    }
}
