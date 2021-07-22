package com.example.project_28_02_2021.util.net;

import android.content.Context;

import com.example.project_28_02_2021.rss.NewsRssItem;
import com.example.project_28_02_2021.rss.NewsRssItemManager;
import com.example.project_28_02_2021.site.Site;
import com.example.project_28_02_2021.site.SiteStatusStates;
import com.example.project_28_02_2021.util.files.FileManager;
import com.example.project_28_02_2021.util.xml.XmlFeedParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;

public class NetLoader {

    public static Document getSiteContent(Site site) throws IOException {
        String xml = Jsoup.connect(site.getUrl()).get().toString();
        return Jsoup.parse(xml, "", Parser.xmlParser());
    }

    public static void loadFromSite(Context context, Site from) {
        Runnable load = () -> {
            try {
                Document doc;
                doc = getSiteContent(from);
                Document readDoc;
                readDoc = Jsoup.parse(FileManager.readFile(context, FileManager.deleted_news_storage), "", Parser.xmlParser());
                from.setImageLink(doc.select(XmlFeedParser.image_tag).select(XmlFeedParser.url_tag).text());
                for (Element el : doc.select(XmlFeedParser.item_tag)) {
                NewsRssItem item = XmlFeedParser.parseItemFromXml(el, from, context);
                boolean notRead = true;
                Elements items = readDoc.select(XmlFeedParser.item_tag);
                for (Element elem : items)
                    if (elem.select(XmlFeedParser.title_tag).text().equalsIgnoreCase(item.getTitle().replace(" ", "")))
                        notRead = false;
                boolean notAgain = true;
                for (NewsRssItem rssItem : NewsRssItemManager.getInstance().getNews())
                    if (rssItem.getTitle().equals(item.getTitle())) { notAgain = false; break; }
                if (notRead && notAgain) NewsRssItemManager.getInstance().addNews(item);
                from.setStatus(SiteStatusStates.FILLED_STATE);
                }
            } catch (Exception e) { from.setStatus(SiteStatusStates.UNKNOWN_STATE); }
        };
        Thread load_thread = new Thread(load);
        load_thread.start();
    }
}
