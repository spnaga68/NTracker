package pasu.ntracker.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pasu.ntracker.DriverMapActivity;
import pasu.ntracker.R;
import pasu.ntracker.data.CommonData;
import pasu.ntracker.data.Waypoints;
import pasu.ntracker.utils.Systems;

/**
 * Created by developer on 28/3/18.
 */

public class CurrentTripTracker extends Service {
    private ScheduledExecutorService mTimer = Executors.newSingleThreadScheduledExecutor();
    private Waypoints lastWayPoints;
    private Waypoints latestWayPoints;
    private static NotificationManager notificationManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("showwwwwwwww "+"CurrentTripTracker service started");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("trackWaypoints/" + CommonData.CURRENT_TRACK_ID);
        final String TAG = "Commentary itemArrayList";

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//
                Iterator iterator = dataSnapshot.getChildren().iterator();
                int length = 1;
                while (iterator.hasNext()){
                    System.out.println("calleddddd "+"__"+length+"__"+dataSnapshot.getChildrenCount());
                    if (length == dataSnapshot.getChildrenCount() ) {
                        DataSnapshot snapshot  = (DataSnapshot) iterator.next();
                        Waypoints waypoints = snapshot.getValue(Waypoints.class);
                        System.out.println("calleddddd "+"__"+length+"__"+dataSnapshot.getChildrenCount()+"__"+waypoints);
                            final Query queryRef;
                            Systems.out.println("postionnnncomm" + CommonData.CURRENT_TRACK_ID + "___" + database.getReference("trackWaypoints/" + CommonData.CURRENT_TRACK_ID));
                            queryRef = myRef
                                    .orderByChild("time").startAt(waypoints.getTime());

                            queryRef.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    Systems.out.println("commentary added*" + dataSnapshot.getValue());
                                    if (lastWayPoints == null) {
                                        lastWayPoints = dataSnapshot.getValue(Waypoints.class);
                                        System.out.println("showwwwwwwww " + "lastWayPoints");
                                    } else {
                                        System.out.println("showwwwwwwww " + "latestWayPoints");
                                        latestWayPoints = dataSnapshot.getValue(Waypoints.class);
                                    }

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
                    length++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mTimer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                calculateTimeTaken();
            }
    }, 0, 15000L, TimeUnit.MILLISECONDS);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void calculateTimeTaken() {
        if (lastWayPoints != null && latestWayPoints != null) {
            if (((latestWayPoints.getTime() - lastWayPoints.getTime()) / 1000) == 0) {
                System.out.println("showwwwwwwww " + "time 0");
                generateNotifications(CurrentTripTracker.this, "Location Not Updated", DriverMapActivity.class, false);
               lastWayPoints = latestWayPoints;
               latestWayPoints = null;
            } else if (((latestWayPoints.getTime() - lastWayPoints.getTime()) / 1000) > 15) {
                System.out.println("showwwwwwwww " + "time more than 15secs");
                generateNotifications(CurrentTripTracker.this, "Location Not Updated more than 15secs", DriverMapActivity.class, false);
                lastWayPoints = latestWayPoints;
                latestWayPoints = null;
            } else if ((latestWayPoints.getDist() - lastWayPoints.getDist()) < 200) {
                System.out.println("showwwwwwwww " + "dist less than 200mts");
                generateNotifications(CurrentTripTracker.this, "Distance more than 200mts", DriverMapActivity.class, false);
                lastWayPoints = latestWayPoints;
                latestWayPoints = null;
            }
        }
    }



    public void generateNotifications(Context context, String message, Class<?> class1, boolean cancelable) {
        if (notificationManager != null){
            notificationManager.cancel(1);
        }
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.ic_launcher, message, System.currentTimeMillis());
        // String title = context.getString(R.string.app_name);
        String title = message;
        Intent notificationIntent = new Intent(this, class1);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        //notification.setLatestEventInfo(context, title, message, pendingIntent);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setAutoCancel(true);
        builder.setTicker("Taximobility");
        builder.setContentTitle("NTracker");
        builder.setContentText(message);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.small_logo);
            builder.setColor(ContextCompat.getColor(getBaseContext(), R.color.colorAccent));
        } else {
            builder.setSmallIcon(R.drawable.small_logo);
        }
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap());
        //builder.setSubText("This is subtext...");   //API level 16
//        builder.setNumber(100);
        builder.build();

        Notification myNotication = builder.getNotification();


        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1, myNotication);
        Uri notification1 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        try {
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification1);
            r.play();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }
}
