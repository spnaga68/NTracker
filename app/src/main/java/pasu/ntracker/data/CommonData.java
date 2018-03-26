package pasu.ntracker.data;

import android.content.Context;

/**
 * Created by Admin on 18-03-2018.
 */

public class CommonData {
    public static final String DRIVER_LOCATION = "driverlocation";
    public static final String CURRENT_TRACK_INFO = "current_info";
    public static final String TRAVEL_SPEED = "travel_speed";
    public static final String TRAVELED_DIST = "travel_dist";
    public static final String TRAVELED_TIME = "travel_time";
    public static final String TRAVELED_IDLE_TIME = "travel_idle";
    public static final String BEARING = "bearing";
    public static String TRACK_ID = "trackid";
    public static boolean speed_waiting_stop;
    public static boolean iswaitingrunning;

    public static boolean serviceIsRunningInForeground(Context context) {
        return false;
    }
}
