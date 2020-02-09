package com.example.charttest;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    Button button;
    LineChart chart;
    CheckBox hr, height, speed, temperature;
    List<ILineDataSet> currentDataSet = new ArrayList<>();
    LineDataSet hrDataSet;
    LineDataSet elevationDataSet;
    LineDataSet tempoDataSet;
    LineDataSet temperatureDataSet;
    TextView content;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chart = findViewById(R.id.line_chart);
        hr = findViewById(R.id.checkbox_hr);
        height = findViewById(R.id.checkbox_height);
        speed = findViewById(R.id.checkbox_speed);
        temperature = findViewById(R.id.checkbox_temperature);
        button = findViewById(R.id.button);

        hr.setOnCheckedChangeListener((buttonView, isChecked) -> {
            addOrRemoveDataSet(isChecked, hrDataSet);
        });

        height.setOnCheckedChangeListener((buttonView, isChecked) -> {
            addOrRemoveDataSet(isChecked, elevationDataSet);
        });

        speed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            addOrRemoveDataSet(isChecked, tempoDataSet);
        });

        temperature.setOnCheckedChangeListener((buttonView, isChecked) -> {
            addOrRemoveDataSet(isChecked, temperatureDataSet);
        });

        button.setOnClickListener(v -> {
            chart.getData().setHighlightEnabled(true);
            Toast.makeText(MainActivity.this, "ENABLED HIGHLIGHTH", Toast.LENGTH_SHORT).show();
        });


        hrDataSet = getHrDataSet();
        elevationDataSet = getElevationDataSet();
        tempoDataSet = getTempoDataSet();
        temperatureDataSet = getTemperatureDataSet();
        setupData(hrDataSet, Color.WHITE, Color.WHITE, Color.RED, Color.MAGENTA);
        setupElevationData(elevationDataSet, Color.GREEN);
        setupTempoData(tempoDataSet, Color.BLUE);
        setupTemperatureData(temperatureDataSet, Color.YELLOW);
        initChar(chart, Color.WHITE);

        CustomMarkerView customMarkerView = new CustomMarkerView(getApplicationContext(), R.layout.radar_marker_view);
