package sdv.datastructures;

import io.vertx.core.json.JsonObject;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class TrackCorrelation extends AbstractData {

    private DataId source;
    private DataId correlated;
    private long timeOfCorrelation;

    public DataId getSource() {
        return source;
    }

    public void setSource(DataId source) {
        this.source = source;
    }

    public DataId getCorrelated() {
        return correlated;
    }

    public void setCorrelated(DataId correlated) {
        this.correlated = correlated;
    }

    public long getTimeOfCorrelation() {
        return timeOfCorrelation;
    }

    public void setTimeOfCorrelation(long timeOfCorrelation) {
        this.timeOfCorrelation = timeOfCorrelation;
    }


    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("sourceId", source.getId());
        json.put("sourceSensorId", source.getSensorId());
        json.put("correlatedId", correlated.getId());
        json.put("correlatedSensorId", correlated.getSensorId());
        json.put("time", timeOfCorrelation);
        json.mergeIn(super.toJson());
        return json;
    }
}
