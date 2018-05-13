package me.lukemeyer.robotteleop;

import android.graphics.Color;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class DataSeriesManager {
    public LineGraphSeries<DataPoint> linearVelocitySPSeries = new LineGraphSeries<>();
    public LineGraphSeries<DataPoint> angularVelocitySPSeries = new LineGraphSeries<>();
    public LineGraphSeries<DataPoint> linearVelocityPVSeries = new LineGraphSeries<>();
    public LineGraphSeries<DataPoint> angularVelocityPVSeries = new LineGraphSeries<>();

    private long timestampOffset = 0;

    DataSeriesManager() {
        timestampOffset = System.currentTimeMillis();

        linearVelocitySPSeries.setColor(Color.BLUE);
        linearVelocityPVSeries.setColor(Color.RED);

        angularVelocitySPSeries.setColor(Color.BLUE);
        angularVelocityPVSeries.setColor(Color.RED);
    }

    public void appendData(double linearPV, double linearSP, double angularPV, double angularSP, boolean silentUpdate) {
        double timestamp = (System.currentTimeMillis() - timestampOffset) / 1000.0;
        int samplesCount = (int)(DefaultValues.PLOT_X_RANGE / (DefaultValues.SYSTEM_UPDATE_TICK_MS / 1000.0));
        linearVelocityPVSeries.appendData(new DataPoint(timestamp, linearPV), true, samplesCount, silentUpdate);
        linearVelocitySPSeries.appendData(new DataPoint(timestamp, linearSP), true, samplesCount, silentUpdate);
        angularVelocityPVSeries.appendData(new DataPoint(timestamp, angularPV), true, samplesCount, silentUpdate);
        angularVelocitySPSeries.appendData(new DataPoint(timestamp, angularSP), true, samplesCount, silentUpdate);
    }


}