//        content = customMarkerView.findViewById(R.id.tvContent);
        chart.setMarker(customMarkerView);
        chart.setOnChartValueSelectedListener(this);
    }

    private void addOrRemoveDataSet(boolean shouldAdd, ILineDataSet dataSet) {
        if (shouldAdd) {
            currentDataSet.add(dataSet);
        } else currentDataSet.remove(dataSet);
        chart.setData(new LineData(currentDataSet));
        chart.invalidate();
//        chart.animateX(2500);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private LineDataSet getHrDataSet() {
        int hrMax = Hr.arrayList.stream().mapToInt(v -> v).max().getAsInt();
        int hrMin = Hr.arrayList.stream().mapToInt(v -> v).min().getAsInt();
        AtomicInteger i = new AtomicInteger();
        List<Entry> hrEntry = Hr.arrayList.stream().map(v -> new Entry(i.getAndIncrement(), v)).collect(Collectors.toList());
        LineDataSet hrData = new LineDataSet(normalizeEntry(hrEntry, hrMin, hrMax, 0, 100), "Dataset 1");
        hrData.setValueFormatter(new Formatter());
        return hrData;
    }

    class Formatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return value + " hr";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private LineDataSet getElevationDataSet() {
        double max = Elevation.arrayList.stream().mapToDouble(v -> v).max().getAsDouble();
        double min = Elevation.arrayList.stream().mapToDouble(v -> v).min().getAsDouble();
        AtomicInteger i = new AtomicInteger();
        List<Entry> entry = Elevation.arrayList.stream().map(v -> new Entry(i.getAndIncrement(), v.floatValue())).collect(Collectors.toList());
        LineDataSet dataSet = new LineDataSet(normalizeEntry(entry, (int) Math.round(min), (int) Math.round(max), 0, 100), "Dataset2");
        return dataSet;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private LineDataSet getTempoDataSet() {
        int max = getSpeedValues().stream().mapToInt(v -> v).max().getAsInt();
        int min = getSpeedValues().stream().mapToInt(v -> v).min().getAsInt();
        AtomicInteger i = new AtomicInteger();
        List<Entry> entry = getSpeedValues().stream().map(v -> new Entry(i.getAndIncrement(), v.floatValue())).collect(Collectors.toList());
        LineDataSet dataSet = new LineDataSet(normalizeEntry(entry, min, max, 0, 100), "Dataset 3");
        return dataSet;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private LineDataSet getTemperatureDataSet() {
        int max = getTemperatureValues().stream().mapToInt(v -> v).max().getAsInt();
        int min = getTemperatureValues().stream().mapToInt(v -> v).min().getAsInt();
        AtomicInteger i = new AtomicInteger();
        List<Entry> entries = getTemperatureValues().stream().map(v -> new Entry(i.getAndIncrement(), v.floatValue())).collect(Collectors.toList());
        LineDataSet dataSet = new LineDataSet(normalizeEntry(entries, min, max, 0, 100), "Dataset4");
        return dataSet;
    }


    private float normalize(float value, int min, int max, int newMin, int newMax) {
        return ((value - min) / (max - min)) * (newMax - newMin) + newMin;
    }

    private List<Entry> normalizeEntry(List<Entry> entries, int min, int max, int newMin, int newMax) {
        for (int i = 0; i < entries.size(); i++) {
            float lastValue = entries.get(i).getY();
            float newValue = normalize(lastValue, min, max, newMin, newMax);
            entries.set(i, new Entry(i, newValue));
        }
        return entries;
    }

    private void initChar(LineChart chart, int color) {
//        chart.getDescription().setEnabled(false);
//        chart.setDrawGridBackground(false);
//        chart.setTouchEnabled(true);
//        chart.setDragEnabled(true);
//        chart.setScaleEnabled(true);
//        chart.setPinchZoom(true);
        chart.setBackgroundColor(color);
        chart.setViewPortOffsets(10, 0, 10, 0);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setSpaceTop(40);
        chart.getAxisLeft().setSpaceBottom(40);
        chart.getAxisRight().setEnabled(false);
        //Pionowa linia nad x
        chart.getXAxis().setEnabled(false);
        Legend l = chart.getLegend();
        l.setEnabled(true);
        l.setForm(Legend.LegendForm.CIRCLE);
    }


    private void setupTempoData(LineDataSet tempoDataSet, int lineColor) {
        tempoDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        tempoDataSet.setCubicIntensity(0.2f);
        tempoDataSet.setColor(lineColor);
        tempoDataSet.setDrawCircles(false);
        tempoDataSet.setDrawValues(false);
    }

    private void setupElevationData(LineDataSet elevationDataSet, int lineColor) {
        elevationDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        elevationDataSet.setCubicIntensity(0.1f);
        elevationDataSet.setDrawValues(false);
        elevationDataSet.setDrawCircles(false);
        elevationDataSet.setColor(lineColor);
        elevationDataSet.setFillColor(lineColor);
        elevationDataSet.setFillAlpha(10);
        elevationDataSet.setLineWidth(1.75f);
        elevationDataSet.setDrawFilled(true);
        elevationDataSet.setValueTextSize(9f);
    }

    private void setupTemperatureData(LineDataSet temperatureDataSet, int lineColor) {
        temperatureDataSet.setMode(LineDataSet.Mode.STEPPED);
        temperatureDataSet.setDrawValues(false);
        temperatureDataSet.setColor(lineColor);
        temperatureDataSet.setLineWidth(1.75f);
        temperatureDataSet.setDrawCircles(false);
        temperatureDataSet.setHighLightColor(Color.BLACK);
    }

    private void setupData(LineDataSet data, int lineColor, int holeColor, int circleColor, int highLightColor) {
        data.setCircleHoleColor(circleColor);
        data.setColor(lineColor);
        data.setLineWidth(1.75f);
        data.setCircleRadius(5f);
        data.setCircleHoleRadius(3f);
        data.setCircleColor(holeColor);
        data.setHighLightColor(highLightColor);
        data.setDrawValues(false);

    }

    private List<Integer> getSpeedValues() {
        ArrayList<Integer> values = new ArrayList<>();
        values.add(speedToSec(3, 20));
        values.add(speedToSec(3, 20));
        values.add(speedToSec(3, 20));
        values.add(speedToSec(3, 22));
        values.add(speedToSec(3, 24));
        values.add(speedToSec(3, 26));
        values.add(speedToSec(3, 23));
        values.add(speedToSec(3, 30));
        values.add(speedToSec(3, 33));
        values.add(speedToSec(3, 31));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 39));
        values.add(speedToSec(3, 42));
        values.add(speedToSec(3, 45));
        values.add(speedToSec(3, 48));
        values.add(speedToSec(3, 55));
        values.add(speedToSec(3, 53));
        values.add(speedToSec(3, 50));
        values.add(speedToSec(3, 53));
        values.add(speedToSec(3, 50));
        values.add(speedToSec(3, 53));
        values.add(speedToSec(3, 50));
        values.add(speedToSec(3, 47));
        values.add(speedToSec(3, 45));
        values.add(speedToSec(3, 40));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 33));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 30));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 40));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 33));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 40));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 33));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 31));
        values.add(speedToSec(3, 35));
        values.add(speedToSec(3, 37));
        values.add(speedToSec(3, 39));
        values.add(speedToSec(3, 42));
        return values;
    }

    private List<Integer> getTemperatureValues() {
        int i = 0;
        ArrayList<Integer> values = new ArrayList<>();
        values.add(20);
        values.add(20);
        values.add(21);
        values.add(21);
        values.add(22);
        values.add(22);
        values.add(22);
        values.add(23);
        values.add(23);
        values.add(24);
        values.add(25);
        values.add(26);
        values.add(27);
        values.add(26);
        values.add(25);
        values.add(24);
        values.add(23);
        values.add(22);
        values.add(22);
        values.add(22);
        values.add(22);
        values.add(22);
        values.add(22);
        values.add(22);
        values.add(22);
        values.add(22);
        values.add(22);
        values.add(23);
        values.add(24);
        values.add(25);
        values.add(25);
        values.add(25);
        values.add(25);
        values.add(25);
        values.add(25);
        values.add(25);
        values.add(25);
        values.add(25);
        return values;
    }

    private int speedToSec(int min, int sec) {
        return min * 60 + sec;
    }

    private List<Entry> getValues(int count, float range) {
        ArrayList<Entry> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range) + 3;
            values.add(new Entry(i, val));
        }
        return values;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("ELO", "Entry selected: " + e.toString());
    }

    @Override
    public void onNothingSelected() {

    }
}
