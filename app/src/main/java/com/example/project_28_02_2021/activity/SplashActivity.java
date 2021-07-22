package com.example.project_28_02_2021.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.project_28_02_2021.R;
import com.example.project_28_02_2021.util.files.FileManager;
import com.example.project_28_02_2021.site.Site;
import com.example.project_28_02_2021.site.SiteManager;
import com.example.project_28_02_2021.site.SiteStatusStates;
import com.example.project_28_02_2021.util.settings.PreferenceManager;
import com.example.project_28_02_2021.notification.SendingWorker;
import com.example.project_28_02_2021.util.net.NetLoader;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Custom_theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Objects.requireNonNull(getSupportActionBar()).hide();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SendingWorker.class, SendingWorker.interval_min, TimeUnit.MINUTES)
                .setInitialDelay(SendingWorker.interval_min, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(SendingWorker.chn_id, ExistingPeriodicWorkPolicy.REPLACE, request);
        PreferenceManager manager = new PreferenceManager(this);
        manager.setValueByKey(
                new PreferenceManager.PreferencePair(PreferenceManager.FILTER_KEY, PreferenceManager.NONE_FILTER_MODE)
        );
        ArrayList<String> exe_rows = new ArrayList<>();
        try { exe_rows = FileManager.getAssetData(getAssets());}
        catch (Exception ignored) {}
        ArrayList<Site> sites = SiteManager.getInstance(this, exe_rows).getSites();
        ProgressBar bar = findViewById(R.id.progress_bar);
        Thread start_thread = new Thread() {
            @Override public void run() {
                bar.post( () -> bar.setMax(sites.size()) );
                for (Site site : sites) {
                    NetLoader.loadFromSite(getApplicationContext(), site);
                    try { while (site.getStatus() == SiteStatusStates.UNFILLED_STATE) Thread.sleep(100); }
                    catch (InterruptedException ignored) { site.setStatus(SiteStatusStates.UNKNOWN_STATE); }
                    bar.post(() -> bar.incrementProgressBy(1));
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                try { Thread.sleep(1000); }
                catch (InterruptedException ignored) {}
                startActivity(intent);
            }
        };
        start_thread.start();
    }
}