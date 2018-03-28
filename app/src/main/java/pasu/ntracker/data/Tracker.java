package pasu.ntracker.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Admin on 18-03-2018.
 */

public class Tracker {
    public String getVechicleID() {
        return vechicleID;
    }

    public void setVechicleID(String vechicleID) {
        this.vechicleID = vechicleID;
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    public long getTimeEnded() {
        return timeEnded;
    }

    public void setTimeEnded(long timeEnded) {
        this.timeEnded = timeEnded;
    }

    private String vechicleID;

    public String getTrackID() {
        return trackID;
    }

    public void setTrackID(String trackID) {
        this.trackID = trackID;
    }

    private String trackID;
    private long timeStarted = 0L;
    private long timeEnded = 0L;

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

    public String getPickAddress() {
        return pickAddress;
    }

    public void setPickAddress(String pickAddress) {
        this.pickAddress = pickAddress;
    }

    public String getDropAddress() {
        return dropAddress;
    }

    public void setDropAddress(String dropAddress) {
        this.dropAddress = dropAddress;
    }

    private String pickAddress, dropAddress;
    private double pickuplat, pickuplng, droplat, droplng;

    public LatLng getPickuplatlng() {
        return new LatLng(getPickuplat(), getPickuplng());
    }

    public LatLng getDroplatlng() {
        return new LatLng(getDroplat(), getDroplng());
    }
}
