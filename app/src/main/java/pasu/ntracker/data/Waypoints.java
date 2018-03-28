package pasu.ntracker.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Admin on 18-03-2018.
 */

public class Waypoints {
    public double getPickuplat() {
        return pickuplat;
    }

    public void setPickuplat(double pickuplat) {
        this.pickuplat = pickuplat;
    }

    public double getPickuplng() {
        return pickuplng;
    }

    public void setPickuplng(double pickuplng) {
        this.pickuplng = pickuplng;
    }

    public double getDroplat() {
        return droplat;
    }

    public void setDroplat(double droplat) {
        this.droplat = droplat;
    }

    public double getDroplng() {
        return droplng;
    }

    public void setDroplng(double droplng) {
        this.droplng = droplng;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LatLng getPickupLatlng() {
        return new LatLng(pickuplat, pickuplng);
    }

    public LatLng getDropLatlng() {
        return new LatLng(pickuplat, pickuplng);
    }

    private double pickuplat, pickuplng, droplat, droplng, dist;
    private long time;
    private String error, source;
    private double speedValue;
    private long idle;
    private String estimatedtime;
    private long timeTravelled;

    public double getSpeedValue() {
        return speedValue;
    }

    public void setSpeedValue(double speedValue) {
        this.speedValue = speedValue;
    }

    public long getIdle() {
        return idle;
    }

    public void setIdle(long idle) {
        this.idle = idle;
    }

    public String getEstimatedtime() {
        return estimatedtime;
    }

    public void setEstimatedtime(String estimatedtime) {
        this.estimatedtime = estimatedtime;
    }

    public long getTimeTravelled() {
        return timeTravelled;
    }

    public void setTimeTravelled(long timeTravelled) {
        this.timeTravelled = timeTravelled;
    }
}
