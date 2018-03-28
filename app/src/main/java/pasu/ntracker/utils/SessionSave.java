package pasu.ntracker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This common class to store the require data by using SharedPreferences.
 */
public class SessionSave {
    public static void saveSession(String key, String value, Context context) {
        if (context != null) {
            Editor editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit();

            editor.putString(key, value);
            editor.commit();
        }
        return;
    }

    public static void saveSession(String key, float value, Context context) {
        if (context != null) {
            Editor editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit();

            editor.putFloat(key, value);
            editor.commit();
        }
        return;
    }

    public static String getSession(String key, Context context) {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE);

            return prefs.getString(key, "");
        }
        return "";
    }

    public static float getSession(String key, Context context, float f) {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE);

            return prefs.getFloat(key, f);
        }
        return f;
    }

    public static void clearSession(Context context) {
        Editor editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }

    public static void setDistance(double distance, Context con) {
        if (con != null) {
            Editor editor = con.getSharedPreferences("DIS", con.MODE_PRIVATE).edit();
            editor.putFloat("DISTANCE", (float) distance);
            editor.commit();
        }
    }

    public static void setGoogleDistance(double distance, Context con) {
        if (con != null) {
            Editor editor = con.getSharedPreferences("GDIS", con.MODE_PRIVATE).edit();
            editor.putFloat("GDISTANCE", (float) distance);
            editor.commit();
        }
    }

    public static float getGoogleDistance(Context con) {
        DecimalFormat df = new DecimalFormat(".###");
//		SharedPreferences sharedPreferences=con.getSharedPreferences("DIS", con.MODE_PRIVATE);
        return Float.parseFloat((getGoogleDistanceString(con)));
    }

    //	public static float getDistance(Context con)
