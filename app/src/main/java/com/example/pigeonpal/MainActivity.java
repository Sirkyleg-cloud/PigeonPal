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
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {

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
    private boolean uvLampStatus = false;
    private boolean radioStatus = false;
    private boolean ambientStatus = false;
    private TextView pigeonName;
    private Boolean darkPref;
    private SocketManager socketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        setupBottomNavigation();
        setupUvLampListener();
        setupRadioListener();
        setupAmbientLightListener();
        setupRefreshListener();
        socketManager = SocketManager.getInstance();
        setupSocketListener();
        socketManager.startConnection("192.168.1.20", 2005);

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
        // Refresh the pigeon name whenever we return to this screen
        displayPigeonName();
        setupSocketListener();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketManager != null) {
            socketManager.stopConnection();
        }
    }

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

    private void displayPigeonName() {
        SharedPreferences prefs = getSharedPreferences("PigeonPrefs", MODE_PRIVATE);
        String savedName = prefs.getString("pigeon_name", "Unset");
        pigeonName.setText(savedName);
    }

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

    private void setupRefreshListener() {
        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
            setupSocketListener();
            socketManager.startConnection("192.168.1.20", 2005);
        });
    }

    private void setupUvLampListener() {
        uvLampCard.setOnClickListener(v -> {
            updateUvLampStatus();
            uvLampCard.setCardBackgroundColor(getColor(R.color.cardPress));
            new Handler().postDelayed(() ->
                    uvLampCard.setCardBackgroundColor(getColor(R.color.cardBackground)), 125
            );
        });
    }

    private void updateUvLampStatus() {
        if (!uvLampStatus) {
            lampSubTxt.setText("UV Lamp is on");
            ivLamp.setImageResource(R.drawable.ic_lamp_on);
            uvLampStatus = true;
        } else {
            lampSubTxt.setText("UV Lamp is off");
            ivLamp.setImageResource(R.drawable.ic_lamp_off);
            uvLampStatus = false;
        }
    }

    private void setupRadioListener() {
        radioCard.setOnClickListener(v -> {
            updateRadioStatus();
            radioCard.setCardBackgroundColor(getColor(R.color.cardPress));
            new Handler().postDelayed(() ->
                    radioCard.setCardBackgroundColor(getColor(R.color.cardBackground)), 125
            );
        });
    }

    private void updateRadioStatus() {
        if (!radioStatus) {
            radioSubTxt.setText("Radio is on");
            ivRadio.setImageResource(R.drawable.ic_radio_on);
            radioStatus = true;
        } else {
            radioSubTxt.setText("Radio is off");
            ivRadio.setImageResource(R.drawable.ic_radio_off);
            radioStatus = false;
        }
    }

    private void setupAmbientLightListener() {
        ambientCard.setOnClickListener(v -> {
            updateAmbientLightStatus();
            ambientCard.setCardBackgroundColor(getColor(R.color.cardPress));
            new Handler().postDelayed(() ->
                    ambientCard.setCardBackgroundColor(getColor(R.color.cardBackground)), 125
            );
        });
    }

    private void updateAmbientLightStatus() {
        if (!ambientStatus) {
            ambientSubTxt.setText("Ambient Light is on");
            ivAmbient.setImageResource(R.drawable.ic_room_light_on);
            ambientStatus = true;
        } else {
            ambientSubTxt.setText("Ambient Light is off");
            ivAmbient.setImageResource(R.drawable.ic_room_light_off);
            ambientStatus = false;
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
        finish();
    }

    private void setupSocketListener() {
        socketManager.setListener(new SocketManager.SocketListener() {
            @Override
            public void onSocketConnected() {
                Log.d("Socket", "Connected to server");
            }

            @Override
            public void onSocketDisconnected() {
                Log.d("Socket", "Disconnected from server");
            }

            @Override
            public void onMessageReceived(float message) {
                runOnUiThread(() -> {
                    tvSensorValue.setText(String.valueOf(message));
                    DataManager.getInstance().addReading(message);
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("Socket", "Error: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
