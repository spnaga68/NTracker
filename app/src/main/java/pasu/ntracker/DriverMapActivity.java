package pasu.ntracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import java.util.HashMap;
import java.util.List;

import pasu.ntracker.Service.LocationUpdate;
import pasu.ntracker.data.CommonData;
import pasu.ntracker.data.Tracker;
import pasu.ntracker.utils.CarMovementAnimation;
import pasu.ntracker.utils.CommonInterface;
import pasu.ntracker.utils.CommonUtils;
import pasu.ntracker.utils.CustomMarker;
import pasu.ntracker.utils.NearestApiCall;
import pasu.ntracker.utils.Route;
import pasu.ntracker.utils.SessionSave;

import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * Created by Admin on 24-03-2018.
 */

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, CommonInterface {
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private String TOUR_ID = "";
    private Marker currentMarker;
    private Tracker data;

    private FloatingActionButton currentLocationIcon;

    private TextView distance_travelled, time_travelled, estimate_time, speed, idle_time;
    private int animationCount;
    // Our handler for received Intents. This will be called whenever an Intent
// with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            if (distance_travelled != null) {
                distance_travelled.setText(intent.getFloatExtra(CommonData.TRAVELED_DIST, 0.0f) + " km");

                long timeTravelled = data.getTimeStarted() - System.currentTimeMillis();
                time_travelled.setText(CommonUtils.getTimeHrs(timeTravelled));
                speed.setText(intent.getDoubleExtra(CommonData.TRAVEL_SPEED, 0.0) + " km/hr");
                String estimatedtime = SessionSave.getSession(String.valueOf(data.getPickuplat() + "T" + data.getDroplat()), context);
                if (!estimatedtime.equals("")) {
                    long estimatedArrival = (Long.parseLong(estimatedtime) * 1000) - timeTravelled + intent.getLongExtra(CommonData.TRAVELED_IDLE_TIME, 0L);
                    estimate_time.setText(CommonUtils.getTimeHrsMins(estimatedArrival));
                }
                idle_time.setText(CommonUtils.getTimeHrs(intent.getLongExtra(CommonData.TRAVELED_IDLE_TIME, 0L)));
            }
            if (truckMarker == null) {
                truckMarker = mMap.addMarker(new MarkerOptions().position(SessionSave.getLastLng(DriverMapActivity.this)).icon(CustomMarker.getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.driver_img))));
                CarMovementAnimation.getInstance().addMarkerAnimate(truckMarker);
            } else {

                if (animationCount > 4) {
                    animationCount = 0;
                    float[] ff = new float[1];
                    Location.distanceBetween(truckMarker.getPosition().latitude, truckMarker.getPosition().longitude,
                            data.getDroplat()
                            , data.getDroplng(), ff);
                    if (ff[0] < 200) {
                        CommonUtils.alertDialog(getString(R.string.reached), DriverMapActivity.this);
                    }

                    float[] f = new float[1];
                    Location.distanceBetween(truckMarker.getPosition().latitude, truckMarker.getPosition().longitude,
                            data.getPickuplat()
                            , data.getPickuplng(), f);

                    CarMovementAnimation.getInstance().animateMarker(truckMarker, SessionSave.getLastLng(DriverMapActivity.this), SessionSave.getSession(CommonData.BEARING, DriverMapActivity.this, 0.0f));
                    if (f[0] > 200) {
                        PolylineOptions lineOptions = new PolylineOptions().width(5).color(Color.BLACK).geodesic(true);
                        lineOptions.add(truckMarker.getPosition(), SessionSave.getLastLng(DriverMapActivity.this));
                        lineOptions.width(10);
                        lineOptions.color(Color.BLACK);
                        lineOptions.startCap(new SquareCap());
                        lineOptions.endCap(new SquareCap());
                        lineOptions.jointType(ROUND);
                        Polyline polyline = mMap.addPolyline(lineOptions);
                        polyline.setZIndex(2);
                    }


                } else animationCount++;
            }
        }
    };
    private Marker pickupMarker;
    private Marker dropMarker;
    private Marker truckMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        TOUR_ID = SessionSave.getSession(CommonData.TRACK_ID, this);
        data = CommonUtils.fromJson(SessionSave.getSession(CommonData.CURRENT_TRACK_INFO, this), Tracker.class);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initialize();

    }

    private void initialize() {
        distance_travelled = findViewById(R.id.distance_travelled);
        time_travelled = findViewById(R.id.time_travelled);
        estimate_time = findViewById(R.id.estimate_time);
        speed = findViewById(R.id.speed);
        currentLocationIcon = findViewById(R.id.currentLocationIcon);
        idle_time = findViewById(R.id.idle_time);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(LocationUpdate.BROADCAST_ACTION));

        currentLocationIcon.setOnClickListener(new View.OnClickListener() {
            public AlertDialog dialog;

            @Override
            public void onClick(View v) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(DriverMapActivity.this);
                } else {
                    builder = new AlertDialog.Builder(DriverMapActivity.this);
                }
                dialog = builder.setTitle("Fuel Alert")
                        .setMessage("Fuel percentage is low. Wanna show nearby fuel station ?")
                        .setPositiveButton("Show me", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete

                                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(truckMarker.getPosition(), 15f);

                                mMap.animateCamera(cu);
                                NearestApiCall.getInstance(mMap, truckMarker.getPosition(), DriverMapActivity.this);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                dialog.dismiss();
                            }
                        })
                        .setIcon(R.drawable.ic_gas)
                        .show();
            }


        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
// Customise the styling of the base map using a JSON object defined
// in a raw resource file.
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(DriverMapActivity.this, R.raw.map_style));
            if (!success) {
                System.out.println("Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            System.out.println("Can't find style. Error: ");
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

//the include method will calculate the min and max bound.
        builder.include(data.getPickuplatlng());
        builder.include(data.getDroplatlng());

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.moveCamera(cu);


        pickupMarker = mMap.addMarker(new MarkerOptions()
                .position(data.getPickuplatlng())
                .icon(BitmapDescriptorFactory.fromBitmap(CustomMarker.getMarkerBitmapFromView("Started At", DriverMapActivity.this))));

        dropMarker = mMap.addMarker(new MarkerOptions()
                .position(data.getDroplatlng())
                .icon(BitmapDescriptorFactory.fromBitmap(CustomMarker.getMarkerBitmapFromView("Destination", DriverMapActivity.this))));
        CustomMarker.getMarkerBitmapFromView("", DriverMapActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Route route = new Route();
                route.setUpPolyLine(mMap, DriverMapActivity.this,
                        new LatLng(data.getPickuplat(), data.getPickuplng()), new LatLng(data.getDroplat(), data.getDroplng())
                        , SessionSave.ReadWaypoints(DriverMapActivity.this),DriverMapActivity.this);
            }
        }, 1200);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


    @Override
    public void getMessage(String Message) {

    }

    @Override
    public void getPolyline(Polyline polyline) {

    }
}