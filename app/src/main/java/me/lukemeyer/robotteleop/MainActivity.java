package me.lukemeyer.robotteleop;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// ROS stuff
import org.ros.android.RosActivity;
import org.ros.node.NodeMainExecutor;
import org.ros.node.NodeConfiguration;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import static java.lang.Math.abs;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends /*AppCompat*/RosActivity implements SensorEventListener, SettingsFragment.ApplySettingsListener {

    // acc control settings
    private double maxRollAngle = 30.0 / 180.0 * Math.PI;
    private double maxPitchAngle = 45.0 / 180.0 * Math.PI;
    private double minActiveAngle = 5.0 / 180.0 * Math.PI; // output angle set to 0 if input angle is small

    private final static String TAG = "MainActivity";

    private Button plotsButton;
    private Button cameraButton;
    private Button settingsButton;
    private Button accButton;
    private JoystickView joystickView;
    private TextView linearVelocitySPTextView;
    private TextView linearVelocityPVTextView;
    private TextView angularVelocitySPTextView;
    private TextView angularVelocityPVTextView;

    final private PlotsFragment plotsFragment = new PlotsFragment();
    final private CameraFragment cameraFragment = new CameraFragment();
    final private SettingsFragment settingsFragment = new SettingsFragment();

    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();
    private boolean timerRunning = false;

    private VelocitySetpointPublisher velocitySetpointPublisher = null;
    private CameraImageSubscriber cameraImageSubscriber = null;
    private VelocitySubscriber velocityPVSubsriber = null;

    private double maxLinearVelocity;
    private double maxAngularVelocity;
    private int joystickAngle;
    private int joystickStrength;

    private SensorManager mSensorManager;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    DataSeriesManager dataSeriesManager = new DataSeriesManager();


    public MainActivity() {
        super("Robot Teleop", "Robot Teleop");
        maxLinearVelocity = DefaultValues.DEFAULT_MAX_LINEAR_VELOCITY;
        maxAngularVelocity = DefaultValues.DEFAULT_MAX_ANGULAR_VELOCITY;
        joystickAngle = joystickStrength = 0;
        plotsFragment.setDataSeriesManager(dataSeriesManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accButton = findViewById(R.id.accButton);
        plotsButton = findViewById(R.id.plotsButton);
        cameraButton = findViewById(R.id.cameraButton);
        settingsButton = findViewById(R.id.settingsButton);

        joystickView = findViewById(R.id.joystick);

        linearVelocitySPTextView = findViewById(R.id.linearVelocitySPTextView);
        linearVelocityPVTextView = findViewById(R.id.linearVelocityPVTextView);
        angularVelocitySPTextView = findViewById(R.id.angularVelocitySPTextView);
        angularVelocityPVTextView = findViewById(R.id.angularVelocityPVTextView);

        joystickView.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                joystickAngle = angle;
                joystickStrength = strength;
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frameLayout, plotsFragment).commit();

        plotsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // switch to plots fragment
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, plotsFragment).commit();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // switch to camera fragment
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, cameraFragment).commit();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // switch to settings fragment
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, settingsFragment).commit();
            }
        });

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        loadSettings();
        Log.i(TAG, "onCreate finished");
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        Log.i(TAG, "init started");
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());

        cameraImageSubscriber = new CameraImageSubscriber();
        nodeMainExecutor.execute(cameraImageSubscriber, nodeConfiguration);

        velocityPVSubsriber = new VelocitySubscriber();
        nodeMainExecutor.execute(velocityPVSubsriber, nodeConfiguration);

        velocitySetpointPublisher = new VelocitySetpointPublisher();
        nodeMainExecutor.execute(velocitySetpointPublisher, nodeConfiguration);
        startTimer();
        Log.i(TAG, "init finished");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        if(!timerRunning) {
            startTimer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mSensorManager.unregisterListener(this);
        if(timerRunning) {
            stopTimer();
        }
        //saveSettings();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        saveSettings();
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.i(TAG, "onSensorChanged");
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Log.i(TAG, "acc");
            System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            //Log.i(TAG, "mag");
            System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);
        }
    }

    @Override
    public void onApplySettingsButtonClicked() {
        Log.i(TAG, "onApplySettingsButtonClicked");
        updateSettings();
    }

    private void updateSystem() {
        double linearVelocityPercentage = 0.0;
        double angularVelocityPercentage = 0.0;
        if(accButton.isPressed()) { // use IMU data to determine velocities
            if((minActiveAngle >= maxRollAngle) || (minActiveAngle >= maxPitchAngle)) { // sanity check
                Log.e(TAG, "minActiveAngle too big, cannot use acc control");
                updateSetpointLabels(0.0, 0.0);
                return;
            }
            mSensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);
            mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
            linearVelocityPercentage = convertPitchAngleToLinearVelocityPercentage(mOrientationAngles[1]);
            angularVelocityPercentage = convertRollAngleToAngularVelocityPercentage(mOrientationAngles[2]);
        } else { // use joystick to determine velocities
            linearVelocityPercentage = convertJoystickReadingToLinearVelocityPercentage(joystickAngle, joystickStrength);
            angularVelocityPercentage = convertJoystickReadingToAngularVelocityPercentage(joystickAngle, joystickStrength);
        }

        //convert percentages to linear and angular velocities
        double linearVelocitySP = maxLinearVelocity * linearVelocityPercentage / 100.0;
        double angularVelocitySP = maxAngularVelocity * angularVelocityPercentage / 100.0;
        if(velocitySetpointPublisher != null) {
            velocitySetpointPublisher.publishSetpoint(linearVelocitySP, angularVelocitySP);
        }
        updateSetpointLabels(linearVelocitySP, angularVelocitySP);
        if(velocityPVSubsriber != null) {
            updateVelocityPVLabels(velocityPVSubsriber.getLinearVelocity(), velocityPVSubsriber.getAngularVelocity());
        }

        // display camera image
        if(cameraImageSubscriber != null && cameraImageSubscriber.isNewImageReady()) {
            if(cameraFragment.isVisible()) {
                cameraFragment.displayImage(cameraImageSubscriber.getNewImage());
            }
        }

        // update plots
        boolean silentUpdate = !plotsFragment.isVisible();
        double linearVelocityPV = 0.0;
        double angularVelocityPV = 0.0;
        if(velocityPVSubsriber != null) {
            linearVelocityPV = velocityPVSubsriber.getLinearVelocity();
            angularVelocityPV = velocityPVSubsriber.getAngularVelocity();
        }
        dataSeriesManager.appendData(linearVelocityPV, linearVelocitySP, angularVelocityPV, angularVelocitySP, silentUpdate);
    }

    private void updateSettings() {
        maxLinearVelocity = settingsFragment.getMaxLinearVelocity();
        maxAngularVelocity = settingsFragment.getMaxAngularVelocity();
    }

    private void saveSettings() {
        Log.i(TAG, "saveSettings");
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(getString(R.string.maxLinearVelocity), (float)maxLinearVelocity);
        editor.putFloat(getString(R.string.maxAngularVelocity), (float)maxAngularVelocity);
        editor.commit();
    }

    private void loadSettings() {
        Log.i(TAG, "loadSettings");
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        maxLinearVelocity = sharedPreferences.getFloat(getString(R.string.maxLinearVelocity), (float)DefaultValues.DEFAULT_MAX_LINEAR_VELOCITY);
        maxAngularVelocity = sharedPreferences.getFloat(getString(R.string.maxAngularVelocity), (float)DefaultValues.DEFAULT_MAX_ANGULAR_VELOCITY);
        settingsFragment.setMaxLinearVelocity(maxLinearVelocity);
        settingsFragment.setMaxAngularVelocity(maxAngularVelocity);
    }

    private void stopTimer(){
        if(mTimer1 != null){
            mTimer1.cancel();
            mTimer1.purge();
        }
        timerRunning = false;
    }

    private void startTimer(){
        if(timerRunning) {
            return;
        }
        mTimer1 = new Timer();
        mTt1 = new TimerTask() {
            public void run() {
                mTimerHandler.post(new Runnable() {
                    public void run(){
                        //Log.i(TAG, "timer callback");
                        updateSystem();
                    }
                });
            }
        };

        mTimer1.schedule(mTt1, 1, DefaultValues.SYSTEM_UPDATE_TICK_MS);
        timerRunning = true;
    }

    private double convertJoystickReadingToLinearVelocityPercentage(double angle, double strength) {
        double v = 0.0; // [%]
        if(angle <= 180) {
            double x = (angle - 90.0) / 90.0;
            v = (1.0 - abs(x)) * strength;
        }
        return v;
    }

    private double convertJoystickReadingToAngularVelocityPercentage(double angle, double strength) {
        double omega = 0.0; // [%]
        if(angle <= 180) {
            omega = -(angle - 90.0) / 90.0 * strength;
        } else {
            omega = (angle - 270.0) / 90.0 * strength;
        }
        return -omega;
    }

    private void updateSetpointLabels(double linearVelocitySetpoint, double angularVelocitySetpoint) {
        // convert to string, update textViews
        String linearSetpointString = String.format(Locale.UK, "SP: %.1f", linearVelocitySetpoint);
        linearVelocitySPTextView.setText(linearSetpointString);
        String angularSetpointString = String.format(Locale.UK, "SP: %.1f", angularVelocitySetpoint);
        angularVelocitySPTextView.setText(angularSetpointString);
    }

    private void updateVelocityPVLabels(double linearVelocity, double angularVelocity) {
        // prevent '-0.0' value being displayed
        if(abs(linearVelocity) < 0.05) {
            linearVelocity = 0.0;
        }
        if(abs(angularVelocity) < 0.05) {
            angularVelocity = 0.0;
        }
        // convert to string, update textViews
        String linearVelocityString = String.format(Locale.UK, "PV: %.1f", linearVelocity);
        linearVelocityPVTextView.setText(linearVelocityString);
        String angularVelocityString = String.format(Locale.UK, "PV: %.1f", angularVelocity);
        angularVelocityPVTextView.setText(angularVelocityString);
    }

    private double convertPitchAngleToLinearVelocityPercentage(double angle) {
        if(angle < minActiveAngle) {
            return 0.0;
        }
        angle = angle - minActiveAngle;
        angle  = clamp(angle, 0.0, maxPitchAngle - minActiveAngle);
        double vPercentage = angle / (maxPitchAngle - minActiveAngle) * 100.0;
        return vPercentage;
    }

    private double convertRollAngleToAngularVelocityPercentage(double angle) {
        if(abs(angle) < minActiveAngle) {
            return 0.0;
        }
        if(angle > 0.0) {
            angle = angle - minActiveAngle;
        } else {
            angle = angle + minActiveAngle;
        }
        angle = clamp(angle, -maxRollAngle + minActiveAngle, maxRollAngle - minActiveAngle);
        double omegaPercentage = -angle / (maxRollAngle - minActiveAngle) * 100.0;
        return omegaPercentage;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
