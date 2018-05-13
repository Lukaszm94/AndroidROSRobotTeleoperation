package me.lukemeyer.robotteleop;

public class DefaultValues {
    public final static double MAX_LINEAR_VELOCITY_LB = 0.5; // [m/s]
    public final static double MAX_LINEAR_VELOCITY_UB = 3.0; // [m/s]
    public final static double MAX_ANGULAR_VELOCITY_LB = 5.0; // [rad/s]
    public final static double MAX_ANGULAR_VELOCITY_UB = 30.0; // [rad/s]

    public final static int SYSTEM_UPDATE_TICK_MS = 100; // [ms]
    public final static double DEFAULT_MAX_LINEAR_VELOCITY = MAX_LINEAR_VELOCITY_LB; // [m/s]
    public final static double DEFAULT_MAX_ANGULAR_VELOCITY = MAX_ANGULAR_VELOCITY_LB; // [rad/s]
    public final static double PLOT_X_RANGE = 20.0; // [s]
}
