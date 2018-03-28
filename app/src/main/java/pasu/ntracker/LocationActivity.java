package pasu.ntracker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import pasu.ntracker.Service.LocationUpdate;
import pasu.ntracker.data.CommonData;
import pasu.ntracker.data.Tracker;
import pasu.ntracker.utils.CommonUtils;
import pasu.ntracker.utils.MapWrapperLayout;
import pasu.ntracker.utils.SessionSave;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationActivity extends AppCompatActivity implements GoogleMap.OnCameraIdleListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnCameraMoveStartedListener {
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    public static int z = 1;
    private static boolean GEOCODE_EXPIRY = false;
    public String currentlocTxt;
    AutoCompleteTextView mLocation;
    GoogleMap googleMap;
    LatLng latLng;
    String type = "pickup";
    Marker p_marker;
    ImageView mapppin;
    TextView txt_location;
    Button btn_view;
    String location = "";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private MapWrapperLayout mapWrapperLayout;
    private Runnable callAddress_drag;
    private LocationActivity.Address_s address;
    private String pickuplocTxt;
    private Handler handlerServercall1;
    private TextView drop_location;
    private LatLng drop_latLng;
    private CardView pickuplay, droplay;
    private CheckBox cb_pickup, cb_drop;
    private LatLng pickupLatLng;
    private Place pickup_Place, drop_Place;

    //    private APIService mAPIService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        pickuplay = (CardView) findViewById(R.id.pickup_lay);
        droplay = (CardView) findViewById(R.id.drop_lay);
        cb_pickup = (CheckBox) findViewById(R.id.checkbox_pick);
        cb_drop = (CheckBox) findViewById(R.id.checkbox_drop);
        mLocation = (AutoCompleteTextView) findViewById(R.id.edtLocation);
        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        txt_location = (TextView) findViewById(R.id.txt_location);
        drop_location = (TextView) findViewById(R.id.txt_drop_location);
        btn_view = (Button) findViewById(R.id.btn_view);
        mapppin = (ImageView) findViewById(R.id.mapppin);
        handlerServercall1 = new Handler(Looper.getMainLooper());
//        Bundle b = getIntent().getExtras();
//        if (b != null) {
//            type = b.getString("type");
//            if (type.equals("P")) {
//                txt_location.setText("Select Pickup Location");
//            } else {
//                txt_location.setText("Select Drop Location");
//            }
//        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        if (this.mGoogleApiClient != null) {
            this.mGoogleApiClient.connect();
        }


        txt_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cb_pickup.setChecked(true);
                cb_drop.setChecked(false);
                txt_location.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txt_location.setClickable(true);
                    }
                }, 500);
                type = "pickup";
//                Glide.with(LocationActivity.this).load(R.drawable.flag_green).into(mapppin);
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(LocationActivity.this);
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                    e.printStackTrace();
                }
            }
        });
        drop_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cb_pickup.setChecked(false);
                cb_drop.setChecked(true);
                drop_location.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drop_location.setEnabled(true);
                    }
                }, 500);
                type = "dropoff";
