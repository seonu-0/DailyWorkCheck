package com.cookandroid.dailyworkcheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        if (!ReminderScheduler.isEnabled(context)) {
            return;
        }

        ChecklistRepository repository =
                new ChecklistRepository(context);

        ArrayList<ChecklistItem> items =
                repository.getItems();

        ArrayList<ChecklistItem> incompleteItems =
                new ArrayList<>();

        for (ChecklistItem item : items) {
            if (!item.isCompleted()) {
                incompleteItems.add(item);
            }
        }

        if (incompleteItems.isEmpty()) {
            ReminderNotificationHelper.cancel(context);
        } else {
            ReminderNotificationHelper.show(
                    context,
                    incompleteItems
            );
        }

        // setAndAllowWhileIdle은 한 번만 실행됨 -> 다음 날 알람을 다시 예약
        ReminderScheduler.scheduleSaved(context);
    }
}