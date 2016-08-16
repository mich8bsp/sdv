package sdv.datastructures;

import io.vertx.core.json.JsonObject;

import java.util.*;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class FusedTrack extends AbstractData implements Comparable{

    private DataId id;
    private SpatialData data;

    public static Optional<FusedTrack> aggregateUpdates(Collection<FusedTrack> fusedTracks, long time) {
        return fusedTracks.stream().filter(t -> t.getData().getTime()<=time).max((tr1, tr2)->Long.compare(tr1.getData().getTime(), tr2.getData().getTime()));
    }

    public DataId getId() {
        return id;
    }

    public void setId(DataId id) {
        this.id = id;
    }

    public SpatialData getData() {
        return data;
    }

    public void setData(SpatialData data) {
        this.data = data;
    }


    @Override
    public int compareTo(Object o) {
        FusedTrack other = (FusedTrack)o;
        int comp = id.compareTo(other.getId());
        if(comp!=0){
            return comp;
        }else {
            return Double.compare(data.getTime(), other.getData().getTime());
        }
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();
        json.mergeIn(id.toJson());
        json.mergeIn(data.toJson());
        json.mergeIn(super.toJson());
        return json;
    }
}
