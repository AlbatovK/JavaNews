package com.example.project_28_02_2021;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Custom_theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SendingWorker.class, 15, TimeUnit.MINUTES).
                setInitialDelay(15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("id", ExistingPeriodicWorkPolicy.REPLACE, request);
        PreferenceManager manager = new PreferenceManager(this);
        manager.setValueByKey(
                new PreferenceManager.PreferencePair(PreferenceManager.FILTER_KEY, PreferenceManager.NONE_FILTER_MODE)
        );
        Objects.requireNonNull(getSupportActionBar()).hide();
        AssetManager assetManager = getAssets();
        ArrayList<String> exe_rows = new ArrayList<>();
        try (InputStream inputStream = assetManager.open("sites_table.sql")) {
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNext()) {
                exe_rows.add(scanner.nextLine());
            }
        } catch (IOException ignored) {
        }
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext(), exe_rows);
        dataBaseHelper.getReadableDatabase();
        Site.getSites().addAll(dataBaseHelper.getSites());
        ProgressBar bar = findViewById(R.id.progress_bar);
        Thread netThread = new Thread() {

            @Override
            public void run() {
                bar.post(() -> bar.setMax(Site.getSites().size()));
                for (Site site : Site.getSites()) {
                    RssParser parser = new RssParser(site, getApplicationContext());
                    parser.start();
                    try {
                        while (site.getStatus() == Site.SiteStatusStates.UNFILLED_STATE) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException ignored) {
                        site.setStatus(Site.SiteStatusStates.UNKNOWN_STATE);
                    }
                    bar.post(() -> bar.incrementProgressBy(1));
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        };

        netThread.start();
    }
}