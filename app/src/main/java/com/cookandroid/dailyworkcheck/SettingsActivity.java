package com.cookandroid.dailyworkcheck;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchReminder;
    private TextView textReminderTime;
    private Button buttonSelectTime;

    private int selectedHour;
    private int selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchReminder = findViewById(R.id.switchReminder);
        textReminderTime = findViewById(R.id.textReminderTime);
        buttonSelectTime = findViewById(R.id.buttonSelectTime);

        // 저장되어 있는 설정을 불러옵니다.
        selectedHour = ReminderScheduler.getHour(this);
        selectedMinute = ReminderScheduler.getMinute(this);

        updateTimeText();

        // 리스너 연결 전에 기존 상태를 먼저 표시합니다.
        switchReminder.setChecked(
                ReminderScheduler.isEnabled(this)
        );

        switchReminder.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        ReminderScheduler.turnOn(
                                SettingsActivity.this,
                                selectedHour,
                                selectedMinute
                        );

                        Toast.makeText(
                                SettingsActivity.this,
                                "하루 종료 알림이 활성화되었습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        ReminderScheduler.turnOff(
                                SettingsActivity.this
                        );

                        Toast.makeText(
                                SettingsActivity.this,
                                "하루 종료 알림이 비활성화되었습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        buttonSelectTime.setOnClickListener(v -> {
            showTimePicker();
        });
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;

                    updateTimeText();

                    // 변경한 시간 저장
                    // 알림이 켜져 있을 시, 새 시간으로 다시 예약
                    ReminderScheduler.updateTime(
                            SettingsActivity.this,
                            selectedHour,
                            selectedMinute
                    );
                },
                selectedHour,
                selectedMinute,
                false
        );

        dialog.show();
    }

    private void updateTimeText() {
        String timeText;

        if (selectedHour == 0) {
            timeText = String.format(
                    Locale.KOREA,
                    "오전 12:%02d",
                    selectedMinute
            );
        } else if (selectedHour < 12) {
            timeText = String.format(
                    Locale.KOREA,
                    "오전 %d:%02d",
                    selectedHour,
                    selectedMinute
            );
        } else if (selectedHour == 12) {
            timeText = String.format(
                    Locale.KOREA,
                    "오후 12:%02d",
                    selectedMinute
            );
        } else {
            timeText = String.format(
                    Locale.KOREA,
                    "오후 %d:%02d",
                    selectedHour - 12,
                    selectedMinute
            );
        }

        textReminderTime.setText("알림 시간: " + timeText);
    }
}
