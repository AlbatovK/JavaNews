package com.example.project_28_02_2021.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.project_28_02_2021.notification.ConnectionWorker;
import com.example.project_28_02_2021.rss.adapter.NewsRssItemAdapter;
import com.example.project_28_02_2021.R;
import com.example.project_28_02_2021.rss.ItemComparators;
import com.example.project_28_02_2021.rss.NewsRssItem;
import com.example.project_28_02_2021.rss.NewsRssItemManager;
import com.example.project_28_02_2021.site.Site;
import com.example.project_28_02_2021.site.SiteManager;
import com.example.project_28_02_2021.site.SiteStatusStates;
import com.example.project_28_02_2021.util.database.DataBaseHelper;
import com.example.project_28_02_2021.util.settings.PreferenceManager;
import com.example.project_28_02_2021.notification.SendingWorker;
import com.example.project_28_02_2021.util.net.NetLoader;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private NewsRssItemAdapter adapter = null;
    private DataBaseHelper dataBaseHelper = null;
    private ListView listView = null;
    private SwipeRefreshLayout refreshLayout = null;
    private Comparator<NewsRssItem> defaultComparator = null;
    private AdapterView.OnItemClickListener itemListener = (parent, view, position, id) -> { };
    private static boolean created = false;
    private final NewsRssItemManager n_manager = NewsRssItemManager.getInstance();
    private final SiteManager s_manager = SiteManager.getInstance(this, new ArrayList<>());

    private void setDefaultSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        }
        SharedPreferences settings = getSharedPreferences(PreferenceManager.SETTINGS_NAME, MODE_MULTI_PROCESS);
        SwipeRefreshLayout.OnRefreshListener listener = () -> {
            Runnable refresher = () -> {
                listView.setOnItemClickListener((parent, view, position, id) -> {
                });
                itemListener = (parent, view, position, id) -> {
                };
                listView.post(() -> {
                    listView.setOnItemClickListener(itemListener);
                    setTitle(R.string.str_wait);
                });
                listView.post(() -> adapter.notifyDataSetChanged());
                if (!adapter.isEmpty()) {
                    adapter.clear();
                }
                if (!s_manager.getSites().isEmpty()) {
                    s_manager.getSites().clear();
                }
                if (!n_manager.getNews().isEmpty()) {
                    n_manager.getNews().clear();
                }
                dataBaseHelper.getReadableDatabase();
                s_manager.getSites().addAll(dataBaseHelper.getSites());
                for (Site site : s_manager.getSites()) {
                    NetLoader.loadFromSite(this, site);
                    try {
                        while (site.getStatus() == SiteStatusStates.UNFILLED_STATE) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException ignored) {
                        site.setStatus(SiteStatusStates.UNKNOWN_STATE);
                    }
                }
                if (n_manager.getNews().isEmpty()) {
                    Snackbar.make(listView,
                            getString(R.string.str_net_exception), Snackbar.LENGTH_LONG).show();
                    Log.d("!!!", "OK");
                    PeriodicWorkRequest request
                            = new PeriodicWorkRequest.Builder(ConnectionWorker.class,
                            ConnectionWorker.interval_min,
                            ConnectionWorker.time_unit).build();
                    WorkManager manager = WorkManager.getInstance(getApplicationContext());
                    manager.enqueueUniquePeriodicWork(ConnectionWorker.work_id, ExistingPeriodicWorkPolicy.KEEP, request);
                } else {
                    String message = getString(R.string.str_upload_news_data, n_manager.getNews().size(),
                            getApplicationContext().getResources().getQuantityString(R.plurals.items_plurals, n_manager.getNews().size()),
                            s_manager.getSites().size(),
                            getResources().getQuantityString(R.plurals.sites_plurals, s_manager.getSites().size())
                    );
                    Snackbar.make(listView, message, Snackbar.LENGTH_LONG).show();
                }
                Collections.sort(n_manager.getNews(), defaultComparator);
                setDefaultSettings();
                listView.post(() -> adapter.notifyDataSetChanged());
                itemListener = (parent, view, position, id) -> startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(n_manager.getNews().get(position).getLink())));
                listView.post(() -> {
                    listView.setOnItemClickListener(itemListener);
                    setTitle(R.string.app_name);
                });
                refreshLayout.setRefreshing(false);
            };
            Thread refresherThread = new Thread(refresher);
            refresherThread.start();
        };
        refreshLayout.setOnRefreshListener(listener);
        switch (settings.getString(PreferenceManager.SORT_KEY, PreferenceManager.SORT_BY_DATE)) {
            case PreferenceManager.SORT_BY_DATE:
                defaultComparator = NewsRssItemManager.getComparator(ItemComparators.SORT_BY_DATE);
                break;
            case PreferenceManager.SORT_BY_SITE:
                defaultComparator = NewsRssItemManager.getComparator(ItemComparators.SORT_BY_SITE);
                break;
            case PreferenceManager.SORT_BY_SIZE:
                defaultComparator = NewsRssItemManager.getComparator(ItemComparators.SORT_BY_SIZE);
                break;
        }
        Collections.sort(n_manager.getNews(), defaultComparator);
        if (settings.getString(PreferenceManager.FILTER_KEY,
                PreferenceManager.NONE_FILTER_MODE).equals(PreferenceManager.NONE_FILTER_MODE)) {
            adapter = new NewsRssItemAdapter(this, R.layout.list_item_layout, n_manager.getNews(), true);
            listView.post(() -> listView.setAdapter(adapter));
            itemListener = (parent, view, position, id) -> startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(n_manager.getNews().get(position).getLink())));
        } else {
            String[] tags = null;
            try {
                openFileOutput("tags.xml", MODE_APPEND);
            } catch (Exception ignored) {
            }
            try (InputStream in_stream = openFileInput("tags.xml")) {
                StringBuilder tmp_str = new StringBuilder();
                Scanner in_scan = new Scanner(in_stream);
                while (in_scan.hasNext()) {
                    tmp_str.append(in_scan.next());
                }
                Document doc = Jsoup.parse(tmp_str.toString(), " ", Parser.xmlParser());
                tags = doc.select("tag").text().split(" ");
            } catch (Exception ignored) {
            }

            assert tags != null;
            ArrayList<NewsRssItem> filteredNews = new ArrayList<>();
            for (NewsRssItem item : n_manager.getNews()) {
                for (String tag : tags) {
                    String lower_tag = tag.toLowerCase().trim();
                    if (item.getCategoryWords().contains(lower_tag)) {
                        filteredNews.add(item);
                    }
                }
            }
            adapter = new NewsRssItemAdapter(this, R.layout.list_item_layout, filteredNews, true);
            listView.post(() -> listView.setAdapter(adapter));
            itemListener = (parent, view, position, id) -> startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(filteredNews.get(position).getLink())));
        }
        listView.post(() -> listView.setOnItemClickListener(itemListener));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_new_site) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.add_site_dialog, null);
            AlertDialog alertDialog = builder
                    .setTitle(R.string.str_add_new_site)
                    .setMessage(R.string.str_add_site_descr_dialog)
                    .setIcon(R.drawable.rss_icon)
                    .setView(view)
                    .setPositiveButton(R.string.str_add_site_pos_button, (dialog, which) -> {
                        EditText nameInput = view.findViewById(R.id.edit_dialog_name_text_view);
                        EditText addressInput = view.findViewById(R.id.edit_dialog_address_text_view);
                        String name = nameInput.getText().toString();
                        String address = addressInput.getText().toString();
                        Pattern urlPattern = Patterns.WEB_URL;
                        Matcher matcher = urlPattern.matcher(address.toLowerCase());
                        if (name.isEmpty() || address.isEmpty() || !matcher.matches()) {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.str_invalid_data),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            AtomicBoolean isValidUrl = new AtomicBoolean(true);
                            Runnable checkSite = () -> {
                                Looper.prepare();
                                try {
                                    Jsoup.connect(address).get();
                                } catch (Exception ignored) {
                                    isValidUrl.set(false);
                                    Toast.makeText(getApplicationContext(), getString(R.string.str_invalid_data),
                                            Toast.LENGTH_SHORT).show();
                                }
                            };
                            Thread checkThread = new Thread(checkSite);
                            checkThread.start();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ignored) {
                            }
                            if (isValidUrl.get()) {
                                dataBaseHelper.addSite(new Site(name, address, this));
                            }
                        }
                    })
                    .setNegativeButton(R.string.str_add_site_neg_button, (dialog, which) -> closeOptionsMenu())
                    .create();
            alertDialog.show();
        } else if (id == R.id.preferences) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.liked_items) {
            startActivity(new Intent(this, LikedNewsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(PreferenceManager.SETTINGS_NAME, MODE_MULTI_PROCESS);
        if (settings.getString(PreferenceManager.CREATED_KEY, PreferenceManager.NONE_CREATED).equalsIgnoreCase(PreferenceManager.NONE_CREATED)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog = builder
                    .setIcon(R.drawable.news_icon)
                    .setTitle(R.string.str_enter_title)
                    .setMessage(R.string.str_enter_dialogue)
                    .setPositiveButton(R.string.str_ok, (dialog1, which) -> closeContextMenu())
                    .create();
            dialog.show();
            PreferenceManager manager = new PreferenceManager(this);
            manager.setValueByKey(
                    new PreferenceManager.PreferencePair(PreferenceManager.CREATED_KEY, PreferenceManager.CREATED)
            );
        }

        listView = findViewById(R.id.list_view);
        refreshLayout = findViewById(R.id.swipe);
        AssetManager assetManager = getAssets();
        ArrayList<String> exe_rows = new ArrayList<>();
        try (InputStream inputStream = getAssets().open("sites_table.sql")) {
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNext()) {
                exe_rows.add(scanner.nextLine());
            }
        } catch (IOException ignored) {
        }
        dataBaseHelper = new DataBaseHelper(getApplicationContext(), exe_rows);
        registerForContextMenu(listView);
        setDefaultSettings();
        if (!created) {
            if (n_manager.getNews().isEmpty()) {
                Snackbar.make(listView,
                        getString(R.string.str_net_exception), Snackbar.LENGTH_LONG).show();
                PeriodicWorkRequest request
                        = new PeriodicWorkRequest.Builder(ConnectionWorker.class,
                        ConnectionWorker.interval_min,
                        ConnectionWorker.time_unit).build();
                WorkManager manager = WorkManager.getInstance(getApplicationContext());
                manager.enqueueUniquePeriodicWork(ConnectionWorker.work_id, ExistingPeriodicWorkPolicy.KEEP, request);

            } else {
                String message = getString(R.string.str_upload_news_data, n_manager.getNews().size(),
                        getApplicationContext().getResources().getQuantityString(R.plurals.items_plurals, n_manager.getNews().size()),
                        s_manager.getSites().size(),
                        getResources().getQuantityString(R.plurals.sites_plurals, s_manager.getSites().size())
                );
                Snackbar.make(listView, message, Snackbar.LENGTH_LONG).show();
            }
            Collections.sort(n_manager.getNews(), defaultComparator);
            created = true;
        }
        n_manager.getLikedNews().clear();
        StringBuilder xmlString = new StringBuilder();
        try (InputStream inputStream = openFileInput("news.xml")) {
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNext()) {
                xmlString.append(scanner.nextLine());
            }
        } catch (IOException ignored) {
        }
        org.jsoup.nodes.Document doc = Jsoup.parse(xmlString.toString(), "", Parser.xmlParser());
        Elements news = doc.select("item");
        for (org.jsoup.nodes.Element item : news) {
            NewsRssItem newsRssItem = new NewsRssItem(
                    s_manager.getSiteByName(item.select("site").text(), this),
                    item.select("link").text(), item.select("title").text(),
                    "", item.select("pubDate").text(),
                    this);
            boolean notAgain = true;
            for (NewsRssItem it : n_manager.getLikedNews()) {
                if (it.isEqual(newsRssItem)) {
                    notAgain = false;
                    break;
                }
            }
            if (notAgain) {
                n_manager.getLikedNews().add(newsRssItem);
            }
        }
    }
}
