package com.example.pigeonpal;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

    // Chart references
    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;
    private TextView tvDataInfo;

    // Colors for charts
    private static final int COLOR_RED = Color.parseColor("#E74C3C");
    private static final int COLOR_YELLOW = Color.parseColor("#F39C12");
    private static final int COLOR_GREEN = Color.parseColor("#27AE60");
    private static final int COLOR_BLUE = Color.parseColor("#3498DB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        initializeViews();
        setupBottomNavigation();
        loadCharts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCharts();  // Refresh charts when returning to screen
    }

    private void initializeViews() {
        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
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

    /**
     * Load all charts with data
     */
    private void loadCharts() {
        List<SensorReading> readings = DataManager.getInstance().getAllReadings();

        // Update info text
        if (readings.isEmpty()) {
            tvDataInfo.setText("No data yet - add readings on Home screen");
        } else {
            tvDataInfo.setText("Showing " + readings.size() + " readings");
        }

        // Setup each chart
        setupLineChart(readings);
        setupBarChart(readings);
        setupPieChart(readings);
    }

// ============================================================
// LINE CHART - Shows trend over time
// ============================================================
private void setupLineChart(List<SensorReading> readings) {
    // Create entries for the chart
    ArrayList<Entry> entries = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<>();

    // SimpleDateFormat formats timestamps into readable strings
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    // "HH:mm:ss" = hours:minutes:seconds (24-hour format)

    for (int i = 0; i < readings.size(); i++) {
        SensorReading reading = readings.get(i);

        entries.add(new Entry(i, reading.getValue()));

        // Create time label for this point
        labels.add(sdf.format(new Date(reading.getTimestamp())));
    }

    if (entries.isEmpty()) {
        lineChart.clear();
        lineChart.setNoDataText("No data available");
        lineChart.invalidate();  // Refresh the chart
        return;
    }

    // Create dataset from entries
    LineDataSet dataSet = new LineDataSet(entries, "Sensor Values");

    // Customize appearance
    dataSet.setColor(COLOR_BLUE);           // Line color
    dataSet.setCircleColor(COLOR_BLUE);     // Point color
    dataSet.setLineWidth(2f);               // Line thickness
    dataSet.setCircleRadius(4f);            // Point size
    dataSet.setDrawValues(true);            // Show values on points
    dataSet.setValueTextSize(10f);          // Value text size
    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);  // Smooth curve
    // CUBIC_BEZIER makes the line curved instead of straight

    // Create LineData object
    LineData lineData = new LineData(dataSet);

    // Configure X axis
    XAxis xAxis = lineChart.getXAxis();
    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // Labels at bottom
    xAxis.setGranularity(1f);                       // Minimum interval
    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
    // IndexAxisValueFormatter uses our labels array for X axis

    xAxis.setLabelRotationAngle(-45f);  // Rotate labels for readability

    // Configure chart
    lineChart.setData(lineData);
    lineChart.getDescription().setEnabled(false);  // Hide description
    lineChart.getLegend().setEnabled(true);        // Show legend
    lineChart.setTouchEnabled(true);               // Allow touch
    lineChart.setDragEnabled(true);                // Allow drag
    lineChart.setScaleEnabled(true);               // Allow zoom
    lineChart.invalidate();  // Refresh to show changes
}

    // ============================================================
    // BAR CHART - Shows recent values as bars
    // ============================================================
    private void setupBarChart(List<SensorReading> readings) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Show last 10 readings (or fewer if less data)
        int startIndex = Math.max(0, readings.size() - 10);
        // Math.max ensures we don't get negative index

        int barIndex = 0;
        for (int i = startIndex; i < readings.size(); i++) {
            SensorReading reading = readings.get(i);

            // BarEntry takes (x, y)
            entries.add(new BarEntry(barIndex, reading.getValue()));
            labels.add(sdf.format(new Date(reading.getTimestamp())));

            // Color based on value
            if (reading.getValue() < 30) {
                colors.add(COLOR_RED);
            } else if (reading.getValue() <= 65) {
                colors.add(COLOR_YELLOW);
            } else {
                colors.add(COLOR_GREEN);
            }

            barIndex++;
        }

        if (entries.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("No data available");
            barChart.invalidate();
            return;
        }

        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Recent Values");
        dataSet.setColors(colors);          // Apply our color array
        dataSet.setValueTextSize(10f);

        // Create BarData
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);          // Bar width (0-1)

        // Configure X axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelRotationAngle(-45f);

        // Configure chart
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);     // Hide legend for cleaner look
        barChart.setFitBars(true);                  // Make bars fit nicely

        barChart.invalidate();
    }

    // ============================================================
    // PIE CHART - Shows distribution of Low/Medium/High
    // ============================================================
    private void setupPieChart(List<SensorReading> readings) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // Count readings in each category
        int lowCount = DataManager.getInstance().countByStatus("Low");
        int mediumCount = DataManager.getInstance().countByStatus("Medium");
        int highCount = DataManager.getInstance().countByStatus("High");

        // Only add entries if count > 0
        if (lowCount > 0) {
            entries.add(new PieEntry(lowCount, "Low"));
            colors.add(COLOR_RED);
        }
        if (mediumCount > 0) {
            entries.add(new PieEntry(mediumCount, "Medium"));
            colors.add(COLOR_YELLOW);
        }
        if (highCount > 0) {
            entries.add(new PieEntry(highCount, "High"));
            colors.add(COLOR_GREEN);
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No data available");
            pieChart.invalidate();
            return;
        }

        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "Distribution");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);          // Space between slices

        // Create PieData
        PieData pieData = new PieData(dataSet);

        // Configure chart
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(40f);        // Size of center hole
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setCenterText("Status\nDistribution");  // Text in center
        pieChart.setCenterTextSize(12f);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.getLegend().setEnabled(true);
        pieChart.invalidate();
    }
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(GraphsActivity.this, activityClass);
        startActivity(intent);
        finish();
    }
}