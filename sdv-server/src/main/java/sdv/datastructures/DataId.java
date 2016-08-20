package sdv.datastructures;

import io.vertx.core.json.JsonObject;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class DataId implements Comparable {

    private int id;

    private int sensorId;

    public DataId(int id, int sensorId) {
        this.id = id;
        this.sensorId = sensorId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public int compareTo(Object o) {
        DataId other = (DataId) o;
        if (sensorId != other.getSensorId()) {
            return Double.compare(id, other.getId());
        } else {
            return Double.compare(sensorId, other.getSensorId());
        }
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("id", id);
        json.put("sensorId", sensorId);
        return json;
    }

    @Override
    public String toString() {
        return "DataId{" +
                "id=" + id +
                ", sensorId=" + sensorId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataId dataId = (DataId) o;

        if (id != dataId.id) return false;
        return sensorId == dataId.sensorId;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + sensorId;
        return result;
    }
}
