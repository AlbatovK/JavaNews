package com.example.project_28_02_2021;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class SendingWorker extends Worker {

    public SendingWorker(Context context, WorkerParameters workerParams) { super(context, workerParams); }

    @Override
    public Result doWork() {
        AssetManager assetManager = getApplicationContext().getAssets();
        ArrayList<String> exe_rows = new ArrayList<>();
        try (InputStream inputStream = assetManager.open("sites_table.sql")) {
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNext()) { exe_rows.add(scanner.nextLine()); }
        } catch (IOException ignored) {}
        assetManager.close();
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext(), exe_rows);
        dataBaseHelper.getReadableDatabase();
        ArrayList<Site> site_list = new ArrayList<>(dataBaseHelper.getSites());

        ArrayList<NewsRssItem> items = new ArrayList<>();

        dataBaseHelper.close();

        Thread loadThread = new Thread() {
            @Override
            public void run() {
                try {
                    for (Site site : site_list) {
                        String xmlString = Jsoup.connect(site.getUrl()).get().toString();
                        Document doc = Jsoup.parse(xmlString, "", Parser.xmlParser());
                        for (Element item : doc.select("item")) {
                            NewsRssItem newsRssItem = new NewsRssItem(
                                    site, item.select("link").text(),
                                    item.select("title").text(),
                                    item.select("category").text().toLowerCase() + " " + item.select("description").text().toLowerCase() + " " + site.getName().toLowerCase(),
                                    item.select("pubDate").text(),
                                    item.select("image").select("url").text(),
                                    getApplicationContext());
                            items.add(newsRssItem);
                        }
                    }
                } catch (Exception ignored) {}
            }
        };
        loadThread.start();
        try { Thread.sleep(30000); } catch (InterruptedException ignored) {}

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel = new NotificationChannel("id", "Main_Thread", NotificationManager.IMPORTANCE_HIGH);
            }
        }
        try {
            if (channel != null) notificationManager.createNotificationChannel(channel);
        } catch (Exception ignored) {}
        Intent shareIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(items.get(0).getLink()));
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "id")
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.news_icon)
                .setContentTitle("JavaNews")
                .setContentText(items.get(0).getTitle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND |
                Notification.DEFAULT_VIBRATE;
        int id = 0;
        ForegroundInfo info = new ForegroundInfo(id, notification, 0);
        setForegroundAsync(info);
        notificationManager.notify(11, notification);
        return Result.success();
    }
}
