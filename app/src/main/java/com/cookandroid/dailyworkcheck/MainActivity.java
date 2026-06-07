package com.cookandroid.dailyworkcheck;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ListView listChecklist;
    private Button buttonAdd;
    private Button buttonSettings;

    private ChecklistRepository repository;
    private ChecklistAdapter adapter;

    private final ArrayList<ChecklistItem> items = new ArrayList<>();

    private final ActivityResultLauncher<String>
            notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            updateChecklistNotification();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listChecklist = findViewById(R.id.listChecklist);
        buttonAdd = findViewById(R.id.buttonAdd);

        repository = new ChecklistRepository(this);

        setupChecklistAdapter();
        setupAddButton();

        NotificationHelper.createChannel(this);
        requestNotificationPermission();

        buttonSettings = findViewById(R.id.buttonSettings);

        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );

            startActivity(intent);
        });
    }

    private void setupChecklistAdapter() {
        adapter = new ChecklistAdapter(
                this,
                items,
                new ChecklistAdapter.OnItemActionListener() {
                    @Override
                    public void onOpenLink(ChecklistItem item) {
                        openWebPage(item.getUrl());
                    }

                    @Override
                    public void onEdit(ChecklistItem item) {
                        openEditActivity(item.getId());
                    }

                    @Override
                    public void onDelete(ChecklistItem item) {
                        repository.deleteItem(item.getId());
                        loadChecklist();
                    }

                    @Override
                    public void onCompletedChanged(
                            ChecklistItem item,
                            boolean completed
                    ) {
                        item.setCompleted(completed);
                        repository.updateItem(item);

                        adapter.notifyDataSetChanged();

                        NotificationHelper.updateNotification(
                                MainActivity.this,
                                items
                        );
                    }
                }
        );

        listChecklist.setAdapter(adapter);
    }

    private void setupAddButton() {
        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    EditItemActivity.class
            );

            startActivity(intent);
        });
    }

    private void openEditActivity(long itemId) {
        Intent intent = new Intent(
                MainActivity.this,
                EditItemActivity.class
        );

        intent.putExtra("itemId", itemId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        repository.resetCompletedItemsIfNewDay();
        loadChecklist();
    }

    private void loadChecklist() {
        items.clear();
        items.addAll(repository.getItems());

        adapter.notifyDataSetChanged();

        NotificationHelper.updateNotification(
                this,
                items
        );
    }

    private void updateChecklistNotification() {
        try {
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);

            if (!notificationManager.areNotificationsEnabled()) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            NotificationHelper.updateNotification(this, items);

        } catch (SecurityException e) {
            Log.e(
                    TAG,
                    "알림 권한이 없어 알림을 표시하지 못했습니다.",
                    e
            );

        } catch (IllegalArgumentException e) {
            Log.e(
                    TAG,
                    "알림 아이콘 또는 알림 설정에 문제가 있습니다.",
                    e
            );
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
            );
        }
    }

    private void openWebPage(String savedUrl) {
        if (savedUrl == null || savedUrl.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "등록된 링크가 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String url = savedUrl.trim();

        if (url.isEmpty()) {
            Toast.makeText(this, "주소 또는 실행할 앱이 지정되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

// 2. 웹 링크 처리 (http:// 또는 https://로 시작하는 경우)
        if (url.startsWith("http://") || url.startsWith("https://")) {
            Uri webpage = Uri.parse(url);

            // 기존에 있던 웹 주소 유효성 검사 유지
            if (webpage.getHost() == null) {
                Toast.makeText(this, "올바른 링크가 아닙니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, webpage);
            try {
                startActivity(browserIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "링크를 열 수 있는 브라우저가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
// 3. 패키지명 처리 (웹 링크가 아닌 경우 -> 기기 내 앱 실행)
        else {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(url);

            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                // 가상 기기에 해당 앱(유튜브 등)이 없을 때 처리
                Toast.makeText(this, "가상 기기에 해당 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}