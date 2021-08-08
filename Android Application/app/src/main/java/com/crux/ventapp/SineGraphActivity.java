package com.crux.ventapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.crux.ventapp.DeviceListActivity.btSocket;

public class SineGraphActivity extends AppCompatActivity {

    private static final String TAG = "SineGraphActivity";
    private BarChart chart;
    private List<BarEntry> data;

    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter = 0;
    volatile boolean stopWorker;
    InputStream mmInputStream;
    ArrayList<BarEntry> entries;
    Random rand = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sine_graph);

        entries = new ArrayList<>(100);

        chart = findViewById(R.id.chart1);
        data = FileUtils.loadBarEntriesFromAssets(getAssets(), "othersine.txt");

        // tvX = findViewById(R.id.tvValueCount);

        // seekBarX = findViewById(R.id.seekbarValues);

        chart = findViewById(R.id.chart1);

        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(500);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);

        // chart.setDrawXLabels(false);

        chart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = chart.getAxisLeft();
        // leftAxis.setTypeface(tfLight);
        leftAxis.setLabelCount(6, false);
        leftAxis.setAxisMinimum(-2.5f);
        leftAxis.setAxisMaximum(2.5f);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(0.1f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        // rightAxis.setTypeface(tfLight);
        rightAxis.setLabelCount(6, false);
        rightAxis.setAxisMinimum(-20f);
        rightAxis.setAxisMaximum(20f);
        rightAxis.setGranularity(0.1f);

        // seekBarX.setOnSeekBarChangeListener(this);
        // seekBarX.setProgress(150); // set data

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        setData(500);
        chart.animateXY(1500, 1500);
    }


    private void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        try {
            mmInputStream = btSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    int finalI = i;
                                    handler.post(() -> {
                                        Log.d(TAG, "bluetooth data " + data);
                                        // setData(counter++);
                                        // feedMultiple(Float.parseFloat(data));
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    private void setData(int count) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        Random random = new Random();

        int randX = random.nextInt(10);
        int randY = random.nextInt(10);
        for (int i = 0; i < count; i++) {
            entries.add(i,new BarEntry(randX,randY));
        }

        BarDataSet set;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set.setValues(entries);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            set = new BarDataSet(entries, "Sinus Function");
            set.setColor(Color.rgb(240, 120, 124));
        }

        BarData data = new BarData(set);
        data.setValueTextSize(10f);
        // data.setValueTypeface(tfLight);
        data.setDrawValues(false);
        data.setBarWidth(0.8f);

        chart.setData(data);
    }


}