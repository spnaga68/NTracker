package pasu.ntracker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.HashMap;

import pasu.ntracker.Service.LocationUpdate;
import pasu.ntracker.data.CommonData;
import pasu.ntracker.data.Tracker;
import pasu.ntracker.utils.CommonUtils;
import pasu.ntracker.utils.SessionSave;
import pasu.ntracker.viewer.VideoActivityMain;


public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 420;
    private AppCompatButton start;
    private AppCompatButton list;
    private AppCompatButton stop, track;
    private double pickuplat = 11.031712, pickuplng = 77.018712, droplat = 11.039167, droplng = 77.036476;
    private Tracker data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = (AppCompatButton) findViewById(R.id.start);
        track = (AppCompatButton) findViewById(R.id.track);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                VideoActivityMain
                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("trackDetail").push();
                data = new Tracker();
                data.setVechicleID("123");
                data.setTrackID(myRef.getKey());
                data.setPickuplat(pickuplat);
                data.setPickuplng(pickuplng);
                data.setDroplat(droplat);
                data.setDroplng(droplng);
                data.setTimeStarted(new Date().getTime());
                myRef.setValue(data, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            SessionSave.saveSession(CommonData.TRACK_ID, databaseReference.getKey(), MainActivity.this);
                            SessionSave.saveSession(CommonData.CURRENT_TRACK_INFO, CommonUtils.toJson(data), MainActivity.this);
                            stop.setVisibility(View.VISIBLE);
                            start.setVisibility(View.GONE);
                            checkID();
                            startActivity(new Intent(MainActivity.this, DriverMapActivity.class));
                        }
                    }
                });
            }
        });

        list = (AppCompatButton) findViewById(R.id.list);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, VideoActivityMain.class));
            }
        });
        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                startActivity(new Intent(MainActivity.this, DriverMapActivity.class));
            }
        });


        stop = (AppCompatButton) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                VideoActivityMain
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("trackDetail/" + SessionSave.getSession(CommonData.TRACK_ID, MainActivity.this));
                Tracker data = new Tracker();
                data.setVechicleID("123");
                data.setTrackID(myRef.getKey());

                data.setTimeStarted(new Date().getTime());
                HashMap<String, Object> map = new HashMap<>();
                map.put("timeEnded", new Date().getTime());
                myRef.updateChildren(map, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            SessionSave.saveSession(CommonData.TRACK_ID, "", MainActivity.this);
                            LocationUpdate.stop(MainActivity.this);
                            stop.setVisibility(View.GONE);
                            start.setVisibility(View.VISIBLE);
                            LocationUpdate.ClearSession(MainActivity.this);
                        }
                    }
                });
            }
        });

        checkID();
    }

    private void checkID() {
        if (!SessionSave.getSession(CommonData.TRACK_ID, MainActivity.this).equals("")) {
//            startActivity(new Intent(MainActivity.this, MapsActivity.class));
            LocationUpdate.startLocationService(MainActivity.this);
            start.setVisibility(View.GONE);
            stop.setVisibility(View.VISIBLE);
            track.setVisibility(View.VISIBLE);
            stop.setText("stop " + SessionSave.getSession(CommonData.TRACK_ID, MainActivity.this));
        } else {
            start.setVisibility(View.VISIBLE);
            track.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }
}