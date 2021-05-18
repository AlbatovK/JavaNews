package com.example.project_28_02_2021;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class LikedNewsActivity extends AppCompatActivity {

    private ListView listView = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_liked_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_all) {
            NewsRssItem.getLikedNews().clear();
            deleteFile("news.xml");
            setDefaultSettings();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        getMenuInflater().inflate(R.menu.liked_list_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.liked_share) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, NewsRssItem.getNews().get(info.position).getLink());
            shareIntent.setType("text/plain");
            startActivity(shareIntent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog alertDialog = builder
                    .setTitle(R.string.str_delete_liked)
                    .setMessage(R.string.str_sure_delete_liked)
                    .setNegativeButton(R.string.str_add_site_neg_button,
                            (dialog, which) -> closeOptionsMenu())
                    .setPositiveButton(R.string.str_delete_site,
                            (dialog, which) -> {
                                NewsRssItem removedItem = NewsRssItem.getLikedNews().get(info.position);
                                StringBuilder gotXml = new StringBuilder();
                                try (InputStream inputStream = openFileInput("news.xml")) {
                                    Scanner scanner = new Scanner(inputStream);
                                    while (scanner.hasNext()) {
                                        gotXml.append(scanner.nextLine());
                                    }
                                } catch (IOException ignored) {
                                }
                                deleteFile("news.xml");
                                Document doc = Jsoup.parse(gotXml.toString(), "", Parser.xmlParser());
                                Elements items = doc.select("item");
                                StringBuilder resXml = new StringBuilder();
                                String root = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
                                String startTag = "<items>";
                                String endTag = "</items>";
                                try {
                                    for (Element docItem : items) {
                                        if (docItem.select("title").text().equals(removedItem.getTitle())) {
                                            continue;
                                        }
                                        resXml.append(docItem.toString());
                                    }
                                } catch (Exception ignored) {
                                }
                                String res = root + startTag + resXml.toString() + endTag;
                                try {
                                    openFileOutput("news.xml", MODE_APPEND).write(res.getBytes());
                                } catch (IOException ignored) {
                                }
                                NewsRssItem.getLikedNews().remove(info.position);
                                listView.setAdapter(new NewsRssItemAdapter(this,
                                        R.layout.list_item_layout, NewsRssItem.getLikedNews(), false));
                            })
                    .create();
            alertDialog.show();
        }
        return true;
    }

    public void setDefaultSettings() {
        SharedPreferences settings = getSharedPreferences(PreferenceManager.SETTINGS_NAME, MODE_MULTI_PROCESS);
        AdapterView.OnItemClickListener itemListener =
                (parent, view, position, id) -> startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(NewsRssItem.getLikedNews().get(position).getLink())));
        listView.setOnItemClickListener(itemListener);
        Comparator<NewsRssItem> defaultComparator;
        switch (settings.getString(PreferenceManager.SORT_KEY, PreferenceManager.SORT_BY_DATE)) {
            case PreferenceManager.SORT_BY_DATE:
                defaultComparator = NewsRssItem.getComparator(NewsRssItem.ItemComparators.SORT_BY_DATE);
                break;
            case PreferenceManager.SORT_BY_SITE:
                defaultComparator = NewsRssItem.getComparator(NewsRssItem.ItemComparators.SORT_BY_SITE);
                break;
            case PreferenceManager.SORT_BY_SIZE:
                defaultComparator = NewsRssItem.getComparator(NewsRssItem.ItemComparators.SORT_BY_SIZE);
                break;
            default:
                defaultComparator = (n_1, n_2) -> 0;
        }
        Collections.sort(NewsRssItem.getLikedNews(), defaultComparator);
        listView.setAdapter(new NewsRssItemAdapter(this,
                R.layout.list_item_layout, NewsRssItem.getLikedNews(), false));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_news_activity);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
        listView = findViewById(R.id.liked_news_list);
        setDefaultSettings();
        registerForContextMenu(listView);
    }
}