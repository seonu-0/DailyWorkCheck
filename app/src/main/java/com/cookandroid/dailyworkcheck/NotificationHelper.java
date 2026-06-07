package com.cookandroid.dailyworkcheck;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class NotificationHelper {

    private static final String CHANNEL_ID = "checklist_channel";
    private static final int NOTIFICATION_ID = 1001;

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "체크리스트",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription("미완료 체크리스트를 표시합니다.");

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    public static void updateNotification(
            Context context,
            ArrayList<ChecklistItem> items
    ) {
        ArrayList<ChecklistItem> incompleteItems = new ArrayList<>();

        for (ChecklistItem item : items) {
            if (!item.isCompleted()) {
                incompleteItems.add(item);
            }
        }

        if (incompleteItems.isEmpty()) {
            cancelNotification(context);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createChannel(context);

        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.InboxStyle style =
                new NotificationCompat.InboxStyle()
                        .setBigContentTitle("남은 체크리스트");

        int visibleCount = Math.min(incompleteItems.size(), 5);

        for (int i = 0; i < visibleCount; i++) {
            style.addLine("• " + incompleteItems.get(i).getTitle());
        }

        if (incompleteItems.size() > visibleCount) {
            int remaining = incompleteItems.size() - visibleCount;
            style.setSummaryText("외 " + remaining + "개");
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("미완료 항목 "
                                + incompleteItems.size() + "개")
                        .setContentText(incompleteItems.get(0).getTitle())
                        .setStyle(style)
                        .setContentIntent(pendingIntent)
                        .setOngoing(true)
                        .setOnlyAlertOnce(true)
                        .setSilent(true)
                        .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelNotification(Context context) {
        NotificationManagerCompat.from(context)
                .cancel(NOTIFICATION_ID);
    }
}