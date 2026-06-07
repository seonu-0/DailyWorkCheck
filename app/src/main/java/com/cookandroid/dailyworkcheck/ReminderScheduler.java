package com.cookandroid.dailyworkcheck;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;

public class ReminderScheduler {

    private static final String PREF_NAME = "reminder_settings";
    private static final String KEY_ENABLED = "reminder_enabled";
    private static final String KEY_HOUR = "reminder_hour";
    private static final String KEY_MINUTE = "reminder_minute";

    private static final int REQUEST_CODE = 2001;

    public static void turnOn(Context context, int hour, int minute) {
        saveSettings(context, true, hour, minute);
        schedule(context, hour, minute);
    }

    public static void turnOff(Context context) {
        SharedPreferences preferences = getPreferences(context);

        preferences.edit()
                .putBoolean(KEY_ENABLED, false)
                .apply();

        cancel(context);
    }

    public static void updateTime(
            Context context,
            int hour,
            int minute
    ) {
        SharedPreferences preferences = getPreferences(context);

        preferences.edit()
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .apply();

        if (isEnabled(context)) {
            schedule(context, hour, minute);
        }
    }

    public static boolean isEnabled(Context context) {
        return getPreferences(context)
                .getBoolean(KEY_ENABLED, false);
    }

    public static int getHour(Context context) {
        return getPreferences(context)
                .getInt(KEY_HOUR, 21);
    }

    public static int getMinute(Context context) {
        return getPreferences(context)
                .getInt(KEY_MINUTE, 0);
    }

    public static void scheduleSaved(Context context) {
        if (!isEnabled(context)) {
            return;
        }

        schedule(
                context,
                getHour(context),
                getMinute(context)
        );
    }

    private static void schedule(
            Context context,
            int hour,
            int minute
    ) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        Calendar alarmTime = Calendar.getInstance();

        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        // 오늘 설정 시간이 이미 지났다면 다음 날로 예약합니다.
        if (alarmTime.getTimeInMillis()
                <= System.currentTimeMillis()) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        PendingIntent pendingIntent =
                createPendingIntent(context);

        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            return;
        }

        PendingIntent pendingIntent =
                createPendingIntent(context);

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private static PendingIntent createPendingIntent(
            Context context
    ) {
        Intent intent = new Intent(
                context,
                ReminderReceiver.class
        );

        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void saveSettings(
            Context context,
            boolean enabled,
            int hour,
            int minute
    ) {
        getPreferences(context)
                .edit()
                .putBoolean(KEY_ENABLED, enabled)
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .apply();
    }

    private static SharedPreferences getPreferences(
            Context context
    ) {
        return context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }
}