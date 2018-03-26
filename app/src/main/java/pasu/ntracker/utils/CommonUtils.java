package pasu.ntracker.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import pasu.ntracker.DriverMapActivity;
import pasu.ntracker.data.Tracker;

/**
 * Created by Admin on 21-03-2018.
 */

public class CommonUtils {
    private static AlertDialog dialog;

    public static String getDate(long timeStamp) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy h:mm a");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
    }

    public static <T> String toJson(T data) {
        if (data != null)
            return new Gson().toJson(data);
        return "";
    }

    public static <T> T fromJson(String currentTrackInfo, Class<T> t) {
        if (currentTrackInfo != null)
            return new Gson().fromJson(currentTrackInfo, t);
        return null;
    }

    public static String getTimeHrs(long millis) {
        String formatted = String.format(
                "%02d:%02d:%02d",
                Math.abs(TimeUnit.MILLISECONDS.toHours(millis)),
                Math.abs(TimeUnit.MILLISECONDS.toMinutes(millis)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))),
                Math.abs(TimeUnit.MILLISECONDS.toSeconds(millis)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
        return formatted;
    }

    public static String getTimeHrsMins(long millis) {

        String formatted = String.format(
                "%02d:%02d",
                Math.abs(TimeUnit.MILLISECONDS.toHours(millis)),
                Math.abs(TimeUnit.MILLISECONDS.toMinutes(millis)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))));
        return formatted;
    }

    public static void alertDialog(String message, Context context) {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        dialog = builder
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
