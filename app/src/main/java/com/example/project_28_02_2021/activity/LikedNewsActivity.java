package com.example.project_28_02_2021.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_28_02_2021.rss.adapter.NewsRssItemAdapter;
import com.example.project_28_02_2021.R;
import com.example.project_28_02_2021.util.files.FileManager;
import com.example.project_28_02_2021.rss.ItemComparators;
import com.example.project_28_02_2021.rss.NewsRssItem;
import com.example.project_28_02_2021.rss.NewsRssItemManager;
import com.example.project_28_02_2021.util.settings.PreferenceManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class LikedNewsActivity extends AppCompatActivity {

    private ListView listView = null;
    private final NewsRssItemManager manager = NewsRssItemManager.getInstance();

    private final AdapterView.OnItemClickListener itemListener = (parent, view, position, id) -> {
        Uri uri = Uri.parse(manager.getLikedNews().get(position - 1).getLink());
        Intent web_intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(web_intent);
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_liked_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_all) {
            manager.clearLikedNews();
            NewsRssItemAdapter adapter =
                    new NewsRssItemAdapter(this,
                            R.layout.list_item_layout,
                            manager.getLikedNews(),
                            false);
            listView.setAdapter(adapter);
            findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            FileManager.deleteFile(this, FileManager.liked_news_storage);
            return true;
        } else return super.onOptionsItemSelected(item);
    }


    public void setDefaultSettings() {
        TextView emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        if (manager.getLikedNews().isEmpty())
            return;
        listView.setOnItemClickListener(itemListener);
        Comparator<NewsRssItem> defaultComparator;
        PreferenceManager manager = new PreferenceManager(this);
        switch (manager.getString(PreferenceManager.SORT_KEY, PreferenceManager.SORT_BY_DATE, this)) {
            case PreferenceManager.SORT_BY_DATE:
                defaultComparator = NewsRssItemManager.getComparator(ItemComparators.SORT_BY_DATE);
                break;
            case PreferenceManager.SORT_BY_SITE:
                defaultComparator = NewsRssItemManager.getComparator(ItemComparators.SORT_BY_SITE);
                break;
            case PreferenceManager.SORT_BY_SIZE:
                defaultComparator = NewsRssItemManager.getComparator(ItemComparators.SORT_BY_SIZE);
                break;
            default:
                defaultComparator = (n_1, n_2) -> 0;
            }
            Collections.sort(this.manager.getLikedNews(), defaultComparator);
            View footer = LayoutInflater.from(this).inflate(R.layout.favorites_footer, listView, false);
            View.OnClickListener footer_listener = (v) -> listView.smoothScrollToPosition(0);
            footer.setOnClickListener(footer_listener);
            listView.setFooterDividersEnabled(true);
            listView.addFooterView(footer);
            View header = LayoutInflater.from(this).inflate(R.layout.favorites_header, listView, false);
            View.OnClickListener header_listener = (v) -> listView.smoothScrollToPosition(listView.getAdapter().getCount() - 1);
            header.setOnClickListener(header_listener);
            listView.setHeaderDividersEnabled(true);
            listView.addHeaderView(header);
            NewsRssItemAdapter adapter =
                    new NewsRssItemAdapter(this,
                            R.layout.list_item_layout,
                            this.manager.getLikedNews(),
                            false);
            listView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        listView = findViewById(R.id.liked_news_list);
        setDefaultSettings();
    }
}