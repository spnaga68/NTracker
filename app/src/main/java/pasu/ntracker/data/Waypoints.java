package pasu.ntracker.data;

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

    private double pickuplat,pickuplng,droplat,droplng,dist;
    private long time;
    private String error,source;
}
