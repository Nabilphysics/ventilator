package com.crux.ventapp.utils;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;



public class Helper {

    private static final String TAG="Helper";

    public static final long MAX_ENTRIES = 250;

    public void addEntry(float entry, LineChart chart, String label, String fillColor) {
        LineData data = chart.getData();
        if (data != null) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well
            if (set == null) {
                set = createSet(label, fillColor);
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(), entry), 0);

            data.notifyDataChanged();



            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            // chart.setVisibleXRangeMaximum(MAX_ENTRIES);
            // chart.getAxisLeft().setAxisMaximum(entry+2);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            if (set.getEntryCount() == MAX_ENTRIES) {
                set.removeFirst();
                // change Indexes - move to beginning by 1
                for (Entry en : set.getValues())
                    en.setX(en.getX() - 1);
            }
            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // YAxis.AxisDependency.LEFT);
        }
    }

    private static LineDataSet createSet(String label, String fillColor) {

        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.parseColor(fillColor));
        set.setCircleColor(Color.WHITE);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setDrawFilled(true);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(Color.parseColor(fillColor));
        // set.setHighlightEnabled(false);
        set.setHighLightColor(Color.BLUE);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        // set.setMode(LineDataSet.Mode.);

        return set;
    }


    public static void setupPressureGraph(BarChart chart) {

        chart.getDescription().setEnabled(false);
        chart.setMaxVisibleValueCount(1);
        chart.setPinchZoom(false);
        chart.setTouchEnabled(false);
        chart.setLongClickable(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(15);
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(Color.WHITE);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        chart.getLegend().setEnabled(false);

    }

    public static void setPressureData(float val, BarChart chart,String state) {

        float start = 1f;

        ArrayList<BarEntry> values = new ArrayList<>();

        values.add(new BarEntry(0, val));

        BarDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);

            if(state.equals("@B")){
                chart.getAxisLeft().setAxisMaximum((float) Math.floor(val+1));
                Log.d(TAG, "setPressureData: "+ (val + 1));
            }

            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.invalidate();

        } else {
            set1 = new BarDataSet(values, "Peak Pressure");
            set1.setDrawIcons(false);
            set1.setColor(Color.WHITE);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setValueTextColor(Color.WHITE);
            // data.setValueTypeface(tfLight);
            data.setBarWidth(0.9f);

            if(state.equals("@B")){
                chart.getAxisLeft().setAxisMaximum((float) Math.floor(val+1));
                Log.d(TAG, "setPressureData: "+ (val + 1));
            }

            chart.setData(data);
            chart.invalidate();
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
