package com.example.project_28_02_2021.notification.interfaces;

import android.app.Notification;
import android.app.PendingIntent;

public interface ISend {
    void createCurrentChannel();
    Notification getNotification(PendingIntent intent);
    void resetWork();
}
