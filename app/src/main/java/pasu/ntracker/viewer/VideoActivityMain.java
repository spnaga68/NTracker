package pasu.ntracker.viewer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import pasu.ntracker.MainActivity;
import pasu.ntracker.MapsActivity;
import pasu.ntracker.R;
import pasu.ntracker.data.Tracker;

/**
 * Created by developer on 26/9/17.
 */


public class VideoActivityMain extends AppCompatActivity {


    private static final String TAG = "VideoActivityMain";
    private RecyclerView recyclerview;
    private int toplay;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simplelistview);
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(mLayoutManager);
        //recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerview.setItemAnimator(new DefaultItemAnimator());


        FirebaseDatabase.getInstance().getReference("trackDetail/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Tracker> arrayList = new ArrayList<>();
                for (DataSnapshot md : dataSnapshot.getChildren()) {
                    if (md.getValue() != null && !md.getValue().equals("")) {
                        Tracker matchDetails = md.getValue(Tracker.class);

                        arrayList.add(matchDetails);
                    }
                }
                try {
                    if (arrayList != null) {
                        recyclerview.setAdapter(new VideoAdapter(VideoActivityMain.this, arrayList));

                        //  ((VideoAdapter)recyclerview.getAdapter()).setSelection(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }//End of onCreate


//-------------------------------------------------------ANDROID LIFECYCLE---------------------------------------------------------------------------------------------

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()...");
    }

}
