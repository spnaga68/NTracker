package pasu.ntracker.viewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import pasu.ntracker.R;
import pasu.ntracker.data.CommonData;
import pasu.ntracker.data.Waypoints;
import pasu.ntracker.utils.CommonUtils;
import pasu.ntracker.utils.SessionSave;

/**
 * Created by Admin on 21-03-2018.
 */

public class ReceiverActivity extends AppCompatActivity implements OnMapReadyCallback {
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private String TOUR_ID = "";
    private Marker currentMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TOUR_ID = getIntent().getStringExtra(CommonData.TRACK_ID);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getCurrentLocation();
    }


    private void getCurrentLocation() {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("trackWaypoints/" + TOUR_ID);
        final String TAG = "Commentary itemArrayList";

        Query queryRef;
        System.out.println("postionnnncomm" + TOUR_ID);
        queryRef = myRef
                .orderByChild("time");
        queryRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        System.out.println("commentary added" + dataSnapshot.getValue());
                        Waypoints data = dataSnapshot.getValue(Waypoints.class);
                        LatLng sydney = new LatLng(data.getDroplat(), data.getDroplng());
                        if(currentMarker!=null)
                            currentMarker.remove();
                        currentMarker = mMap.addMarker(new MarkerOptions().position(sydney).title(CommonUtils.getDate(data.getTime())));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,017f));


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

}
