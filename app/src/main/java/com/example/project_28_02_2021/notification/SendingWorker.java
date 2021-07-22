package com.example.project_28_02_2021.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ForegroundInfo;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.project_28_02_2021.notification.interfaces.ISend;
import com.example.project_28_02_2021.rss.NewsRssItem;
import com.example.project_28_02_2021.R;
import com.example.project_28_02_2021.site.Site;
import com.example.project_28_02_2021.site.SiteManager;
import com.example.project_28_02_2021.util.xml.XmlFeedParser;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SendingWorker extends Worker implements ISend {

    public SendingWorker(Context context, WorkerParameters workerParams) { super(context, workerParams); }

    public static String work_id = "id_work";
    public static int interval_min = 15;
    public static TimeUnit time_unit = TimeUnit.MINUTES;
    public static String chn_id = "id";
    public static String chn_name = "Main_Thread";
    private ArrayList<NewsRssItem> items;
    private NotificationChannel channel;
    private int pos;
    private final NotificationManagerCompat notificationManager
            = NotificationManagerCompat.from(getApplicationContext());

    private final Runnable loader = () -> {
        ArrayList<Site> sites = SiteManager.getInstance(getApplicationContext(), new ArrayList<>()).getSites();
        for (Site site : sites) {
            String xmlString = "";
            try { xmlString = Jsoup.connect(site.getUrl()).get().toString(); }
            catch (Exception ignored) {}
            items = XmlFeedParser.parseFeedFromXml(xmlString, site, getApplicationContext());
        }
    };

    @Override
    public void createCurrentChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = new NotificationChannel(chn_id, chn_name, NotificationManager.IMPORTANCE_HIGH);
        try { if (channel != null) notificationManager.createNotificationChannel(channel);
        } catch (Exception ignored) {}
    }

    private PendingIntent getContentIntent(int pos) {
        Intent shareIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(items.get(pos).getLink()));
        return PendingIntent.getActivity(
                getApplicationContext(),
                0,
                shareIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    @Override
    public Notification getNotification(PendingIntent contentIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), chn_id)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.news_icon)
                .setContentText(items.get(pos).getTitle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        return builder.build();
    }

    @Override
    public void resetWork() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SendingWorker.class, interval_min, time_unit)
                .setInitialDelay(interval_min, time_unit)
                .build();
        WorkManager manager = WorkManager.getInstance(getApplicationContext());
        manager.enqueueUniquePeriodicWork(work_id, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    @Override
    public Result doWork() {
        Thread loadThread = new Thread(loader);
        loadThread.start();
        try { Thread.sleep(60 * 1000); }
        catch (InterruptedException ignored) {}
        if (items.isEmpty()) return Result.failure();
        Random random = new Random();
        pos = random.nextInt(items.size());
        createCurrentChannel();
        PendingIntent contentIntent = getContentIntent(pos);
        Notification notification = getNotification(contentIntent);
        notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
        ForegroundInfo info = new ForegroundInfo(0, notification, 0);
        setForegroundAsync(info);
        notificationManager.notify(12, notification);
        resetWork();
        return Result.success();
    }
}