//	{
//		SharedPreferences sharedPreferences=con.getSharedPreferences("DIS", con.MODE_PRIVATE);
//		return sharedPreferences.getFloat("DISTANCE", 0);
//
//	}
    public static float getDistance(Context con) {
        DecimalFormat df = new DecimalFormat(".###");
//		SharedPreferences sharedPreferences=con.getSharedPreferences("DIS", con.MODE_PRIVATE);
        return Float.parseFloat((getDistanceString(con)));
    }

    public static String getGoogleDistanceString(Context con) {
        SharedPreferences sharedPreferences = con.getSharedPreferences("GDIS", con.MODE_PRIVATE);
        return String.format(Locale.UK, "%.2f", sharedPreferences.getFloat("GDISTANCE", 0));
    }

    public static String getDistanceString(Context con) {
        SharedPreferences sharedPreferences = con.getSharedPreferences("DIS", con.MODE_PRIVATE);
        return String.format(Locale.UK, "%.2f", sharedPreferences.getFloat("DISTANCE", 0));
    }

    public static void setWaitingTime(Long time, Context con) {
        Editor editor = con.getSharedPreferences("long", con.MODE_PRIVATE).edit();
        editor.putLong("LONG", time);
        editor.commit();
    }

    public static void saveSession(String key, boolean value, Context context) {
        Editor editor = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getSession(String key, Context context, boolean a) {
        SharedPreferences prefs = context.getSharedPreferences("KEY", Activity.MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }

    public static long getWaitingTime(Context con) {
        SharedPreferences sharedPreferences = con.getSharedPreferences("long", con.MODE_PRIVATE);
        return sharedPreferences.getLong("LONG", 0);

    }


    public static void saveGoogleWaypoints(LatLng start, LatLng dest, String source,
                                           double dist, String error,
                                           double speedValue, long idle,
                                           String estimatedtime, long timeTravelled,
                                           Context mContext) {


        SharedPreferences prefs = mContext.getSharedPreferences("wayPoints", 0);

        Editor editor = prefs.edit();
        if (start != null) {
//            WayPointsData[] wayPointsData = ReadGoogleWaypoints(mContext);
//            ArrayList<WayPointsData> arrayList = new ArrayList<WayPointsData>(Arrays.asList(wayPointsData));
//            WayPointsData data = new WayPointsData();
//            data.setDist(dist);
//            data.setError(error);
////            data.setLatlng(start+"");
//            data.setType(source);
//            data.setPickuplat(start.latitude);
//            data.setDroplat(dest.latitude);
//            data.setPickuplng(start.longitude);
//            data.setDroplng(dest.longitude);
//            arrayList.add(data);
//            WayPointsData[] wayPointsData1=arrayList.toArray(new WayPointsData[arrayList.size()]);
//            Systems.out.println("waydataaaaa"+wayPointsData1+"__"+wayPointsData1.toString());
//            String s = new Gson().toJson(wayPointsData1.toString());
//            Systems.out.println("saving way points" + s);


            JSONArray jsonArray = ReadGoogleWaypoints(mContext);

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("dist", dist);
                jsonObject.put("error", error);
                jsonObject.put("source", source);
                jsonObject.put("pickuplat", start.latitude);
                jsonObject.put("pickuplng", start.longitude);
                jsonObject.put("droplat", dest.latitude);
                jsonObject.put("droplng", dest.longitude);
                jsonObject.put("time", new Date().getTime());

                jsonObject.put("speedValue", speedValue);
                jsonObject.put("idle", idle);
                jsonObject.put("estimatedtime", estimatedtime);
                jsonObject.put("timeTravelled", timeTravelled);
                jsonArray.put(jsonObject);
                Systems.out.println("waypoints storing" + jsonArray.toString());
                editor.putString("wayPoints", jsonArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else
            editor.clear();
        editor.commit();
//        try {
//            JSONObject ss = new JSONObject();
//            ss.put("way", ReadGoogleWaypoints(mContext));
//            ss.put("hai", "sdfds");
//            Systems.out.println("haiiii" + ss);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

//		return editor.commit();
    }

    public static JSONArray ReadGoogleWaypoints(Context mContext) {

        JSONArray jsonArray = new JSONArray();
        try {
            SharedPreferences prefs = mContext.getSharedPreferences("wayPoints", 0);
            jsonArray = new JSONArray(prefs.getString("wayPoints", "[]"));
            Systems.out.println("waypoints reading" + jsonArray.toString());
//            JSONObject jsonObject = new JSONObject(s);
//            Systems.out.println("readingway" + s + "___" + jsonObject);
//            wayPointsData = new Gson().fromJson(jsonObject.toString(), WayPointsData[].class);
//            jsonArray2 = jo.getJSONArray("values");
//            for (int i = 0; i < jsonArray2.length(); i++) {
//                JSONObject jsonObject = jsonArray2.getJSONObject(i);
//                Systems.out.println("haiiiiii" + jsonObject.toString());
//                if (jsonObject != null) {
//                    WayPointsData data = new Gson().fromJson(jsonObject.toString(), WayPointsData.class);
//                    jsonArray.put(data);
//                }
//            }
//			for (int i = 0; i < jsonArray2.length(); i++) {
//				//Log.d("your JSON Array", jsonArray2.getInt(i)+"");
//			}

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonArray;
    }


    public static void saveWaypoints(LatLng start, LatLng dest, String source, double dist, String error, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("localwayPoints", 0);

        Editor editor = prefs.edit();
        if (start != null) {

            JSONArray jsonArray = ReadWaypoints(mContext);

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("dist", dist);
                jsonObject.put("error", error);
                jsonObject.put("source", source);
                jsonObject.put("pickuplat", start.latitude);
                jsonObject.put("pickuplng", start.longitude);
                jsonObject.put("droplat", dest.latitude);
                jsonObject.put("droplng", dest.longitude);

                jsonObject.put("trip_id", SessionSave.getSession("trip_id", mContext));
                jsonObject.put("time", DateFormat.getTimeInstance().format(new Date()));
                jsonArray.put(jsonObject);
                Systems.out.println("waypoints storing" + jsonArray.toString());
                editor.putString("localwayPoints", jsonArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else
            editor.clear();
        editor.commit();
    }

    public static JSONArray ReadWaypoints(Context mContext) {

        JSONArray jsonArray = new JSONArray();
        try {
            SharedPreferences prefs = mContext.getSharedPreferences("localwayPoints", 0);
            jsonArray = new JSONArray(prefs.getString("localwayPoints", "[]"));
            Systems.out.println("waypoints reading" + jsonArray.toString());
            for (int i = 0; i < ReadGoogleWaypoints(mContext).length(); i++) {
                JSONObject jj = ReadGoogleWaypoints(mContext).getJSONObject(i);
                jsonArray.put(jj);
            }

            String savingTripDetail = jsonArray.toString();
            SessionSave.saveSession(SessionSave.getSession("trip_id", mContext) + "data", savingTripDetail, mContext);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonArray;
    }


    public static void saveLastLng(LatLng ll, Context con) {
        if (ll != null && ll.latitude != 0.0) {
            double lastLat = ll.latitude;
            double lastLng = ll.longitude;
            Editor editor = con.getSharedPreferences("nlastlong", con.MODE_PRIVATE).edit();
            editor.putString("nLat", String.valueOf(lastLat));
            editor.putString("nLng", String.valueOf(lastLng));
            editor.commit();
        }
    }

    public static LatLng getLastLng(Context con) {

        double nLat = 0.0;
        double nLng = 0.0;
        SharedPreferences preferences = con.getSharedPreferences("nlastlong", con.MODE_PRIVATE);
        if (!preferences.getString("nLat", "").equals("")) {
            Systems.out.println("LassstString" + SessionSave.getSession("nLat", con));
            nLat = Double.parseDouble(preferences.getString("nLat", ""));
            nLng = Double.parseDouble(preferences.getString("nLng", ""));
        }
        Systems.out.println("getLastLat" + preferences.getString("nLat", ""));
        return new LatLng(nLat, nLng);
    }

    public static List<LatLng> getLatLongFromSessionArray(JSONArray jsonArray) {
        List<LatLng> ll = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jj = null;
                try {
                    jj = jsonArray.getJSONObject(i);
                    ll.add(new LatLng(jj.getDouble("pickuplat"), jj.getDouble("pickuplng")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        return ll;
    }
}
