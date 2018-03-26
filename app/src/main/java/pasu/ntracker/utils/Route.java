package pasu.ntracker.utils;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import pasu.ntracker.Service.getPolyline;
import pasu.ntracker.data.CommonData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.google.android.gms.maps.model.JointType.ROUND;


/**
 * Draw the route to the map object .
 * Routes are drawn with attributes according to the constructor its triggered.
 */
public class Route {
    GoogleMap mMap;
    Context context;
    String lang;
    int colorcode;
    static String LANGUAGE_SPANISH = "es";
    static String LANGUAGE_ENGLISH = "en";
    static String LANGUAGE_FRENCH = "fr";
    static String LANGUAGE_GERMAN = "de";
    static String LANGUAGE_CHINESE_SIMPLIFIED = "zh-CN";
    static String LANGUAGE_CHINESE_TRADITIONAL = "zh-TW";
    public static boolean ROUTE_EXPIRED_TODAY = false;
    private Polyline startPolyline, finalPolyline;
    private List<LatLng> listLatLng = new ArrayList<>();
    private CommonInterface commonInterface;
    int count = 0;
    private JSONArray secListLatLng;
    private LatLng src, dest;

    public void setUpPolyLine(final GoogleMap map, final FragmentActivity mcontext,
                              final LatLng source, final LatLng destination, final JSONArray secListLatLng, final CommonInterface commonInterface) {
        this.mMap = map;
        this.context=mcontext;
        this.commonInterface = commonInterface;
//        final Fragment ff = (mcontext.getSupportFragmentManager().findFragmentById(R.id.mainFrag));
        if (source != null && destination != null) {
            src = source;
            dest = destination;
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            getPolyline polyline = retrofit.create(getPolyline.class);

            polyline.getPolylineData(source.latitude + "," + source.longitude, destination.latitude + "," + destination.longitude)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                            JsonObject gson = new JsonParser().parse(response.body().toString()).getAsJsonObject();
                            try {
                                System.out.println("statusssss" + gson.toString());

                                if (gson.get("status").getAsString().equals("OVER_QUERY_LIMIT")) {
                                    count++;
//                                    if (count >= 10)
//                                        drawRoute(map, mcontext, source, destination, SessionSave.getSession("Lang", mcontext), 0);
//                                    else
                                    setUpPolyLine(map, mcontext, source, destination, secListLatLng, commonInterface);
                                } else {
                                    count = 0;
                                    JsonObject leg = gson.get("routes").getAsJsonArray().get(0).getAsJsonObject().get("legs").getAsJsonArray().get(0).getAsJsonObject();
                                    System.out.println("estimated_dd*" + source.latitude + "T" + destination.latitude + "___" + leg.get("duration").getAsJsonObject().get("value").getAsString());
                                    SessionSave.saveSession(source.latitude + "D" + destination.latitude, leg.get("distance").getAsJsonObject().get("value").getAsString(), mcontext);
                                    SessionSave.saveSession(source.latitude + "T" + destination.latitude, leg.get("duration").getAsJsonObject().get("value").getAsString(), mcontext);

                                    Single.just(parse(new JSONObject(gson.toString())))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new io.reactivex.functions.Consumer<List<List<HashMap<String, String>>>>() {
                                                @Override
                                                public void accept(List<List<HashMap<String, String>>> lists) throws Exception {

                                                    drawPolyline(lists, secListLatLng);
                                                }
                                            });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, Throwable t) {
//                            Toast.makeText(getActivity(), "Something went wrong hello", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        //else
//            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
    }

    //methods for route animation

    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception ignored) {
        }

        return routes;
    }

    void drawPolyline(List<List<HashMap<String, String>>> result, JSONArray secList) {

        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        listLatLng.clear();
        secListLatLng = secList;
//        if(startPolyline!=null)
//        startPolyline.setPoints(listLatLng);
        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            this.listLatLng.addAll(points);
        }
        if (lineOptions != null) {
            lineOptions.width(10);
            lineOptions.color(Color.GRAY);
            lineOptions.startCap(new SquareCap());
            lineOptions.endCap(new SquareCap());
            lineOptions.jointType(ROUND);
            startPolyline = mMap.addPolyline(lineOptions);

            PolylineOptions greyOptions = new PolylineOptions();
            greyOptions.width(10);
            greyOptions.color(Color.BLACK);
            greyOptions.startCap(new SquareCap());
            greyOptions.endCap(new SquareCap());
            greyOptions.jointType(ROUND);
            finalPolyline = mMap.addPolyline(greyOptions);

//            startPolyline.setPoints(listLatLng);

            animatePolyLine(1000);
        }
    }

    private void animatePolyLine(long duration) {

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                List<LatLng> latLngList = startPolyline.getPoints();
                int initialPointSize = latLngList.size();
                int animatedValue = (int) animator.getAnimatedValue();
                int newPoints = (animatedValue * listLatLng.size()) / 100;

                if (initialPointSize < newPoints) {
                    latLngList.addAll(listLatLng.subList(initialPointSize, newPoints));
                    startPolyline.setPoints(latLngList);
                }


            }
        });

        animator.addListener(polyLineAnimationListener);
        animator.start();

    }

    Animator.AnimatorListener polyLineAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            if (listLatLng.size() > 0)
                addMarker(listLatLng.get(listLatLng.size() - 1));
        }

        @Override
        public void onAnimationEnd(Animator animator) {

            List<LatLng> startLatLng = startPolyline.getPoints();
            List<LatLng> finalLatLng = finalPolyline.getPoints();

            finalLatLng.clear();

            if (commonInterface != null)
                commonInterface.getPolyline(startPolyline);
            System.out.println("polylat*" + src.latitude + "P" + dest.latitude+"__"+CommonUtils.toJson(startLatLng.toArray()));
            SessionSave.saveSession(src.latitude + "P" + dest.latitude, CommonUtils.toJson(startLatLng.toArray()), context);

            if (secListLatLng == null)
                finalLatLng.addAll(startLatLng);
            else
                finalLatLng.addAll(SessionSave.getLatLongFromSessionArray(secListLatLng));
//            startLatLng.clear();

            startPolyline.setPoints(startLatLng);
            finalPolyline.setPoints(finalLatLng);

            startPolyline.setZIndex(1);
            finalPolyline.setZIndex(2);

//            animator.start();
            //  animatePolyLine(1500);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {


        }
    };

    private void addMarker(LatLng destination) {

//        MarkerOptions options = new MarkerOptions();
//        options.position(destination);
//        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//        map.addMarker(options);

    }


    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng(((lat / 1E5)), ((lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}