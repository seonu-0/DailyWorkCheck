package com.cookandroid.dailyworkcheck;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChecklistRepository {
    private static final String PREF_NAME = "checklist_pref";
    private static final String KEY_ITEMS = "items";

    private static final String KEY_LAST_RESET_DATE = "last_reset_date";

    private SharedPreferences preferences;

    public ChecklistRepository(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public ArrayList<ChecklistItem> getItems() {
        ArrayList<ChecklistItem> items = new ArrayList<>();
        String savedText = preferences.getString(KEY_ITEMS, "");

        if (savedText.isEmpty()) {
            return items;
        }

        String[] lines = savedText.split("\n");

        for (String line : lines) {
            String[] parts = line.split("\\|", -1);

            if (parts.length == 4) {
                long id = Long.parseLong(parts[0]);
                String title = parts[1];
                String url = parts[2];
                boolean completed = Boolean.parseBoolean(parts[3]);

                items.add(new ChecklistItem(id, title, url, completed));
            }
        }

        return items;
    }

    public void addItem(String title, String url) {
        ArrayList<ChecklistItem> items = getItems();

        long id = System.currentTimeMillis();
        ChecklistItem newItem = new ChecklistItem(id, title, url, false);

        items.add(newItem);
        saveItems(items);
    }

    public void updateItem(ChecklistItem updatedItem) {
        ArrayList<ChecklistItem> items = getItems();

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == updatedItem.getId()) {
                items.set(i, updatedItem);
                break;
            }
        }

        saveItems(items);
    }

    public void saveItems(ArrayList<ChecklistItem> items) {
        StringBuilder builder = new StringBuilder();

        for (ChecklistItem item : items) {
            builder.append(item.getId())
                    .append("|")
                    .append(item.getTitle())
                    .append("|")
                    .append(item.getUrl())
                    .append("|")
                    .append(item.isCompleted())
                    .append("\n");
        }

        preferences.edit()
                .putString(KEY_ITEMS, builder.toString())
                .apply();
    }

    public ChecklistItem getItemById(long id) {
        ArrayList<ChecklistItem> items = getItems();

        for (ChecklistItem item : items) {
            if (item.getId() == id) {
                return item;
            }
        }

        return null;
    }

    public void deleteItem(long id) {
        ArrayList<ChecklistItem> items = getItems();

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == id) {
                items.remove(i);
                break;
            }
        }

        saveItems(items);
    }

    public boolean resetCompletedItemsIfNewDay() {
        String today = getToday();

        String lastResetDate = preferences.getString(
                KEY_LAST_RESET_DATE,
                null
        );

        // 앱을 처음 실행한 경우 -> 오늘 날짜만 저장
        if (lastResetDate == null) {
            preferences.edit()
                    .putString(KEY_LAST_RESET_DATE, today)
                    .apply();

            return false;
        }

        // 아직 같은 날짜라면 초기화 X
        if (today.equals(lastResetDate)) {
            return false;
        }

        ArrayList<ChecklistItem> items = getItems();

        for (ChecklistItem item : items) {
            item.setCompleted(false);
        }

        saveItems(items);

        preferences.edit()
                .putString(KEY_LAST_RESET_DATE, today)
                .apply();

        return true;
    }

    private String getToday() {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        return dateFormat.format(new Date());
    }
}