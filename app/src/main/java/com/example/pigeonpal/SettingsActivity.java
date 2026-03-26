package com.example.pigeonpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchAutomation;
    private SwitchMaterial switchNotifications;
    private Button btnSaveSettings;
    private TextView tvSettingsStatus;
    private Button nameUpdate;
    private EditText nameInput;
    private SwitchMaterial darkModeSwitch;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        setupBottomNavigation();
        setupSaveButton();
        setupNameButtonListeners();

        // Initialize SharedPreferences
        SharedPreferences darkPref = getSharedPreferences("darkModePrefs", MODE_PRIVATE);
        editor = darkPref.edit();

        boolean isDarkModeOn = darkPref.getBoolean("isDarkModeOn", false);

        // Apply the saved theme preference
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkModeSwitch.setChecked(true);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            darkModeSwitch.setChecked(false);
        }

        // Toggle theme when switch is clicked
        darkModeSwitch.setOnClickListener(v -> {
            boolean newDarkModeState = !isDarkModeOn; // Toggle state
            if (newDarkModeState) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            // Save preference
            editor.putBoolean("isDarkModeOn", newDarkModeState);
            editor.apply();
        });
    }

    private void initializeViews() {
        switchAutomation = findViewById(R.id.switchAutomation);
        switchNotifications = findViewById(R.id.switchNotifications);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        tvSettingsStatus = findViewById(R.id.tvSettingsStatus);
        nameUpdate = findViewById(R.id.nameUpdate);
        nameInput = findViewById(R.id.nameInput);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
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
    private void setupNameButtonListeners() {
        nameUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePigeonName();
            }
        });
    }

    private void updatePigeonName() {
        String nameText = nameInput.getText().toString().trim();
        if (nameText.isEmpty()) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("PigeonPrefs", MODE_PRIVATE);
        prefs.edit().putString("pigeon_name", nameText).apply();

        Toast.makeText(this, "Pigeon name updated", Toast.LENGTH_SHORT).show();
        nameInput.setText("");
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(SettingsActivity.this, activityClass);
        startActivity(intent);
        finish();
    }
}