//                Glide.with(LocationActivity.this).load(R.drawable.flag_red).into(mapppin);
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(LocationActivity.this);
                    startActivityForResult(intent, 2);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                    e.printStackTrace();
                }
            }
        });

        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        callAddress_drag = new Runnable() {
            @Override
            public void run() {
                Log.e("Locationaa", "onCameraChange: ca" + z);
                if (z == 1) {

                    LatLng ss = null;
                    if (latLng != null)
                        ss = latLng;
                    else
                        ss = latLng;
                    if (address != null)
                        address.cancel(true);
                    Log.e("Location_address", "onCameraChange: ca");
                    address = new Address_s(LocationActivity.this, new LatLng(ss.latitude, ss.longitude), type);
                    try {
                        address.execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                } else {
                    Log.e("Check your internet", "check the internet");
//                        errorInConnection(getString(R.string.check_internet_connection));
                }
            }

        };

//        googleMap.setOnCameraMoveStartedListener(this);
//        googleMap.setOnCameraIdleListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        cb_pickup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                buttonView.setChecked(isChecked);
                cb_drop.setChecked(!isChecked);

                if (isChecked) {
                    type = "pickup";
                    Glide.with(LocationActivity.this).load(R.drawable.flag_green).into(mapppin);
                    if (pickupLatLng != null && googleMap != null) {
                        System.out.println("resulttt " + pickupLatLng.latitude + "___" + pickupLatLng.longitude);
                        googleMap.setOnCameraIdleListener(null);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 16f));
                        enableIdleListner(500);
                    }
                }
            }
        });
        cb_drop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                buttonView.setChecked(isChecked);
                cb_pickup.setChecked(!isChecked);
                if (isChecked) {
                    type = "dropoff";
                    if (drop_latLng != null && googleMap != null) {
                        Glide.with(LocationActivity.this).load(R.drawable.flag_red).into(mapppin);
                        System.out.println("resulttt " + drop_latLng.latitude + "___" + drop_latLng.longitude);
                        googleMap.setOnCameraIdleListener(null);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(drop_latLng, 16f));
                        enableIdleListner(500);
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(drop_latLng, 16f));
                    }
                }
            }
        });
        btn_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pickup = txt_location.getText().toString();
                String drop = drop_location.getText().toString();


                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("trackDetail").push();
                final Tracker data = new Tracker();
                data.setVechicleID("123");
                data.setTrackID(myRef.getKey());
                data.setPickuplat(pickupLatLng.latitude);
                data.setPickuplng(pickupLatLng.longitude);
                data.setDroplat(drop_latLng.latitude);
                data.setDroplng(drop_latLng.longitude);
                data.setPickAddress(pickup);
                data.setDropAddress(drop);
                data.setTimeStarted(new Date().getTime());
                myRef.setValue(data, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            SessionSave.saveSession(CommonData.TRACK_ID, databaseReference.getKey(), LocationActivity.this);
                            SessionSave.saveSession(CommonData.CURRENT_TRACK_INFO, CommonUtils.toJson(data), LocationActivity.this);
                            LocationUpdate.startLocationService(LocationActivity.this);
                            startActivity(new Intent(LocationActivity.this, DriverMapActivity.class));
                            finish();
                        }
                    }
                });
            }
        });
    }

    private void enableIdleListner(int i) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                googleMap.setOnCameraIdleListener(LocationActivity.this);
            }
        }, i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            System.out.println("calleddddd " + "11");
            if (resultCode == RESULT_OK) {
                type = "pickup";
                if (pickup_Place != null) {
                    pickup_Place = null;
                }
                pickup_Place = PlaceAutocomplete.getPlace(this, data);
                Log.i("", "Place: " + pickup_Place.getName());
                txt_location.setText(pickup_Place.getAddress());
                location = String.valueOf(pickup_Place.getAddress());
                Log.i("nn--", "Place: " + pickup_Place.getLatLng());//get place details here
                pickupLatLng = pickup_Place.getLatLng();

                LatLng ll = new LatLng(pickupLatLng.latitude, pickupLatLng.longitude);
                if (ll != null && googleMap != null)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 16f));
                mapppin.setVisibility(View.VISIBLE);


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                cb_pickup.setChecked(true);
                cb_drop.setChecked(false);
                System.out.println("resulttt " + "pickup cancelled");
                if (pickupLatLng != null && googleMap != null) {
                    System.out.println("resulttt " + pickupLatLng.latitude + "___" + pickupLatLng.longitude);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng, 16f));
                }
            }
        } else if (requestCode == 2) {
            System.out.println("calleddddd " + "333");
            if (resultCode == RESULT_OK) {
                type = "dropoff";
                if (drop_Place != null) {
                    drop_Place = null;
                }
                drop_Place = PlaceAutocomplete.getPlace(this, data);
                Log.i("", "Place: " + drop_Place.getName());
                drop_location.setText(drop_Place.getAddress());
                location = String.valueOf(drop_Place.getAddress());
                Log.i("nn--", "Place: " + drop_Place.getLatLng());//get place details here
                drop_latLng = drop_Place.getLatLng();

                mapppin.setVisibility(View.VISIBLE);

                LatLng ll = new LatLng(drop_latLng.latitude, drop_latLng.longitude);
                if (ll != null && googleMap != null) {
                    System.out.println("calleddd " + drop_latLng.latitude + "___" + drop_latLng.longitude);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, 16f));
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                cb_pickup.setChecked(false);
                cb_drop.setChecked(true);
                System.out.println("resulttt " + " cancelled");
                if (drop_latLng != null && googleMap != null) {
                    System.out.println("resulttt " + drop_latLng.latitude + "___" + drop_latLng.longitude);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(drop_latLng, 16f));
                }

            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("", "Google Places API connection onConnectionFailed.");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (pickupLatLng == null) {

            pickupLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            cb_pickup.setChecked(true);
            latLng = pickupLatLng;
            handlerServercall1.postDelayed(callAddress_drag, 800);
        }
        System.out.println("nn--onLocationChanged" + location.getLatitude() + "___________" + location.getLongitude());
    }

    public int getPixelsFromDp(final Context context, final float dp) {

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onMapReady(GoogleMap Map) {
        System.out.println("nn--onMapReady");
        this.googleMap = Map;
        this.googleMap.setMyLocationEnabled(true);
        googleMap.setOnCameraMoveStartedListener(this);
        googleMap.setOnCameraIdleListener(this);
        mapWrapperLayout.init(googleMap, getPixelsFromDp(LocationActivity.this, 39 + 20), true);
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                Log.i("centerLat", String.valueOf(cameraPosition.target.latitude));

                Log.i("centerLong", String.valueOf(cameraPosition.target.longitude));
            }
        });


    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        System.out.println("nn--onMarkerDragStart");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        System.out.println("nn--onMarkerDrag");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        System.out.println("nn--onMarkerDragEnd");
    }

    @Override
    public void onCameraMoveStarted(int i) {

        if (MapWrapperLayout.ismMapIsTouched()) {
            MapWrapperLayout.setmMapIsTouched(true);
            System.out.println("nn--truuuuuuu");
        } else {
            System.out.println("nn--falseeee");

        }
    }

    @Override
    public void onCameraIdle() {
        System.out.println("nn--falseeeeiii");
        latLng = googleMap.getCameraPosition().target;
        handlerServercall1.removeCallbacks(callAddress_drag);
        handlerServercall1.postDelayed(callAddress_drag, 800);
    }

    public void setPickuplocTxt(String pickuplocTxt, String typeLoc) {
        pickuplocTxt = pickuplocTxt.replaceAll("null", "").replaceAll(", ,", "").replaceAll(", ,", "");
        LocationActivity.this.pickuplocTxt = pickuplocTxt;
        currentlocTxt = (pickuplocTxt.replace(", null", ""));
        System.out.println("address" + pickuplocTxt);
        if (typeLoc.equals("pickup"))
            txt_location.setText(pickuplocTxt);
        else
            drop_location.setText(pickuplocTxt);
    }

    public void convertLatLngtoAddressApi(final Context c, final double lati, final double longi) {
        String googleMapUrl;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface service = retrofit.create(ApiInterface.class);


        service.getCityResults(lati + "," + longi, false).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String places = response.body().string();
                    System.out.println("nn--places" + places);
                    setLocation(places, c, lati, longi);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
//            googleMapUrl = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lati + "," + longi + "&sensor=false";
    }

    public void setLocation(String inputJson, Context mContext, Double latitude, Double longitude) {

        try {
            if (mContext != null && inputJson != null) {
                //Toast.makeText(getActivity(), "convert the lat/lng to text line 4066" + inputJson + " latitude" + latitude + " , longitude " + longitude + " goecode_expiry " + GEOCODE_EXPIRY, Toast.LENGTH_SHORT).show();
                System.out.println("____SDD____3dd" + inputJson);
                JSONObject object = new JSONObject("" + inputJson);
                JSONArray array = object.getJSONArray("results");

                try {
                    object = array.getJSONObject(0);
                    JSONArray addressComponent = object.getJSONArray("address_components");
                    for (int i = 0; i < addressComponent.length(); i++) {
                        JSONObject ob = addressComponent.getJSONObject(i);
//                        if (ob.getJSONArray("types").getString(0).equals("locality")) {
//                            defaultCityName = ob.getString("long_name");
//                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("exception of googleapi" + ex.getLocalizedMessage() + "____" + ex.getMessage());
        }
    }

    public class Address_s extends AsyncTask<String, String, String> {
        public Context mContext;
        LatLng mPosition;
        String Address = "";
        Geocoder geocoder;
        List<android.location.Address> addresses = new ArrayList<>();
        private double latitude;
        private double longitude;
        private String typeLocation;

        public Address_s(Context context, LatLng position, String type) {
            Bundle params = new Bundle();
            params.putString("user", position.latitude + "," + position.longitude);

            params.putString("type", "passenger");
//            mFirebaseAnalytics.logEvent("addresscalled", params);
            mContext = context;
            mPosition = position;
            latitude = mPosition.latitude;
            longitude = mPosition.longitude;
            this.typeLocation = type;
            try {
                geocoder = new Geocoder(context, Locale.getDefault());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
//              showDialog();
//        SessionSave.saveSession("notes", "", mContext);
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                System.out.println("address size11:" + latitude + "%$#" + longitude);

                if (Geocoder.isPresent()) {
                    addresses = geocoder.getFromLocation(latitude, longitude, 3);
                    System.out.println("_________11111111111" + addresses);
                    if (addresses != null && addresses.size() == 0) {

                        convertLatLngtoAddressApi(mContext, latitude, longitude);

                    } else {
                        System.out.println("address size:@@" + addresses.size());
                        for (int i = 0; i < addresses.size(); i++) {
                            Address += addresses.get(0).getAddressLine(i) + ", ";
                        }
                        if (Address.length() > 0)
                            Address = Address.substring(0, Address.length() - 2);
                    }
                } else {
                    System.out.println("address size:@V" + addresses.size());
//                if (NetworkStatus.isOnline(mContext))
                    System.out.println("______22222222222");
                    convertLatLngtoAddressApi(mContext, latitude, longitude);
//                else {
//                }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("address size:@#");
//            if (NetworkStatus.isOnline(mContext))
                convertLatLngtoAddressApi(mContext, latitude, longitude);
//            else {
//            }
            }
            return Address;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            //closeDialog();
            if (cb_pickup.isChecked())
                pickupLatLng = latLng;
            if (cb_drop.isChecked())
                drop_latLng = latLng;
            if (Address.length() != 0) {
                setPickuplocTxt(Address, typeLocation);
            } else {
                GEOCODE_EXPIRY = !GEOCODE_EXPIRY;
//                    if(!GEOCODE_EXPIRY)
//                    errorInGettingAddress("Can't able to get address due to status 3981",latitude,longitude);
            }
        }

    }

}




