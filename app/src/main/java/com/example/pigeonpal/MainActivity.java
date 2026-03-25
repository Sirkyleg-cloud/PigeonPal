package com.example.pigeonpal;
// Package declaration - this file belongs to com.example.example1


import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    // Member variables - accessible throughout the class
    private TextView tvSensorValue;
    private TextView lampSubTxt;
    private ImageView ivLamp;
    private CardView radioCard;
    private TextView radioSubTxt;
    private ImageView ivRadio;
    private CardView uvLampCard;

    private boolean uvLampStatus = false;
    private boolean radioStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        setupBottomNavigation();
        setupUvLampListener();
        setupRadioListener();
    }


    @Override
    protected void onResume() {
        super.onResume();
  // NEW: Update count when returning to screen
    }
// onResume is called when activity becomes visible
// Useful for refreshing data after navigating away and back

    private void initializeViews() {
        tvSensorValue = findViewById(R.id.tvSensorValue);
        uvLampCard = findViewById(R.id.uvLampCard);
        lampSubTxt = findViewById(R.id.lampSubTxt);
        ivLamp = findViewById(R.id.ivLamp);
        radioCard = findViewById(R.id.radioCard);
        radioSubTxt = findViewById(R.id.radioSubTxt);
        ivRadio = findViewById(R.id.ivRadio);

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
     * Set up lamp card click listeners
     */
    private void setupUvLampListener() {
        uvLampCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateUvLampStatus();
            }
        });
    }

    private void updateUvLampStatus() {
        if (uvLampStatus == false) {
            lampSubTxt.setText("UV Lamp is on");
            ivLamp.setImageResource(R.drawable.ic_lamp_on);
            uvLampStatus = true;
            uvLampCard.setCardBackgroundColor(Color.parseColor("#A8E4A0"));
        } else {
            lampSubTxt.setText("UV Lamp is off");
            ivLamp.setImageResource(R.drawable.ic_lamp_off);
            uvLampStatus = false;
            uvLampCard.setCardBackgroundColor(Color.parseColor("#E4717A"));
        }
    }

    /**
     * Set up radio card click listeners
     */
    private void setupRadioListener() {
        radioCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateRadioStatus();
            }
        });
    }
    ValueAnimator valueAnimator = ValueAnimator.ofArgb(Color.RED, Color.GREEN);

    private void updateRadioStatus() {
        if (radioStatus == false) {
            radioSubTxt.setText("Radio is on");
            ivRadio.setImageResource(R.drawable.ic_radio_on);
            radioStatus = true;
            radioCard.setCardBackgroundColor(Color.parseColor("#A8E4A0"));
        } else {
            radioSubTxt.setText("Radio is off");
            ivRadio.setImageResource(R.drawable.ic_radio_off);
            radioStatus = false;
            radioCard.setCardBackgroundColor(Color.parseColor("#E4717A"));
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