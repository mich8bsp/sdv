package sdv.datastructures;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mich8bsp on 18-Aug-16.
 */
public class FusedTrackState extends FusedTrack{
    List<SpatialData> trackTrace = new LinkedList<>();

    public FusedTrackState(DataId key) {
        super(key);
    }

    public FusedTrackState init(List<FusedTrack> updates){
        if(updates.size()>0) {
            trackTrace = updates.stream().map(FusedTrack::getData).distinct().sorted((d1,d2)->Long.compare(d1.getTime(), d2.getTime())).collect(Collectors.toList());
            SpatialData currPosition = trackTrace.get(trackTrace.size()-1);
            setData(currPosition);
        }
        return this;
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();
        JsonArray trace = new JsonArray();
        for(SpatialData update : trackTrace){
            trace.add(update.toJson());
        }
        json.put("trackTrace", trace);
        json.mergeIn(super.toJson());
        return json;
    }
}
