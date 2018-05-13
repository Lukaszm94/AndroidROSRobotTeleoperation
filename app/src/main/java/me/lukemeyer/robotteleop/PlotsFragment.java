package me.lukemeyer.robotteleop;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlotsFragment extends Fragment {
    private final static String TAG = "PlotsFragment";
    private DataSeriesManager dataSeriesManager;
    private GraphView linearVelocityGraph = null;
    private GraphView angularVelocityGraph = null;

    public PlotsFragment() {
        Log.i(TAG, "constructor");
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plots, container, false);

        linearVelocityGraph = view.findViewById(R.id.linearVelocityPlot);
        angularVelocityGraph = view.findViewById(R.id.angularVelocityPlot);

        linearVelocityGraph.getGridLabelRenderer().setPadding(32);
        angularVelocityGraph.getGridLabelRenderer().setPadding(32);

        linearVelocityGraph.getViewport().setXAxisBoundsManual(true);
        linearVelocityGraph.getViewport().setMinX(0.0);
        linearVelocityGraph.getViewport().setMaxX(DefaultValues.PLOT_X_RANGE);
        angularVelocityGraph.getViewport().setXAxisBoundsManual(true);
        angularVelocityGraph.getViewport().setMinX(0.0);
        angularVelocityGraph.getViewport().setMaxX(DefaultValues.PLOT_X_RANGE);

        linearVelocityGraph.addSeries(dataSeriesManager.linearVelocitySPSeries); // FIXME code crashes here on screen orientation change (dataSeriesManager is null???)
        linearVelocityGraph.addSeries(dataSeriesManager.linearVelocityPVSeries);
        angularVelocityGraph.addSeries(dataSeriesManager.angularVelocitySPSeries);
        angularVelocityGraph.addSeries(dataSeriesManager.angularVelocityPVSeries);
        return view;
    }

    public void setDataSeriesManager(DataSeriesManager manager) {
        dataSeriesManager = manager;
    }

}
