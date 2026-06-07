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

public class ReminderNotificationHelper {

    private static final String CHANNEL_ID =
            "daily_reminder_channel";

    private static final int NOTIFICATION_ID = 2001;

    public static void show(
            Context context,
            ArrayList<ChecklistItem> items
    ) {
        if (items.isEmpty()) {
            return;
        }

        createChannel(context);

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(
                context,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        2001,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.InboxStyle style =
                new NotificationCompat.InboxStyle()
                        .setBigContentTitle("완료하지 않은 항목");

        int count = Math.min(items.size(), 5);

        for (int i = 0; i < count; i++) {
            style.addLine("• " + items.get(i).getTitle());
        }

        if (items.size() > count) {
            style.setSummaryText(
                    "외 " + (items.size() - count) + "개"
            );
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                R.drawable.ic_notification
                        )
                        .setContentTitle(
                                "아직 미완료 항목이 있습니다"
                        )
                        .setContentText(
                                "남은 항목: " + items.size() + "개"
                        )
                        .setStyle(style)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setCategory(
                                NotificationCompat.CATEGORY_REMINDER
                        )
                        .setDefaults(
                                NotificationCompat.DEFAULT_ALL
                        );

        try {
            NotificationManagerCompat.from(context)
                    .notify(
                            NOTIFICATION_ID,
                            builder.build()
                    );
        } catch (SecurityException ignored) {
            // 알림 권한이 거부된 경우 표시하지 않습니다.
        }
    }

    public static void cancel(Context context) {
        NotificationManagerCompat.from(context)
                .cancel(NOTIFICATION_ID);
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "하루 종료 알림",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "하루가 끝나기 전에 미완료 항목을 알려줍니다."
            );

            channel.enableVibration(true);

            NotificationManager manager =
                    context.getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
