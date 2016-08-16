package sdv.datastructures;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mich8bsp on 16-Aug-16.
 */
public class AbstractData {
    protected Map<String, String> additionalData = new HashMap<>();

    public void setAdditionalData(String key, String value) {
        additionalData.put(key, value);
    }

    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, String> entry : additionalData.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        return json;
    }
}

