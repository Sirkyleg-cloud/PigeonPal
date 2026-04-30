package com.example.pigeonpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * MainActivity - The primary dashboard for the PigeonPal app.
 * Displays real-time sensor data and provides controls for the UV lamp, radio, and ambient lights.
 */
public class MainActivity extends AppCompatActivity {

    // UI elements for sensor data and device status
    private TextView tvSensorValue;
    private CardView uvLampCard;
    private TextView lampSubTxt;
    private ImageView ivLamp;
    private CardView radioCard;
    private TextView radioSubTxt;
    private ImageView ivRadio;
    private CardView ambientCard;
    private TextView ambientSubTxt;
    private ImageView ivAmbient;
    private Button btnRefresh;
    
    // Status variables to track device states
    private boolean uvLampStatus;
    private boolean radioStatus;
    private boolean ambientStatus;
    
    private TextView pigeonName;
    private SocketManager socketManager;

    /**
     * Listener for socket events to update the UI when data is received from the server.
     */
    private final SocketManager.SocketListener socketListener = new SocketManager.SocketListener() {
        @Override
        public void onSocketConnected() {
            Log.d("Socket", "Connected to server");
        }

        @Override
        public void onSocketDisconnected() {
            Log.d("Socket", "Disconnected from server");
        }

        @Override
        public void onMessageReceived(float temp, Boolean lamp, Boolean light, Boolean radio) {
            // Update UI on the main thread
            runOnUiThread(() -> {
                if (tvSensorValue != null) {
                    tvSensorValue.setText(String.valueOf(temp));
                }
                // Sync local state with data from the server
                uvLampStatus = lamp;
                ambientStatus = light;
                radioStatus = radio;
                
                // Update icons and text based on new states
                updateUvLampUI();
                updateRadioUI();
                updateAmbientLightUI();
            });
        }

        @Override
        public void onError(Exception e) {
            Log.e("Socket", "Error: " + e.getMessage());
            Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views and setup listeners
        initializeViews();
        setupBottomNavigation();
        setupUvLampListener();
        setupRadioListener();
        setupAmbientLightListener();
        setupRefreshListener();
        
        // Get the singleton SocketManager instance
        socketManager = SocketManager.getInstance();
        
        // Establish connection if not already active
        if (!socketManager.isConnected()) {
            socketManager.startConnection("192.168.1.20", 2005);
        }

        // Apply dark mode preference
        SharedPreferences darkPref = getSharedPreferences("darkModePrefs", MODE_PRIVATE);
        boolean isDarkModeOn = darkPref.getBoolean("isDarkModeOn", false);

        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh display name and socket listener when returning to activity
        displayPigeonName();
        socketManager.addListener(socketListener);
        updateLatestReading();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove listener to prevent memory leaks and unnecessary UI updates
        socketManager.removeListener(socketListener);
    }

    /**
     * Finds and assigns UI components from the layout.
     */
    private void initializeViews() {
        tvSensorValue = findViewById(R.id.tvSensorValue);
        uvLampCard = findViewById(R.id.uvLampCard);
        lampSubTxt = findViewById(R.id.lampSubTxt);
        ivLamp = findViewById(R.id.ivLamp);
        radioCard = findViewById(R.id.radioCard);
        radioSubTxt = findViewById(R.id.radioSubTxt);
        ivRadio = findViewById(R.id.ivRadio);
        ambientCard = findViewById(R.id.ambientCard);
        ambientSubTxt = findViewById(R.id.ambientSubTxt);
        ivAmbient = findViewById(R.id.ivAmbient);
        pigeonName = findViewById(R.id.pigeonName);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    /**
     * Reads the pigeon name from SharedPreferences and displays it.
     */
    private void displayPigeonName() {
        SharedPreferences prefs = getSharedPreferences("PigeonPrefs", MODE_PRIVATE);
        String savedName = prefs.getString("pigeon_name", "Unset");
        pigeonName.setText(savedName);
    }

    /**
     * Updates the sensor display with the latest reading stored in DataManager.
     */
    private void updateLatestReading() {
        List<SensorReading> readings = DataManager.getInstance().getAllReadings();
        if (!readings.isEmpty()) {
            float lastValue = readings.get(readings.size() - 1).getValue();
            tvSensorValue.setText(String.valueOf(lastValue));
        }
    }

    /**
     * Configures the bottom navigation bar and activity transitions.
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
     * Configures the refresh button to restart the socket connection.
     */
    private void setupRefreshListener() {
        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
            socketManager.stopConnection();
            socketManager.startConnection("192.168.1.20", 2005);
        });
    }

