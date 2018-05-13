package me.lukemeyer.robotteleop;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    public interface ApplySettingsListener {
        void onApplySettingsButtonClicked();
    }
    private final static String TAG = "SettingsFragment";

    MainActivity mainActivity = null;

    SeekBar maxLinearVelocitySeekBar;
    SeekBar maxAngularVelocitySeekBar;
    TextView maxLinearVelocityTextView;
    TextView maxAngularVelocityTextView;
    Button applyButton;

    double defaultMaxLinearVelocity = 0.0;
    double defaultMaxAngularVelocity = 0.0;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) { // API >= 23
        super.onAttach(context);
        if(context instanceof MainActivity) {
            Log.i(TAG, "onAttach(activity), casting Context to MainActivity");
            mainActivity = (MainActivity)context;
        } else {
            Log.e(TAG, "onAttach(context), cannot acquire mainActivity reference");
        }
    }

    @Override
    public void onAttach(Activity activity) { // API < 23
        super.onAttach(activity);
        if(activity instanceof MainActivity) {
            Log.i(TAG, "onAttach(activity), casting Activity to MainActivity");
            mainActivity = (MainActivity)activity;
        } else {
            Log.e(TAG, "onAttach(activity), cannot acquire mainActivity reference");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);
        maxLinearVelocitySeekBar = view.findViewById(R.id.maxLinearVelocitySeekBar);
        maxAngularVelocitySeekBar = view.findViewById(R.id.maxAngularVelocitySeekBar);

        maxLinearVelocityTextView = view.findViewById(R.id.maxLinearVelocityTextView);
        maxAngularVelocityTextView = view.findViewById(R.id.maxAngularVelocityTextView);

        applyButton = view.findViewById(R.id.applyButton);

        maxLinearVelocitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Log.i(TAG, "maxLinearVelocity onProgressChanged, progress= " + progress);
                // update text view
                updateValueTextView(getMaxLinearVelocity(), maxLinearVelocityTextView);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        maxAngularVelocitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Log.i(TAG, "maxAngularVelocity onProgressChanged, progress= " + progress);
                // update text view
                updateValueTextView(getMaxAngularVelocity(), maxAngularVelocityTextView);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "applyButton clicked");
                if(mainActivity == null) {
                    Log.e(TAG, "applyButton onClick: mainActivity ref is null");
                    return;
                }
                try {
                    ((ApplySettingsListener) mainActivity).onApplySettingsButtonClicked();
                } catch (ClassCastException cce) {

                }
            }
        });

        setMaxLinearVelocity(defaultMaxLinearVelocity);
        setMaxAngularVelocity(defaultMaxAngularVelocity);
        return view;
    }

    public void setMaxLinearVelocity(double value) {
        defaultMaxLinearVelocity = value;
        if(maxLinearVelocitySeekBar != null) {
            int progress = (int)(mapRangedValueToRange(value, DefaultValues.MAX_LINEAR_VELOCITY_LB, DefaultValues.MAX_LINEAR_VELOCITY_UB, 0, 100) + 0.5);
            maxLinearVelocitySeekBar.setProgress(progress);
        } else {
            Log.w(TAG, "maxLinearVelocitySeekBar is null, cannot set progress");
        }
    }

    public void setMaxAngularVelocity(double value) {
        defaultMaxAngularVelocity = value;
        if(maxAngularVelocitySeekBar != null) {
            int progress = (int)(mapRangedValueToRange(value, DefaultValues.MAX_ANGULAR_VELOCITY_LB, DefaultValues.MAX_ANGULAR_VELOCITY_UB, 0, 100) + 0.5);
            maxAngularVelocitySeekBar.setProgress(progress);
        } else {
            Log.w(TAG, "maxLinearAngularSeekBar is null, cannot set progress");
        }
    }

    public double getMaxLinearVelocity() {
        if(maxLinearVelocitySeekBar == null) {
            Log.w(TAG, "getMaxLinearVelocity() called when maxLinearVelocitySeekBar is null");
            return 0.0;
        }
        int progress = maxLinearVelocitySeekBar.getProgress();
        return mapRangedValueToRange(progress, 0, 100, DefaultValues.MAX_LINEAR_VELOCITY_LB, DefaultValues.MAX_LINEAR_VELOCITY_UB);
    }

    public double getMaxAngularVelocity() {
        if(maxAngularVelocitySeekBar == null) {
            Log.w(TAG, "getMaxAngularVelocity() called when maxAngularVelocitySeekBar is null");
            return 0.0;
        }
        int progress = maxAngularVelocitySeekBar.getProgress();
        return mapRangedValueToRange(progress, 0, 100, DefaultValues.MAX_ANGULAR_VELOCITY_LB, DefaultValues.MAX_ANGULAR_VELOCITY_UB);
    }

    private double mapRangedValueToRange(double input, double inputMin, double inputMax, double outputMin, double outputMax) {
        double inputRange = inputMax - inputMin;
        if(inputRange <= 0.0) {
            Log.e(TAG, "mapRangedValueToRange(), inputRange <= 0");
            return 0.0;
        }
        double ratio = (input - inputMin) / inputRange;
        if(ratio < 0.0) {
            ratio = 0.0;
        }
        if(ratio > 1.0) {
            ratio = 1.0;
        }
        double output = outputMin + ratio * (outputMax - outputMin);
        return output;
    }

    private void updateValueTextView(double value, TextView textView) {
        String str = String.format(Locale.UK, "%.1f", value);
        textView.setText(str);
    }

}
