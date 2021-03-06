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
import android.widget.Toast;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import pasu.ntracker.Service.CurrentTripTracker;
import pasu.ntracker.Service.LocationUpdate;
import pasu.ntracker.data.CommonData;
import pasu.ntracker.data.Tracker;
import pasu.ntracker.data.Waypoints;
import pasu.ntracker.utils.CarMovementAnimation;
import pasu.ntracker.utils.CommonInterface;
import pasu.ntracker.utils.CommonUtils;
import pasu.ntracker.utils.CustomMarker;
import pasu.ntracker.utils.NearestApiCall;
import pasu.ntracker.utils.Route;
import pasu.ntracker.utils.SessionSave;
import pasu.ntracker.utils.Systems;

import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * Created by Admin on 24-03-2018.
 */

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, CommonInterface {
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private String TOUR_ID = "";
    private Tracker data;
    ArrayList<Waypoints> arrayList = new ArrayList<>();
    private FloatingActionButton currentLocationIcon;

    private TextView distance_travelled, time_travelled, estimate_time, speed, idle_time;
    private int animationCount;
    // Our handler for received Intents. This will be called whenever an Intent
// with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            updateRoutePoints(intent.getFloatExtra(CommonData.TRAVELED_DIST, 0.0f), intent.getDoubleExtra(CommonData.TRAVEL_SPEED, 0.0)
                    , intent.getLongExtra(CommonData.TRAVELED_IDLE_TIME, 0L)
                    , SessionSave.getSession(String.valueOf(data.getPickuplat() + "T" + data.getDroplat()), DriverMapActivity.this)
                    , data.getTimeStarted() - System.currentTimeMillis(), SessionSave.getLastLng(DriverMapActivity.this));
        }
    };
    private Handler handler = new Handler();
    private LatLng latestLatLng;
    private Runnable carMovement = new Runnable() {
        @Override
        public void run() {
            if (latestLatLng != null)
                CarMovementAnimation.getInstance().animateMarker(truckMarker, latestLatLng, SessionSave.getSession(CommonData.BEARING, DriverMapActivity.this, 0.0f));
        }
    };

    private void updateRoutePoints(float travelledDistance, double speedValue, long idle,
                                   String estimatedtime, long timeTravelled, LatLng latestLatLng) {
        String message = "" + idle;
        Log.d("receiver", "Got message: " + message);
        if (distance_travelled != null) {
            distance_travelled.setText(String.format(Locale.UK, "%.2f", travelledDistance) + " km");

            time_travelled.setText(CommonUtils.getTimeHrs(timeTravelled));
            speed.setText(speedValue + " km/hr");
            if (estimatedtime != null && !estimatedtime.equals("")) {
                long estimatedArrival = (Long.parseLong(estimatedtime) * 1000) - timeTravelled + idle;
                estimate_time.setText(CommonUtils.getTimeHrsMins(estimatedArrival));
            }
            idle_time.setText(CommonUtils.getTimeHrs(idle));
        }
        if (truckMarker == null) {
            truckMarker = mMap.addMarker(new MarkerOptions().position(latestLatLng).icon(CustomMarker.getMarkerIconFromDrawable(getResources().getDrawable(R.drawable.driver_img))));
            CarMovementAnimation.getInstance().addMarkerAnimate(truckMarker);
        } else {

            if (animationCount > 4 || isReceiver) {
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
                this.latestLatLng = latestLatLng;
                handler.removeCallbacks(carMovement);
                handler.postDelayed(carMovement, 2000);
                if (f[0] > 100 && !isReceiver) {

                    PolylineOptions lineOptions = new PolylineOptions().width(5).color(Color.BLACK).geodesic(true);
                    lineOptions.add(truckMarker.getPosition(), latestLatLng);
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

    private Marker pickupMarker;
    private Marker dropMarker;
    private Marker truckMarker;
    private boolean isReceiver;
    ArrayList<Integer> colors = new ArrayList<>();
    private int color_var;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        stopService(new Intent(DriverMapActivity.this, CurrentTripTracker.class));
//        startService(new Intent(DriverMapActivity.this, CurrentTripTracker.class));
        if (getIntent() != null && getIntent().getStringExtra("type") != null) {
            if (getIntent().getStringExtra("type").equals(CommonData.RECEIVER_ACTIVITY))
                isReceiver = true;
            TOUR_ID = getIntent().getStringExtra(CommonData.TRACK_ID);
            data = CommonUtils.fromJson(getIntent().getStringExtra(CommonData.CURRENT_TRACK_INFO), Tracker.class);
        } else {

            TOUR_ID = SessionSave.getSession(CommonData.TRACK_ID, this);
            data = CommonUtils.fromJson(SessionSave.getSession(CommonData.CURRENT_TRACK_INFO, this), Tracker.class);

        }
//
//        colors.add(Color.parseColor("#26C6DA"));
//        colors.add(Color.parseColor("#00BCD4"));
//        colors.add(Color.parseColor("#00ACC1"));
//        colors.add(Color.parseColor("#0097A7"));
//        colors.add(Color.parseColor("#00838F"));


        colors.add(Color.parseColor("#0097A7"));
        colors.add(Color.parseColor("#0097A7"));
        colors.add(Color.parseColor("#0097A7"));
        colors.add(Color.parseColor("#0097A7"));
        colors.add(Color.parseColor("#0097A7"));

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

        if (!isReceiver)
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

        if (isReceiver) {
            getPreviousLatLng();
            getCurrentLocation();
        }
    }

    private void getPreviousLatLng() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("trackWaypoints/" + TOUR_ID);
        final String TAG = "Commentary itemArrayList";

//        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                Systems.out.println(TAG + dataSnapshot.getValue());
//
//                for (DataSnapshot md : dataSnapshot.getChildren()) {
//                    if (md.getValue() != null && !md.getValue().equals("")) {
//                        Waypoints matchDetails = md.getValue(Waypoints.class);
//
//                        arrayList.add(matchDetails);
//                        if (arrayList.size() > 2) {
//                            addMultiColorPolyLine(arrayList);
//
//                        }
//                    }
//                }
//
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


    }

    private void addMultiColorPolyLine(ArrayList<Waypoints> arrayList) {
        if (arrayList.size() > 2) {

            PolylineOptions lineOptions = new PolylineOptions().width(5).color(colors.get(color_var)).geodesic(true);
            lineOptions.add(arrayList.get(arrayList.size() - 2).getPickupLatlng(), arrayList.get(arrayList.size() - 1).getPickupLatlng());
            lineOptions.width(10);
            lineOptions.color(colors.get(color_var));
            lineOptions.startCap(new SquareCap());
            lineOptions.endCap(new SquareCap());
            lineOptions.jointType(ROUND);
            Polyline polyline = mMap.addPolyline(lineOptions);
            polyline.setZIndex(2);
            polyline.setTag(arrayList.get(arrayList.size() - 1));
            polyline.setClickable(true);
        }
        if (color_var > 3)
            color_var = 0;
        else
            color_var++;
    }


    private void getCurrentLocation() {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("trackWaypoints/" + TOUR_ID);
        final String TAG = "Commentary itemArrayList";

        Query queryRef;
        Systems.out.println("postionnnncomm" + TOUR_ID);
        queryRef = myRef
                .orderByChild("time");
        queryRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Systems.out.println("commentary added*" + dataSnapshot.getValue());
                        final Waypoints data = dataSnapshot.getValue(Waypoints.class);
                        LatLng sydney = new LatLng(data.getDroplat(), data.getDroplng());
//                        if (currentMarker != null)
//                            currentMarker.remove();
//                        currentMarker = mMap.addMarker(new MarkerOptions().position(sydney).title(CommonUtils.getDate(data.getTime())));
                        //   mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 017f));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateRoutePoints((float) data.getDist(), data.getSpeedValue(), data.getIdle(), data.getEstimatedtime(), data.getTimeTravelled(), data.getDropLatlng());
                                arrayList.add(data);
                                if (DriverMapActivity.this != null)
                                    addMultiColorPolyLine(arrayList);

                            }
                        }, 3000);

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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
                Systems.out.println("Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Systems.out.println("Can't find style. Error: ");
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

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {




                //do something with polyline
                Waypoints data = (Waypoints) polyline.getTag();
                Toast.makeText(DriverMapActivity.this, CommonUtils.getDate(data.getTime()), Toast.LENGTH_LONG).show();
            }
        });
        pickupMarker = mMap.addMarker(new MarkerOptions()
                .position(data.getPickuplatlng())
                .icon(BitmapDescriptorFactory.fromBitmap(CustomMarker.getMarkerBitmapFromView(data.getPickAddress(), DriverMapActivity.this))));

        dropMarker = mMap.addMarker(new MarkerOptions()
                .position(data.getDroplatlng())
                .icon(BitmapDescriptorFactory.fromBitmap(CustomMarker.getMarkerBitmapFromView(data.getDropAddress(), DriverMapActivity.this))));
        CustomMarker.getMarkerBitmapFromView("", DriverMapActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Route route = new Route();
                route.setUpPolyLine(mMap, DriverMapActivity.this,
                        new LatLng(data.getPickuplat(), data.getPickuplng()), new LatLng(data.getDroplat(), data.getDroplng())
                        , isReceiver ? null : SessionSave.ReadWaypoints(DriverMapActivity.this), DriverMapActivity.this);
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