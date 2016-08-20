package sdv.datastructures;

import io.vertx.core.json.JsonObject;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class SpatialData {
    private long time;
    private double lon;
    private double lat;
    private double alt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpatialData that = (SpatialData) o;

        if (Double.compare(that.lon, lon) != 0) return false;
        if (Double.compare(that.lat, lat) != 0) return false;
        return Double.compare(that.alt, alt) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lon);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lat);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(alt);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public SpatialData(long time, double lon, double lat, double alt) {
        this.time = time;
        this.lon = lon;
        this.lat = lat;
        this.alt = alt;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("time", time);
        json.put("lon", lon);
        json.put("lat", lat);
        json.put("alt", alt);
        return json;
    }

    @Override
    public String toString() {
        return "SpatialData{" +
                "time=" + time +
                ", lon=" + lon +
                ", lat=" + lat +
                ", alt=" + alt +
                '}';
    }
}
