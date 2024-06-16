package com.erman.pomotracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.ImageButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String TASK_SWITCH = "taskSwitch";
    private static final String SESSION_SWITCH = "sessionSwitch";
    private static final String BREAK_SWITCH = "breakSwitch";

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        ImageButton settingsBackButton;
        SwitchCompat taskSwitch, sessionSwitch, breakSwitch;

        settingsBackButton = findViewById(R.id.settings_back_button);

        settingsBackButton.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            finish();
        });

        taskSwitch = findViewById(R.id.task_switch);
        sessionSwitch = findViewById(R.id.session_switch);
        breakSwitch = findViewById(R.id.break_switch);

        // Load settings (can be from SharedPreferences)
        // For simplicity, set all switches to true initially
        taskSwitch.setChecked(sharedPreferences.getBoolean(TASK_SWITCH, true));
        sessionSwitch.setChecked(sharedPreferences.getBoolean(SESSION_SWITCH, true));
        breakSwitch.setChecked(sharedPreferences.getBoolean(BREAK_SWITCH, true));

        // Save settings on switch change
        taskSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSwitchState(TASK_SWITCH, isChecked);
        });

        sessionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSwitchState(SESSION_SWITCH, isChecked);
        });

        breakSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSwitchState(BREAK_SWITCH, isChecked);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveSwitchState(String key, boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, isChecked);
        editor.apply();
    }
}