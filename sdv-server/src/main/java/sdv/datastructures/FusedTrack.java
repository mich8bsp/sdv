package sdv.datastructures;

import io.vertx.core.json.JsonObject;

import java.util.*;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class FusedTrack extends AbstractData implements Comparable{

    private DataId id;
    private SpatialData data;

    public FusedTrack(){}
    public FusedTrack(DataId key) {
        this.id=key;
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
        if(data!=null) {
            json.mergeIn(data.toJson());
        }
        json.mergeIn(super.toJson());
        return json;
    }
}
