package com.example.pigeonpal;
// Package declaration - this file belongs to com.example.example1


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    // Member variables - accessible throughout the class
    private TextView tvSensorValue;
    private SwitchMaterial switchLamp;
    private TextView switchSubTxt;
    private TextView tvSensorStatus;
    private EditText etTestValue;
    private Button btnUpdate;
    private TextView tvReadingCount;  // NEW: Shows reading count

    // Color constants
    private static final int COLOR_RED = Color.parseColor("#E74C3C");
    private static final int COLOR_YELLOW = Color.parseColor("#F39C12");
    private static final int COLOR_GREEN = Color.parseColor("#27AE60");
    // Color.parseColor converts hex string to integer

    // Threshold constants
    private static final int THRESHOLD_LOW = 30;
    private static final int THRESHOLD_HIGH = 65;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupBottomNavigation();
        setupButtonListeners();
        updateReadingCount();  // NEW: Show count on startup

        switchLamp.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchSubTxt.setText("UV Lamp is on");
            } else {
                switchSubTxt.setText("UV Lamp is off");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateReadingCount();  // NEW: Update count when returning to screen
    }
// onResume is called when activity becomes visible
// Useful for refreshing data after navigating away and back

    /**
     * Find all views in the layout
     */
    private void initializeViews() {
        tvSensorValue = findViewById(R.id.tvSensorValue);
        switchSubTxt = findViewById(R.id.switchSubTxt);
        switchLamp = findViewById(R.id.switchLamp);

        /*
        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        etTestValue = findViewById(R.id.etTestValue);
        btnUpdate = findViewById(R.id.btnUpdate);
        tvReadingCount = findViewById(R.id.tvReadingCount);  // NEW
         */
    }

    /**
     * Set up bottom navigation
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_graphs) {
                navigateToActivity(GraphsActivity.class);
                return true;
            } else if (itemId == R.id.nav_settings) {
                navigateToActivity(SettingsActivity.class);
                return true;
            }
            return false;
        });
    }

    /**
     * Set up button click listeners
     */
    private void setupButtonListeners() {
        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateSensorValue();
                }
            });
        }
    }

    /**
     * Update sensor value display
     */

    private void updateSensorValue() {
        if (etTestValue == null) return;

        // Get text from input field
        String inputText = etTestValue.getText().toString().trim();
        // .getText() returns Editable object
        // .toString() converts to String
        // .trim() removes whitespace
        // Check if empty
        if (inputText.isEmpty()) {
            Toast.makeText(this, "Please enter a value", Toast.LENGTH_SHORT).show();
            // Toast = small popup message
            return;  // Exit method early
        }

        try {
            // Parse as integer
            int value = Integer.parseInt(inputText);
            // Can throw NumberFormatException if not a number

            // Validate range (0-100)
            if (value < 0 || value > 100) {
                Toast.makeText(this, "Please enter value between 0 and 100",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // NEW: Save to DataManager with timestamp
            DataManager.getInstance().addReading(value);
            // This saves the value AND current time automatically

            // Update display (same as before)
            if (tvSensorValue != null) {
                tvSensorValue.setText(String.valueOf(value));
                updateValueColor(value);
            }

            // NEW: Update the reading count display
            updateReadingCount();
            // Clear input
            etTestValue.setText("");

            // NEW: Show confirmation with count
            int count = DataManager.getInstance().getReadingCount();
            Toast.makeText(this, "Saved! Total readings: " + count,
                    Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            // Runs if parseInt fails (e.g., user entered "abc")
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update text color based on value
     *
     * COLOR RULES:
     * - value < 30: RED (Low)
     * - 30 <= value <= 65: YELLOW (Medium)
     * - value > 65: GREEN (High)
     */
    private void updateValueColor(int value) {
        if (tvSensorValue == null || tvSensorStatus == null) return;

        if (value < THRESHOLD_LOW) {
            // LOW VALUE - RED
            tvSensorValue.setTextColor(COLOR_RED);
            tvSensorStatus.setText("Low");
            tvSensorStatus.setTextColor(COLOR_RED);

        } else if (value <= THRESHOLD_HIGH) {
            // MEDIUM VALUE - YELLOW
            // We know value >= 30 from first if
            tvSensorValue.setTextColor(COLOR_YELLOW);
            tvSensorStatus.setText("Medium");
            tvSensorStatus.setTextColor(COLOR_YELLOW);

        } else {
            // HIGH VALUE - GREEN
            // We know value > 65
            tvSensorValue.setTextColor(COLOR_GREEN);
            tvSensorStatus.setText("High");
            tvSensorStatus.setTextColor(COLOR_GREEN);
        }
    }
    /**
     * NEW: Update the reading count display
     */
    private void updateReadingCount() {
        int count = DataManager.getInstance().getReadingCount();
        if (tvReadingCount != null) {
            tvReadingCount.setText("Readings saved: " + count);
        }
    }
    /**
     * Helper method for navigation
     */
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
        finish();
    }

}