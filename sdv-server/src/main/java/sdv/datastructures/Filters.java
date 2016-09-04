package sdv.datastructures;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import sdv.datastore.DataStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mich8bsp on 01-Sep-16.
 */
public class Filters {
    private int sensorId;
    private boolean isShown = true;
    private boolean readingsShown = true;
    private Map<Integer, Boolean> tracksShown = new HashMap<>();

    public Filters(int sensorId, DataStore store) {
        this.sensorId = sensorId;
        List<Integer> trackIds = store.getAllTracksBySensor(sensorId);
        trackIds.forEach(id -> tracksShown.put(id, true));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("sensorId", sensorId);
        json.put("isShown", isShown);
        json.put("readingsShown", readingsShown);
        JsonArray tracksShownJson = new JsonArray();
        tracksShown.entrySet().forEach(entry -> {
            JsonObject obj = new JsonObject();
            obj.put("trackId", entry.getKey());
            obj.put("isTrackShown", entry.getValue());
            tracksShownJson.add(obj);
        });
        json.put("tracksShown", tracksShownJson);
        return json;
    }
}
