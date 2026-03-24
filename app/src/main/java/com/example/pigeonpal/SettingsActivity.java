package com.example.pigeonpal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchAutomation;
    private SwitchMaterial switchNotifications;
    private Button btnSaveSettings;
    private TextView tvSettingsStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setupBottomNavigation();
        setupSaveButton();
    }

    private void initializeViews() {
        switchAutomation = findViewById(R.id.switchAutomation);
        switchNotifications = findViewById(R.id.switchNotifications);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        tvSettingsStatus = findViewById(R.id.tvSettingsStatus);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                navigateToActivity(MainActivity.class);
                return true;
            } else if (itemId == R.id.nav_graphs) {
                navigateToActivity(GraphsActivity.class);
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }

    private void setupSaveButton() {
        btnSaveSettings.setOnClickListener(v -> {
            // Get switch states
            boolean automationEnabled = switchAutomation.isChecked();
            // isChecked() returns true if ON, false if OFF

            boolean notificationsEnabled = switchNotifications.isChecked();

            // In real app, save to SharedPreferences or Firebase
            // For now, just show confirmation
            tvSettingsStatus.setText("Settings saved!");

            // Log values (visible in Logcat panel)
            android.util.Log.d("Settings",
                    "Automation: " + automationEnabled +
                            ", Notifications: " + notificationsEnabled);
        });
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(SettingsActivity.this, activityClass);
        startActivity(intent);
        finish();
    }
}