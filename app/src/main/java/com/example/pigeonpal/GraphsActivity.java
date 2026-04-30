package com.example.pigeonpal;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphsActivity extends AppCompatActivity {

    private LineChart lineChart;
    private TextView tvDataInfo;
    private SocketManager socketManager;

    private static final int COLOR_BLUE = Color.parseColor("#3498DB");

    private final SocketManager.SocketListener socketListener = new SocketManager.SocketListener() {
        @Override
        public void onSocketConnected() {
            Log.d("Graphs", "Socket connected");
        }

        @Override
        public void onSocketDisconnected() {
            Log.d("Graphs", "Socket disconnected");
        }

        @Override
        public void onMessageReceived(float message, Boolean lamp, Boolean light, Boolean radio) {
            // Refresh charts when new data arrives
            runOnUiThread(() -> loadCharts());
        }

        @Override
        public void onError(Exception e) {
            Log.e("Graphs", "Socket error: " + e.getMessage());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        socketManager = SocketManager.getInstance();
        initializeViews();
        setupBottomNavigation();
        loadCharts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        socketManager.addListener(socketListener);
        loadCharts();
    }

    @Override
    protected void onPause() {
        super.onPause();
        socketManager.removeListener(socketListener);
    }

    private void initializeViews() {
        lineChart = findViewById(R.id.lineChart);
        tvDataInfo = findViewById(R.id.tvDataInfo);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_graphs);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                navigateToActivity(MainActivity.class);
                return true;
            } else if (itemId == R.id.nav_graphs) {
                return true;
            } else if (itemId == R.id.nav_settings) {
                navigateToActivity(SettingsActivity.class);
                return true;
            }
            return false;
        });
    }

    private void loadCharts() {
        List<SensorReading> readings = DataManager.getInstance().getAllReadings();

        if (readings.isEmpty()) {
            tvDataInfo.setText("No data yet - add readings on Home screen");
        } else {
            tvDataInfo.setText("Showing " + readings.size() + " readings");
        }

        setupLineChart(readings);
    }

    private void setupLineChart(List<SensorReading> readings) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        for (int i = 0; i < readings.size(); i++) {
            SensorReading reading = readings.get(i);
            entries.add(new Entry(i, reading.getValue()));
            labels.add(sdf.format(new Date(reading.getTimestamp())));
        }

        if (entries.isEmpty()) {
            lineChart.clear();
            lineChart.setNoDataText("No data available");
            lineChart.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Sensor Values");
        dataSet.setColor(COLOR_BLUE);
        dataSet.setCircleColor(COLOR_BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(COLOR_BLUE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-45f);

        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.invalidate();
    }


    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(GraphsActivity.this, activityClass);
        startActivity(intent);
    }
}
