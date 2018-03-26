package pasu.ntracker.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import pasu.ntracker.R;

/**
 * Created by Admin on 24-03-2018.
 */
public class CustomMarker {

    public static Bitmap getMarkerBitmapFromView(String text, Context c) {

        View customMarkerView = ((LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
//        TextView time_to_reach = (TextView) customMarkerView.findViewById(R.id.time_to_reach);
//        time_to_reach.setText(time + "\n" + "MIN");
//
//        if(time.equals("0"))
//            time_to_reach.setVisibility(View.GONE);
//        else
//            time_to_reach.setVisibility(View.VISIBLE);
        ((TextView) customMarkerView.findViewById(R.id.location_name)).setText(text);

        //  markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
    public static BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
//    public static Bitmap getMarkerBitmapFromViewForDrop(String text, Context c) {
//
//        View customMarkerView = ((LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
//        customMarkerView.findViewById(R.id.time_to_reach).setVisibility(View.GONE);
//        ((TextView) customMarkerView.findViewById(R.id.location_name)).setText(text);
//
//        customMarkerView.findViewById(R.id.pickup_icon).setVisibility(View.GONE);
//        customMarkerView.findViewById(R.id.drop_icon).setVisibility(View.VISIBLE);
//
//        //  markerImageView.setImageResource(resId);
//        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
//        customMarkerView.buildDrawingCache();
//        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
//                Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(returnedBitmap);
//        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
//        Drawable drawable = customMarkerView.getBackground();
//        if (drawable != null)
//            drawable.draw(canvas);
//        customMarkerView.draw(canvas);
//        return returnedBitmap;
//    }
}