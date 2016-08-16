package sdv.datastructures;

import io.vertx.core.json.JsonObject;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class SensorReading extends AbstractData{

    private DataId readingId;
    private SpatialData data;

    public DataId getReadingId() {
        return readingId;
    }

    public void setReadingId(DataId readingId) {
        this.readingId = readingId;
    }

    public SpatialData getData() {
        return data;
    }

    public void setData(SpatialData data) {
        this.data = data;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.mergeIn(readingId.toJson());
        json.mergeIn(data.toJson());
        json.mergeIn(super.toJson());
        return json;
    }
}
