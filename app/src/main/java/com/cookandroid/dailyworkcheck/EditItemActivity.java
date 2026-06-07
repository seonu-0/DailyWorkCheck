package com.cookandroid.dailyworkcheck;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class EditItemActivity extends AppCompatActivity {

    private EditText editTitle;
    private EditText editUrl;
    private Button buttonSave;
    private Spinner spinnerApp;

    private ChecklistRepository repository;
    private long itemId = -1;
    private ChecklistItem editingItem;

    private List<String> appNames = new ArrayList<>();
    private List<String> appPackages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        editTitle = findViewById(R.id.editTitle);
        editUrl = findViewById(R.id.editUrl);
        spinnerApp = findViewById(R.id.spinnerApp);
        buttonSave = findViewById(R.id.buttonSave);

        // 1. 휴대폰에 설치된 모든 앱 목록을 동적으로 가져오기
        loadInstalledApps();

        // 2. 동적으로 만든 리스트를 스피너 어댑터에 연결
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, appNames);
        spinnerApp.setAdapter(adapter);

        repository = new ChecklistRepository(this);
        itemId = getIntent().getLongExtra("itemId", -1);

        if (itemId != -1) {
            editingItem = repository.getItemById(itemId);
            if (editingItem != null) {
                editTitle.setText(editingItem.getTitle());
                String savedUrl = editingItem.getUrl();

                if (savedUrl.startsWith("http://") || savedUrl.startsWith("https://")) {
                    editUrl.setText(savedUrl);
                    spinnerApp.setSelection(0);
                } else {
                    editUrl.setText("");
                    // List에서 일치하는 패키지명 위치 찾기
                    int matchIndex = appPackages.indexOf(savedUrl);
                    if (matchIndex != -1) {
                        spinnerApp.setSelection(matchIndex);
                    } else {
                        spinnerApp.setSelection(0);
                    }
                }
            }
        }

        // 저장 버튼 클릭 리스너 (기존과 거의 동일하나 배열 대신 List 사용)
        buttonSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String urlInput = editUrl.getText().toString().trim();
            int selectedAppIndex = spinnerApp.getSelectedItemPosition();

            if (title.isEmpty()) {
                editTitle.setError("출석/과제의 제목을 입력하세요");
                return;
            }

            String finalTargetValue = "";
            if (!urlInput.isEmpty()) {
                finalTargetValue = urlInput;
            } else if (selectedAppIndex > 0) {
                // ⭕ 배열 대신 List에서 값을 가져옴
                finalTargetValue = appPackages.get(selectedAppIndex);
            }

            if (finalTargetValue.isEmpty()) {
                Toast.makeText(this, "웹 링크를 입력하거나 실행할 앱을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (itemId == -1) {
                repository.addItem(title, finalTargetValue);
            } else if (editingItem != null) {
                editingItem.setTitle(title);
                editingItem.setUrl(finalTargetValue);
                repository.updateItem(editingItem);
            }

            finish();
        });
    }

    // 💡 [핵심 메서드] 스마트폰 내부의 앱 목록을 긁어오는 기능
    private void loadInstalledApps() {
        // 첫 번째 항목은 기본 선택값으로 비워둠
        appNames.add("선택 안 함(웹 링크 사용)");
        appPackages.add("");

        PackageManager pm = getPackageManager();
        // 기기에 설치된 모든 앱 정보 가져오기
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            // 시스템 앱(기본 설정 등)을 제외하고, 사용자가 실행할 수 있는 '런처 아이콘'이 있는 앱만 필터링
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                String label = app.loadLabel(pm).toString(); // 사용자가 보는 앱 이름 (예: "유튜브")
                String packageName = app.packageName;        // 실제 패키지명 (예: "com.google.android.youtube")

                appNames.add(label);
                appPackages.add(packageName);
            }
        }

    }
}



//        buttonSave.setOnClickListener(v -> {
//            String title = editTitle.getText().toString().trim();
//            String url = editUrl.getText().toString().trim();
//
//            if (title.isEmpty()) {
//                editTitle.setError("출석/과제의 제목을 입력하세요");
//                return;
//            }
//
//            if (itemId == -1) {
//                repository.addItem(title, url);
//            } else if (editingItem != null) {
//                editingItem.setTitle(title);
//                editingItem.setUrl(url);
//                repository.updateItem(editingItem);
//            }
//
//            finish();
//        });