    /**
     * Sets up the click listener for the UV Lamp card to toggle status and notify the server.
     */
    private void setupUvLampListener() {
        uvLampCard.setOnClickListener(v -> {
            uvLampStatus = !uvLampStatus;
            updateUvLampUI();
            // Inform the server about the new state
            socketManager.sendData(uvLampStatus ? "L1\n" : "L0\n");
            
            // Visual feedback for click interaction
            uvLampCard.setCardBackgroundColor(getColor(R.color.cardPress));
            new Handler().postDelayed(() ->
                    uvLampCard.setCardBackgroundColor(getColor(R.color.cardBackground)), 125
            );
        });
    }

    /**
     * Updates the UV lamp UI components based on the current status.
     */
    private void updateUvLampUI() {
        if (uvLampStatus) {
            lampSubTxt.setText("UV Lamp is on");
            ivLamp.setImageResource(R.drawable.ic_lamp_on);
        } else {
            lampSubTxt.setText("UV Lamp is off");
            ivLamp.setImageResource(R.drawable.ic_lamp_off);
        }
    }

    /**
     * Sets up the click listener for the Radio card to toggle status and notify the server.
     */
    private void setupRadioListener() {
        radioCard.setOnClickListener(v -> {
            radioStatus = !radioStatus;
            updateRadioUI();
            // Inform the server about the new state
            socketManager.sendData(radioStatus ? "R1\n" : "R0\n");

            radioCard.setCardBackgroundColor(getColor(R.color.cardPress));
            new Handler().postDelayed(() ->
                    radioCard.setCardBackgroundColor(getColor(R.color.cardBackground)), 125
            );
        });
    }

    /**
     * Updates the Radio UI components based on the current status.
     */
    private void updateRadioUI() {
        if (radioStatus) {
            radioSubTxt.setText("Radio is on");
            ivRadio.setImageResource(R.drawable.ic_radio_on);
        } else {
            radioSubTxt.setText("Radio is off");
            ivRadio.setImageResource(R.drawable.ic_radio_off);
        }
    }

    /**
     * Sets up the click listener for the Ambient Light card to toggle status and notify the server.
     */
    private void setupAmbientLightListener() {
        ambientCard.setOnClickListener(v -> {
            ambientStatus = !ambientStatus;
            updateAmbientLightUI();
            // Inform the server about the new state
            socketManager.sendData(ambientStatus ? "A1\n" : "A0\n");

            ambientCard.setCardBackgroundColor(getColor(R.color.cardPress));
            new Handler().postDelayed(() ->
                    ambientCard.setCardBackgroundColor(getColor(R.color.cardBackground)), 125
            );
        });
    }

    /**
     * Updates the Ambient Light UI components based on the current status.
     */
    private void updateAmbientLightUI() {
        if (ambientStatus) {
            ambientSubTxt.setText("Ambient Light is on");
            ivAmbient.setImageResource(R.drawable.ic_room_light_on);
        } else {
            ambientSubTxt.setText("Ambient Light is off");
            ivAmbient.setImageResource(R.drawable.ic_room_light_off);
        }
    }

    /**
     * Starts a new activity for the given class.
     */
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }
}
