package pasu.ntracker.Service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pasu.ntracker.DriverMapActivity;
import pasu.ntracker.GetTravelInfo;
import pasu.ntracker.R;
import pasu.ntracker.data.CommonData;
import pasu.ntracker.data.Tracker;
import pasu.ntracker.data.Waypoints;
import pasu.ntracker.utils.CommonUtils;
import pasu.ntracker.utils.LocationUtils;
import pasu.ntracker.utils.NetworkStatus;
import pasu.ntracker.utils.SessionSave;
import pasu.ntracker.utils.Systems;


/**
 * getting gps status without location manager
 * This class helps to get the driver current location using location client. It
 * Keep on updating driver location to server with certain time interval (Every
 * 5sec). In this class,Driver gets the new request notification and trip cancel
 * notifications.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LocationUpdate extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final double Rad = 6372.8;
    public static final String BROADCAST_ACTION = "NTracker";
    //public static String distanceKM = "";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public static final String CUSTOM_ACTION = "YOUR_CUSTOM_ACTION";
    private static int SOCKET_ATTEMPTING = 0;

    private static boolean ROUTE_EXPIRED_TODAY = false;
    public static double currentLatitude = 0.0;
    public static double currentLongtitude = 0.0;
    public static String oLocation = "";
    public static double speed;
    public static double HTotdistanceKM = 0.0;
    public static LocationUpdate instance;
    private static int Notification_ID = 1;
    private static NotificationManager notificationManager;
    public final int UPDATE_INTERVAL_IN_TRIP = 3000;
    public ArrayList<String> wayPoint = new ArrayList<String>();
    public Location previousBestLocation = null;
    int locationUpdate = 1000 * 5;
    String historyValues = "";
    int historyCount = 0;
    Notification notification;
    int tempKMVariable = 0;
    File file;
    Intent intent;
    private ScheduledExecutorService mTimer = Executors.newSingleThreadScheduledExecutor();
    private double lastlatitude = 0.0;
    private double lastlongitude = 0.0;
    private String sLocation = "";
    private String updateLocation = "";
    private Handler mhandler;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private String Reg_ID;
    private double haverdistance = 0.0;
    private int connectionCheck = 0;
    private int avoidLatLng = 0;
    private double tempLat = 0.0;
    private double tempLng = 0.0;
    private InputStream in;
    private boolean dont_encode;
    private String bearing = "0";
    private GetTravelInfo getTravelInfo;
    //    @Override
//    public void onStart(final Intent intent, final int startId) {
//        mGoogleApiClient.connect();
//        super.onStart(intent, startId);
//    }
    public static Location currentLocation = null;
    //    private long locationUpdatedAt = Long.MIN_VALUE;
    private double lastlatitude1, lastlongitude1;
    private Location mLastLocation;
    public static boolean isSocket = false;
    private double slabDistance = 250;
    private boolean canCalculateDistance = true;
    private Handler DistanceHandler;
    private Runnable distanceRunnable;
    private long locationUpdatedAt = 0L;
    private int startID;
    private Tracker track_data;


    // Send an Intent with an action named "custom-event-name". The Intent sent should
// be received by the ReceiverActivity.
    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent(LocationUpdate.BROADCAST_ACTION);
        // You can also include some extra data.
        intent.putExtra(CommonData.TRAVELED_DIST, SessionSave.getDistance(LocationUpdate.this));
        intent.putExtra(CommonData.TRAVELED_IDLE_TIME, SessionSave.getWaitingTime(LocationUpdate.this));
        intent.putExtra(CommonData.TRAVEL_SPEED, speed);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public static void getTravelInfo(GetTravelInfo getTravelInfo) {
        getTravelInfo = getTravelInfo;
    }

    public static void startLocationService(Context context) {
        if (!CommonData.serviceIsRunningInForeground(context)) {
            Intent pushIntent1 = new Intent(context, LocationUpdate.class);
            context.startService(pushIntent1);
        }
    }

    public static void stop(Context context) {
        if (CommonData.serviceIsRunningInForeground(context)) {
            Intent pushIntent1 = new Intent(context, LocationUpdate.class);
            context.stopService(pushIntent1);
        }
    }


//    public void onCreate() {
//        super.onCreate();
//
//
//
//
//    }


//    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(MyIntentService.BROADCAST_ACTION_BAZ)) {
//                final String param = intent.getStringExtra(EXTRA_PARAM_B);
//                // do something
//            }
//        }
//    };

    public static boolean isNetworkEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("", "Location update service started Start");
        startID = startId;
        System.out.println("startID" + startId);
        mGoogleApiClient.connect();

        startTime();
//
//        bintent.putExtra("DATE", new Date().toString());
//        Log.d(MyIntentService.class.getSimpleName(), "sending broadcast");

        // send local broadcast


        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("Wakelock")
    @Override
    public void onCreate() {
        super.onCreate();
        CommonData.speed_waiting_stop = false;
//        lastlatitude1 = SessionSave.getLastLng(LocationUpdate.this).latitude;
//        lastlongitude1 = SessionSave.getLastLng(LocationUpdate.this).longitude;
        lastlatitude = SessionSave.getLastLng(LocationUpdate.this).latitude;
        lastlongitude = SessionSave.getLastLng(LocationUpdate.this).longitude;
        DistanceHandler = new Handler();
        distanceRunnable = new Runnable() {
            @Override
            public void run() {
                canCalculateDistance = true;
            }
        };
        DistanceHandler.post(distanceRunnable);
        intent = new Intent(BROADCAST_ACTION);
        instance = this;
        System.out.println("Location  ConnectionResult 1");
        Log.e("", "Location update service create");
        mhandler = new Handler();
//        mTimer.scheduleAtFixedRate(new LocationUpdateTask(), 0, UPDATE_INTERVAL_IN_TRIP);

        mTimer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("Gcmupdate ----->   " + new Date());
                sendMessage();
                System.out.println("NetworkStatus " + NetworkStatus.isOnline(LocationUpdate.this) + " sLocation " + sLocation + " updateLocation " + updateLocation
                        + "__" + SessionSave.getSession("Id", LocationUpdate.this) + "__" + SessionSave.getSession("shift_status", LocationUpdate.this));
                if (SessionSave.getSession(CommonData.TRACK_ID, LocationUpdate.this).trim().equals("")) {
                    if (mTimer != null)
                        mTimer.shutdown();
                    stopSelf();

                } else if (!GPSEnabled(LocationUpdate.this)) {
                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {
//
                            connectionCheck = 1;
                            String message = "";
                            if (!isNetworkEnabled(LocationUpdate.this))
                                message = LocationUpdate.this.getString(R.string.location_enable);
                            else
                                message = LocationUpdate.this.getString(R.string.change_network);
                            Toast.makeText(LocationUpdate.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {

                    if (NetworkStatus.isOnline(LocationUpdate.this)) {
//                            && updateLocation.equals("")) {
                        DriverStatusUpdate(SessionSave.getSession("Id", LocationUpdate.this), SessionSave.getSession("status", LocationUpdate.this), "");
                    }
                }

            }
        }, 0, 15000L, TimeUnit.MILLISECONDS);
        //  mTimer.
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }


    public void cancelNotify() {
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    private StringBuilder inputStreamToString(final InputStream is) {
        String rLine = "";
        final StringBuilder answer = new StringBuilder();
        final BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try {
            while ((rLine = rd.readLine()) != null)
                answer.append(rLine);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return answer;
    }


    @Override
    public void onDestroy() {
        try {
            Log.e("timereererere", "Location update service Stopped");
            if (SessionSave.getSession("Id", LocationUpdate.this).equals("")) {

                Log.e("", "Location update service Stopped");
                stopLocationUpdates();
            } else {
                stopLocationUpdates();
            }
            if (mTimer != null) {
                mTimer.shutdown();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//

        stopForeground(true);
        CommonData.speed_waiting_stop = true;
        Systems.out.println("timer destory " + timeSwap + " finalTime " + finalTime + " timeInMillies" + timeInMillies);
        stoptime();
        Systems.out.println("wait destroy");
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(final ConnectionResult arg0) {
        //   System.out.println("Location  ConnectionResult " + arg0);
    }

    /**
     * Calculates the Internal distance that travel by fleet during active status
     */
    public void DistanceCalculation(Location currentLocation, LatLng to) {
        Double lastlatitude = to.latitude;
        Double lastlongitude = to.longitude;
        if (currentLocation != null && currentLocation.getSpeed() > 2 && currentLocation.hasAccuracy() && currentLocation.getAccuracy() <= 100) {
            boolean updateLocationandReport = false;

            System.out.println("haversine 2*" + updateLocationandReport + "__" + lastlatitude1 + "__" + lastlongitude1);


            if (currentLocation.getLatitude() != 0.0 && currentLocation.getLongitude() != 0.0) {
                LocationUpdate.oLocation += currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "," + mLastLocation.getAccuracy() + "," + "," + speed + "," + SessionSave.getDistance(LocationUpdate.this) + "|";
                haversine(lastlatitude,
                        lastlongitude, currentLocation.getLatitude(), currentLocation.getLongitude());

            }

        }
    }


    public void UtilizeLocation() {
        Location location = mLastLocation;
        if (currentLatitude != 0.0 && currentLongtitude != 0.0) {

            if (speed > 3 && location.hasAccuracy() && location.getAccuracy() <= 50) {
                sLocation += currentLatitude + "," + currentLongtitude + "," + location.getAccuracy() + "," + speed + "," + SessionSave.getDistance(LocationUpdate.this) + "|";


                float[] ff = new float[1];

                track_data = CommonUtils.fromJson(SessionSave.getSession(CommonData.CURRENT_TRACK_INFO, this), Tracker.class);
                System.out.println("polylat"+track_data.getPickuplat() + "P" + track_data.getDroplat()+"__"+SessionSave.getSession(track_data.getPickuplat() + "P" + track_data.getDroplat(), getApplicationContext()));
                if (!SessionSave.getSession(track_data.getPickuplat() + "P" + track_data.getDroplat(), getApplicationContext()).equals("")) {
                    List<LatLng> latLngs = Arrays.asList(CommonUtils.fromJson(SessionSave.getSession(track_data.getPickuplat() + "P" + track_data.getDroplat(), getApplicationContext()), LatLng[].class));

                    if (!PolyUtil.isLocationOnEdge(new LatLng(currentLatitude, currentLongtitude), latLngs, true, 1000)) {
//                    CommonUtils.alertDialog(getString(R.string.reached), DriverMapActivity.this);
                        Toast.makeText(getApplicationContext(), "Route Deviated", Toast.LENGTH_LONG).show();
                    }
                }
                if (location.hasBearing())
                    bearing = String.valueOf(location.getBearing());
                if (canCalculateDistance) {
                    if (locationUpdatedAt == 0) {
                        locationUpdatedAt = System.currentTimeMillis();

                    } else {
                        System.out.println("secondElapsed" + startID + "*" + System.currentTimeMillis() + "__" + locationUpdatedAt + "__" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - locationUpdatedAt));
                        long secondsElapsed = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - locationUpdatedAt);
                        if (secondsElapsed <= 0) {
                            locationUpdatedAt = System.currentTimeMillis();
                        }
                        if (secondsElapsed >= 5) {
                            // check location accuracy here
                            locationUpdatedAt = System.currentTimeMillis();
                            if (lastlatitude != 0.0 && lastlongitude != 0.0) {
                                boolean isLocationRepeating = false;
                                System.out.println("calculateDistancecalled");
                                JSONArray jsonArray = SessionSave.ReadWaypoints(LocationUpdate.this);
//                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    try {
//                                        String time = ((JSONObject) jsonArray.get(i)).getLong("time");
//                                        if (time.equals("" + DateFormat.getTimeInstance().format(new Date()))) {
//                                            isLocationRepeating = true;
//                                        }
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
                                if (!isLocationRepeating) {
                                    DistanceCalculation(location, new LatLng(lastlatitude, lastlongitude));
                                    SessionSave.saveLastLng(new LatLng(location.getLatitude(), location.getLongitude()), LocationUpdate.this);
                                    SessionSave.saveSession(CommonData.BEARING, location.getBearing(), LocationUpdate.this);
                                    lastlatitude = location.getLatitude();
                                    lastlongitude = location.getLongitude();
                                    DistanceHandler.postDelayed(distanceRunnable, 3000);
                                }
                            } else {
                                lastlatitude = currentLatitude;
                                lastlongitude = currentLongtitude;
                            }
                        }

                    }


                    System.out.println("GCM update --- > ll  " + SessionSave.getDistance(LocationUpdate.this) + lastlatitude);

                } else if (!SessionSave.getSession("travel_status", LocationUpdate.this).equalsIgnoreCase("2")) {
                    lastlatitude = currentLatitude;
                    lastlongitude = currentLongtitude;
                }
            }


        }

    }

    @Override
    public void onConnected(final Bundle connectionHint) {
        try {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                avoidLatLng = 0;
                tempKMVariable = 0;
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (servicesConnected() && mLastLocation != null && mLastLocation.hasAccuracy() && mLastLocation.getAccuracy() <= 200) {

                    currentLatitude = mLastLocation.getLatitude();
                    currentLongtitude = mLastLocation.getLongitude();
                    sLocation = currentLatitude + "," + currentLongtitude + "|";
                    if (mLastLocation.hasBearing())
                        bearing = String.valueOf(mLastLocation.getBearing());
                } else {
                    mhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LocationUpdate.this, "Gps accuracy is not good", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                mGoogleApiClient.connect();
            }

        } catch (final SecurityException e) {
            Log.e("", "mlocation exception" + e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(final Location location) {

        System.out.println("onlocaitonchangeddd");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (location != null && location.getLatitude() != 0.0) {
                String mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                double _speed = location.getSpeed();
                speed = roundDecimal(convertSpeed(_speed), 2);
                currentLatitude = location.getLatitude();
                currentLongtitude = location.getLongitude();

                mLastLocation = location;
                System.out.println("onNewLocationAvailable " + startID + "__" + mLastUpdateTime + "_" + SessionSave.getSession("status", LocationUpdate.this) + "++" + currentLatitude + "__" + speed + "__" + location.getAccuracy());
                try {
                    UtilizeLocation();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else
            mhandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!SessionSave.getSession("Id", LocationUpdate.this).equals(""))
                        Toast.makeText(LocationUpdate.this, "Gps accuracy is not good", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    private double convertSpeed(double speed) {
        return ((speed * 3600) * 0.001);
    }

    private double roundDecimal(double value, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        value = bd.doubleValue();
        return value;
    }


    /**
     * This Function is used for calculate the distance travelled
     */
    public synchronized void haversine(final double lat1, final double lon1, final double lat2, final double lon2) {
        // TODO Auto-generated method stub
        //Getting both the coordinates
        LatLng from = new LatLng(lat1, lon1);
        final LatLng to = new LatLng(lat2, lon2);

        System.out.println("haversine 1");

        //Calculating the distance in meters

        double distance = (float) SphericalUtil.computeDistanceBetween(from, to) / 1000;
        if (SessionSave.getSession("Metric", LocationUpdate.this).trim().equalsIgnoreCase("miles"))
            distance = distance / 1.60934;
        System.out.println("Haversine Distance*" + (distance) + "__" + from.latitude + "__" + from.longitude + "__" + to.latitude + "__" + to.longitude);
//        if ((distance * 1000) > slabDistance) {
//            new FindApproxDistance(from, to);
////            DistanceHandler.postDelayed(distanceRunnable, 2000);
//            lastlatitude1 = 0.0;
//            lastlongitude1 = 0.0;
//        } else {
        distance += SessionSave.getDistance(LocationUpdate.this);

        SessionSave.setDistance(distance, LocationUpdate.this);
        System.out.println("Haversine Distance**" + (distance));

        SessionSave.saveSession("lastknowlats", "" + to.latitude, LocationUpdate.this);
        SessionSave.saveSession("lastknowlngs", "" + to.longitude, LocationUpdate.this);


        SessionSave.saveWaypoints(from, to, "haversine", distance, "" + "___" + startID, LocationUpdate.this);

        SessionSave.saveGoogleWaypoints(from, to, "haversine", distance, "" + "___" + startID, LocationUpdate.this);
//        }
    }


    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */

    @SuppressWarnings("deprecation")
    @SuppressLint("Wakelock")
    public String DriverStatusUpdate(final String id, final String status, final String gcmid) {
        String r_message = "";
        try {

            if (NetworkStatus.isOnline(LocationUpdate.this)) {
                updateLocation = sLocation;
                SessionSave.saveSession(CommonData.DRIVER_LOCATION, updateLocation, LocationUpdate.this);

                Waypoints waypoints = new Waypoints();
//                JSONObject data = new JSONObject();
//                data.put("data", j);
//                data.put("unique", unique());
//                data.put("platform", "ANDROID");
//                data.put("app", "DRIVER");
//                data.put("id", SessionSave.getSession("Id", LocationUpdate.this));
                SendDataToserver(waypoints);

            }

            SessionSave.saveSession("status", SessionSave.getSession("status", LocationUpdate.this), LocationUpdate.this);
        } catch (final Exception e) {
            e.printStackTrace();

        }

        return r_message;
    }

    private void driverResponseHandling(JSONObject json) {

        //   json = new JSONObject(data);

    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            return true;
            // Google Play services was not available for some reason
        } else
            // Display an error dialog
            return false;
    }

    public void generateNotifications(Context context, String message, Class<?> class1, boolean cancelable) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.mipmap.ic_launcher, message, System.currentTimeMillis());
        // String title = context.getString(R.string.app_name);
        String title = message;
        Intent notificationIntent = new Intent(this, class1);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        //notification.setLatestEventInfo(context, title, message, pendingIntent);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

            // Configure the notification channel.
            notificationChannel.setDescription("Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(getString(R.string.app_name))
                //     .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Hai")
                .setContentText(message);

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());

//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Uri notification1 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        try {
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification1);
            r.play();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private boolean GPSEnabled(Context mContext) {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    private void SendDataToserver(Waypoints jsonobject) {


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("trackWaypoints/" + SessionSave.getSession(CommonData.TRACK_ID, LocationUpdate.this)).push();
        Gson gson = new Gson();
        Type type = new TypeToken<List<Waypoints>>() {
        }.getType();
        List<Waypoints> contactList = gson.fromJson(SessionSave.ReadGoogleWaypoints(LocationUpdate.this).toString(), type);
        System.out.println("readingdata" + contactList.size() + "___" + SessionSave.ReadGoogleWaypoints(LocationUpdate.this));
        if (contactList.size() > 0)
            myRef.setValue(contactList.get(0), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        SessionSave.saveGoogleWaypoints(null, null, "", SessionSave.getDistance(LocationUpdate.this), "", LocationUpdate.this);
                    }
                }
            });


    }


    /**
     * For Calculating approx distance for eta and trip if user has buisness key
     */

//    public class FindApproxDistance implements APIResult {
//        int type;
//        LatLng pick = null;
//        LatLng drop = null;
//
//        public FindApproxDistance(LatLng from, LatLng to) {
//            //Toast.makeText(LocationUpdate.this, "Calling Google", Toast.LENGTH_SHORT).show();
//            ArrayList<LatLng> points = new ArrayList<>();
//            pick = from;
//            drop = to;
//            points.add(pick);
//            points.add(drop);
//            String url = new Route().GetDistanceTime(LocationUpdate.this, points, "en", false);
//            if (url != null && !url.equals("")) {
//                if (ROUTE_EXPIRED_TODAY) {
//                    url = "google_geocoding";
//                    JSONObject j = new JSONObject();
//                    try {
//                        j.put("origin", from.latitude + "," + from.longitude);
//                        j.put("destination", to.latitude + "," + to.longitude);
//                        j.put("type", "3");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    new APIService_Retrofit_JSON_NoProgress(LocationUpdate.this, this, j, false, url, false).execute();
//                } else
//
//                    //https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=Washington,DC&destinations=New+York+City,NY&key=YOUR_API_KEY
//                    new APIService_Retrofit_JSON_NoProgress(LocationUpdate.this, this, null, true, url, true).execute();
//
//                System.out.println("carmodel" + url);
//            }
//
//        }
//
//
//        @Override
//        public void getResult(boolean isSuccess, String result) {
//
//            if (isSuccess) {
//                try {
//                    System.out.println("carmodel" + result.replaceAll("\\s", ""));
//                    JSONObject data = new JSONObject(result);
//                    if (!data.getString("status").equalsIgnoreCase("OK")) {
//                        if (result != null && data.getString("status").equalsIgnoreCase("OVER_QUERY_LIMIT") && !ROUTE_EXPIRED_TODAY) {
//                            ROUTE_EXPIRED_TODAY = true;
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    new FindApproxDistance(pick, drop);
//                                }
//                            }, 2000);
//
//                        } else {
//                            double distance = (float) SphericalUtil.computeDistanceBetween(pick, drop) / 1000;
//                            if (SessionSave.getSession("Metric", LocationUpdate.this).trim().equalsIgnoreCase("miles"))
//                                distance = distance / 1.60934;
//                            System.out.println("Haversine Distance" + (distance));
//                            distance += SessionSave.getGoogleDistance(LocationUpdate.this);
//                            SessionSave.setGoogleDistance(distance, LocationUpdate.this);
//                            System.out.println("googledistanceee " + "1_" + pick.latitude + "__" + pick.longitude + "_____" + drop.latitude + "__" + drop.longitude);
//                            SessionSave.saveGoogleWaypoints(pick, drop, "haversine", distance, "UNKNOWN" + result, LocationUpdate.this);
//
//
//                            SessionSave.saveSession("lastknowlats", "" + drop.latitude, LocationUpdate.this);
//                            SessionSave.saveSession("lastknowlngs", "" + drop.longitude, LocationUpdate.this);
//
//
//                            SessionSave.saveWaypoints(pick, drop, "haversine", distance, "" + "___" + startID, LocationUpdate.this);
//
//                            LocalBroadcastManager.getInstance(LocationUpdate.this).sendBroadcast(bintent);
//                        }
//                    } else {
//                        if (new JSONObject(result).getJSONArray("rows").length() != 0) {
//                            JSONObject obj = new JSONObject(result).getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
//                            JSONObject ds = obj.getJSONObject("distance");
//                            String dis = ds.getString("value");
//
//                            double dist = Double.parseDouble(dis) / 1000;
//                            if (SessionSave.getSession("Metric", LocationUpdate.this).trim().equalsIgnoreCase("miles"))
//                                dist = dist / 1.60934;
//                            SessionSave.setGoogleDistance(SessionSave.getGoogleDistance(LocationUpdate.this) + dist, LocationUpdate.this);
//                            System.out.println("googledistanceee " + "2_" + pick.latitude + "__" + pick.longitude + "_____" + drop.latitude + "__" + drop.longitude);
//                            SessionSave.saveGoogleWaypoints(pick, drop, "google", dist, "", LocationUpdate.this);
//
//                            System.out.println("broad__________" + dist + "__" + "__" + SessionSave.getGoogleDistance(LocationUpdate.this));
//
//                            SessionSave.saveSession("lastknowlats", "" + drop.latitude, LocationUpdate.this);
//                            SessionSave.saveSession("lastknowlngs", "" + drop.longitude, LocationUpdate.this);
////                            lastlatitude1 = drop.latitude;
////                            lastlongitude1 = drop.longitude;
////                            SessionSave.saveLastLng(new LatLng(drop.latitude, drop.longitude), LocationUpdate.this);
////                            lastlatitude1 = SessionSave.getLastLng(LocationUpdate.this).latitude;
////                            lastlongitude1 = SessionSave.getLastLng(LocationUpdate.this).longitude;
////                            Handler mHandler = new Handler(Looper.getMainLooper()) {
////                                @Override
////                                public void handleMessage(Message message) {
////                                    // This is where you do your work in the UI thread.
////                                    // Your worker tells you in the message what to do.
////                                    //  Toast.makeText(LocationUpdate.this, "" + SessionSave.getDistance(LocationUpdate.this) + "Accuracy " + mLastLocation.getAccuracy() + " Speed  " + speed + "  Google speed" + SessionSave.getGoogleDistance(LocationUpdate.this), Toast.LENGTH_SHORT).show();
////                                    String savingTripDetail = "";
////                                    savingTripDetail += SessionSave.getSession(SessionSave.getSession("trip_id", LocationUpdate.this) + "data", LocationUpdate.this) + "\n\n\n<br><br>" + "&nbsp;&nbsp;Time&nbsp;" + DateFormat.getTimeInstance().format(new Date()) +
////                                            "&nbsp;&nbsp;old&nbsp;&nbsp;" + lastlatitude1 + "&nbsp;" + lastlongitude1 + "***";
////                                    SessionSave.saveSession(SessionSave.getSession("trip_id", LocationUpdate.this) + "data", savingTripDetail, LocationUpdate.this);
////
////
////                                }
////                            };
////                            mHandler.sendEmptyMessage(0);
//                            SessionSave.saveWaypoints(pick, drop, "haversine", dist, "server" + "___" + startID, LocationUpdate.this);
//                            LocalBroadcastManager.getInstance(LocationUpdate.this).sendBroadcast(bintent);
//                        }
//                    }
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    if (NetworkStatus.isOnline(LocationUpdate.this)) {
//                        if (!ROUTE_EXPIRED_TODAY) {
//                            ROUTE_EXPIRED_TODAY = true;
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    new FindApproxDistance(pick, drop);
//                                }
//                            }, 2000);
//                        } else {
//                            double distance = (float) SphericalUtil.computeDistanceBetween(pick, drop) / 1000;
//                            if (SessionSave.getSession("Metric", LocationUpdate.this).trim().equalsIgnoreCase("miles"))
//                                distance = distance / 1.60934;
//                            System.out.println("Haversine Distance" + (distance));
//                            distance += SessionSave.getGoogleDistance(LocationUpdate.this);
//                            SessionSave.setGoogleDistance(distance, LocationUpdate.this);
//                            System.out.println("googledistanceee " + "3_" + pick.latitude + "__" + pick.longitude + "_____" + drop.latitude + "__" + drop.longitude);
//                            SessionSave.saveGoogleWaypoints(pick, drop, "haversine", distance, e.getLocalizedMessage(), LocationUpdate.this);
//
//
//                            SessionSave.saveSession("lastknowlats", "" + drop.latitude, LocationUpdate.this);
//                            SessionSave.saveSession("lastknowlngs", "" + drop.longitude, LocationUpdate.this);
//                            SessionSave.saveWaypoints(pick, drop, "haversine" + "___" + startID, distance, e.getLocalizedMessage(), LocationUpdate.this);
////                            Handler mHandler = new Handler(Looper.getMainLooper()) {
////                                @Override
////                                public void handleMessage(Message message) {
////                                    // This is where you do your work in the UI thread.
////                                    // Your worker tells you in the message what to do.
////                                    //  Toast.makeText(LocationUpdate.this, "" + SessionSave.getDistance(LocationUpdate.this) + "Accuracy " + mLastLocation.getAccuracy() + " Speed  " + speed + "  Google speed" + SessionSave.getGoogleDistance(LocationUpdate.this), Toast.LENGTH_SHORT).show();
////                                    String savingTripDetail = "";
////                                    savingTripDetail += SessionSave.getSession(SessionSave.getSession("trip_id", LocationUpdate.this) + "data", LocationUpdate.this) + "\n\n\n<br><br>" + "&nbsp;&nbsp;Time&nbsp;" + DateFormat.getTimeInstance().format(new Date()) +
////                                            "&nbsp;&nbsp;old&nbsp;&nbsp;" + lastlatitude1 + "&nbsp;" + lastlongitude1 + "**";
////                                    SessionSave.saveSession(SessionSave.getSession("trip_id", LocationUpdate.this) + "data", savingTripDetail, LocationUpdate.this);
//////                                    lastlatitude1 = drop.latitude;
//////                                    lastlongitude1 = drop.longitude;
//////                                    SessionSave.saveLastLng(new LatLng(drop.latitude, drop.longitude), LocationUpdate.this);
//////                                    lastlatitude1 = SessionSave.getLastLng(LocationUpdate.this).latitude;
//////                                    lastlongitude1 = SessionSave.getLastLng(LocationUpdate.this).longitude;
////
////                                }
////                            };
////                            mHandler.sendEmptyMessage(0);
//                            //LocalBroadcastManager.getInstance(LocationUpdate.this).sendBroadcast(bintent);
//
//
//                        }
//
//                    } else {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                new FindApproxDistance(pick, drop);
//                            }
//                        }, 2000);
//
//
//                    }
//
//
//                }
//
//
//            } else {
//                double distance = (float) SphericalUtil.computeDistanceBetween(pick, drop) / 1000;
//                if (SessionSave.getSession("Metric", LocationUpdate.this).trim().equalsIgnoreCase("miles"))
//                    distance = distance / 1.60934;
//                System.out.println("Haversine Distance" + (distance));
//                distance += SessionSave.getGoogleDistance(LocationUpdate.this);
//                SessionSave.setGoogleDistance(distance, LocationUpdate.this);
//                System.out.println("googledistanceee " + "4_" + pick.latitude + "__" + pick.longitude + "_____" + drop.latitude + "__" + drop.longitude);
//                SessionSave.saveGoogleWaypoints(pick, drop, "haversine", distance, "error" + result, LocationUpdate.this);
//
//                SessionSave.saveSession("lastknowlats", "" + drop.latitude, LocationUpdate.this);
//                SessionSave.saveSession("lastknowlngs", "" + drop.longitude, LocationUpdate.this);
//                SessionSave.saveWaypoints(pick, drop, "haversine", distance, "error" + "___" + startID, LocationUpdate.this);
////                Handler mHandler = new Handler(Looper.getMainLooper()) {
////                    @Override
////                    public void handleMessage(Message message) {
////                        // This is where you do your work in the UI thread.
////                        // Your worker tells you in the message what to do.
////                        //  Toast.makeText(LocationUpdate.this, "" + SessionSave.getDistance(LocationUpdate.this) + "Accuracy " + mLastLocation.getAccuracy() + " Speed  " + speed + "  Google speed" + SessionSave.getGoogleDistance(LocationUpdate.this), Toast.LENGTH_SHORT).show();
////                        String savingTripDetail = "";
////                        savingTripDetail += SessionSave.getSession(SessionSave.getSession("trip_id", LocationUpdate.this) + "data", LocationUpdate.this) + "\n\n\n<br><br>" + "&nbsp;&nbsp;Time&nbsp;" + DateFormat.getTimeInstance().format(new Date()) +
////                                "&nbsp;&nbsp;old&nbsp;&nbsp;" + lastlatitude1 + "&nbsp;" + lastlongitude1 + "*";
////                        SessionSave.saveSession(SessionSave.getSession("trip_id", LocationUpdate.this) + "data", savingTripDetail, LocationUpdate.this);
//////                        lastlatitude1 = drop.latitude;
//////                        lastlongitude1 = drop.longitude;
//////                        SessionSave.saveLastLng(new LatLng(drop.latitude, drop.longitude), LocationUpdate.this);
//////                        lastlatitude1 = SessionSave.getLastLng(LocationUpdate.this).latitude;
//////                        lastlongitude1 = SessionSave.getLastLng(LocationUpdate.this).longitude;
////
////                    }
////                };
////                mHandler.sendEmptyMessage(0);
//                // LocalBroadcastManager.getInstance(LocationUpdate.this).sendBroadcast(bintent);
//            }
//
//
//        }
//    }


    public static String sTimer = "00:00:00";
    private static long startTime = 0L;
    private final Handler myHandler = new Handler();
    public static long timeInMillies = 0L;
    long timeSwap;
    public static long finalTime = 0L;
    public static long saveTime;
    private String Tag;


    public static void ClearSession(Context context) {
        timeInMillies = 0L;
        finalTime = 0L;
        startTime = 0L;
        sTimer = "00:00:00";
        SessionSave.setWaitingTime(0L, context);
        SessionSave.setDistance(0.0, context);
        SessionSave.setGoogleDistance(0f, context);
        SessionSave.saveSession("lastknowlats", "", context);
        SessionSave.saveGoogleWaypoints(null, null, "", 0.0, "", context);
        SessionSave.saveWaypoints(null, null, "", 0.0, "", context);
    }


    private void startTime() {

        Systems.out.println("timer started" + SessionSave.getWaitingTime(LocationUpdate.this));
        startTime = SystemClock.uptimeMillis();
        timeSwap = SessionSave.getWaitingTime(LocationUpdate.this);
        myHandler.postDelayed(updateTimerMethod, 0);
    }

    private final Runnable updateTimerMethod = new Runnable() {
        @Override
        public void run() {
            if (speed < 5) {
                CommonData.iswaitingrunning = true;
                timeInMillies = SystemClock.uptimeMillis() - startTime;
                finalTime = timeSwap + timeInMillies;
                int seconds = (int) (finalTime / 1000);

                int minutes = seconds / 60;

                seconds = seconds % 60;
                int hour = minutes / 60;
                if (minutes >= 60) {
                    minutes = minutes - (hour * 60);
                }
                sTimer = String.format(Locale.UK, "%02d", hour) + ":" + String.format(Locale.UK, "%02d", minutes)
                        + ":" + String.format(Locale.UK, "%02d", seconds);
                Systems.out.println("timer runing" + SessionSave.getWaitingTime(LocationUpdate.this) + "   " + SystemClock.uptimeMillis());

                if (finalTime != 0) {
                    SessionSave.setWaitingTime(finalTime, LocationUpdate.this);
                }
            }
            sendMessage();
            myHandler.postDelayed(this, 1000);
        }
    };

    private void stoptime() {

        timeSwap += SessionSave.getWaitingTime(LocationUpdate.this);
        Systems.out.println("timer stop " + SessionSave.getWaitingTime(LocationUpdate.this));
        myHandler.removeCallbacks(updateTimerMethod);
    }

}


