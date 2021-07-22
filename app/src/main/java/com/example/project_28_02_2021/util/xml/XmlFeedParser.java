package com.example.project_28_02_2021.util.xml;

import android.content.Context;

import com.example.project_28_02_2021.rss.NewsRssItem;
import com.example.project_28_02_2021.site.Site;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.util.ArrayList;

public class XmlFeedParser {

    public static NewsRssItem parseItemFromXml(Element element, Site from, Context context) {
        return new NewsRssItem(
                from, element.select(link_tag).text(),
                element.select(title_tag).text(),
                element.select(category_tag).text().toLowerCase()
                        + " " + element.select(description_tag).text().toLowerCase()
                        + " " + from.getName().toLowerCase(),
                element.select(pubDate_tag).text(),
                context);
    }

    public static ArrayList<NewsRssItem> parseFeedFromXml(String feed, Site from, Context context) {
        Parser parser = Parser.xmlParser();
        Document doc = Jsoup.parse(feed, "", parser);
        ArrayList<NewsRssItem> items = new ArrayList<>();
        for (Element element : doc.select(item_tag))
            items.add(parseItemFromXml(element, from, context));
        return items;
    }

    public static String item_tag
            = "item";
    public static String link_tag
            = "link";
    public static String url_tag
            = "url";
    public static String title_tag
            = "title";
    public static String category_tag
            = "category";
    public static String description_tag
            = "description";
    public static String pubDate_tag
            = "pubDate";
    public static String image_tag
            = "image";
}
