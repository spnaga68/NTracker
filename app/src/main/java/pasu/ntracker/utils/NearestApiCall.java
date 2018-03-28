package pasu.ntracker.utils;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

import pasu.ntracker.R;

/**
 * Created by Admin on 25-03-2018.
 */

public class NearestApiCall extends AsyncTask<Object, String, String> {

    String googlePlacesData;
    String url;
    Context mContext;
    private static int PROXIMITY_RADIUS = 3000;
    private static LatLng currentLatlng;
    private static GoogleMap mMap;

    public static void getInstance(GoogleMap map, LatLng latLng, Context context) {
        NearestApiCall NearestApiCall = new NearestApiCall();

        String url = getUrl(context, latLng.latitude, latLng.longitude, "gas_station");
        Object[] DataTransfer = new Object[4];
        DataTransfer[0] = map;
        DataTransfer[1] = url;
        DataTransfer[2] = context;
        currentLatlng = latLng;
        mMap = map;
        Log.d("onClick", url);
        NearestApiCall.execute(DataTransfer);
    }

    private static String getUrl(Context context, double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + context.getString(R.string.googleID));
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d("NearestApiCall", "doInBackground entered");
            url = (String) params[1];
            mContext = (Context) params[2];

            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
            Log.d("GooglePlacesReadTask", "doInBackground Exit");
        } catch (Exception e) {
            Log.d("GooglePlacesReadTask", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("GooglePlacesReadTask", "onPostExecute Entered");
        List<HashMap<String, String>> nearbyPlacesList = null;
        DataParser dataParser = new DataParser();
        nearbyPlacesList = dataParser.parse(result);
        ShowNearbyPlaces(nearbyPlacesList, currentLatlng);

        Log.d("GooglePlacesReadTask", "onPostExecute Exit");
    }


    private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList, LatLng currentLatLng) {
        float distance = 0.0f;
        LatLng shortLatLng = null;
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            Log.d("onPostExecute", "Entered into showing locations");
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));

            float[] distances = new float[2];
            Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude, lat, lng, distances);
            Systems.out.println("calleddddd " + "___" + distances[1]);
            if (distance == 0.0 || distances[0] < distance) {
                distance = distances[0];
                shortLatLng = new LatLng(lat, lng);
            }

        }


        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            Log.d("onPostExecute", "Entered into showing locations");
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));


            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);


            float[] distances = new float[2];
            Location.distanceBetween(shortLatLng.latitude, shortLatLng.longitude, lat, lng, distances);
            Systems.out.println("calleddddd " + "___" + distances[0]);
            if (distances[0] < 10) {
                markerOptions.icon(CustomMarker.getMarkerIconFromDrawable(mContext.getResources().getDrawable(R.drawable.ic_gas)));
                Marker m = mMap.addMarker(markerOptions);
                CarMovementAnimation.getInstance().addMarkerAnimate(m);
            } else {
                markerOptions.icon(CustomMarker.getMarkerIconFromDrawable(mContext.getResources().getDrawable(R.drawable.ic_gas_red)));
                Marker m = mMap.addMarker(markerOptions);
                CarMovementAnimation.getInstance().addMarkerAnimate(m);
//                mMap.addMarker(markerOptions);
            }
        }


    }
}