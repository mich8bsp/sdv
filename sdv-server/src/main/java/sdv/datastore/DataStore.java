package sdv.datastore;

import com.google.common.collect.*;
import sdv.datastructures.DataId;
import sdv.datastructures.FusedTrack;
import sdv.datastructures.SensorReading;
import sdv.datastructures.TrackCorrelation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class DataStore {

    private ListMultimap<Integer, SensorReading> sensorReadings = LinkedListMultimap.create();
    private TreeMultimap<DataId, FusedTrack> fusedTracks = TreeMultimap.create();
    private List<TrackCorrelation> correlations = new LinkedList<>();


    public Collection<TrackCorrelation> getCorrelationsAtTime(long time){
        List<TrackCorrelation> relevantCorrelations = correlations.stream().filter(corr -> corr.getTimeOfCorrelation()<=time).collect(Collectors.toList());
        //each track can only be correlated to one track
        Map<DataId, TrackCorrelation> updatedCorrelations = new HashMap<>();
        for(TrackCorrelation correlation : relevantCorrelations){
            TrackCorrelation existing = updatedCorrelations.get(correlation.getSource());
            if(existing==null || existing.getTimeOfCorrelation()<correlation.getTimeOfCorrelation()){
                updatedCorrelations.put(correlation.getSource(), correlation);
            }
        }
        return updatedCorrelations.values();
    }

    public Collection<SensorReading> getReadingsAtTime(long time){
        return sensorReadings.values().stream().filter(r -> r.getData().getTime()<=time).collect(Collectors.toList());
    }

    public Collection<Optional<FusedTrack>> getTrackAtTime(long time){
        return fusedTracks.asMap().values().stream().map(updates -> FusedTrack.aggregateUpdates(updates, time)).collect(Collectors.toList());
    }

    public Collection<FusedTrack> getTrackUpdatesAtTime(long time, DataId id){
        return fusedTracks.asMap().get(id).stream().filter(track -> track.getData().getTime() <= time).collect(Collectors.toList());
    }

    public void addSensorReading(SensorReading reading) {
        sensorReadings.put(reading.getReadingId().getSensorId(), reading);
    }

    public void addFusedTrack(FusedTrack track) {
        fusedTracks.put(track.getId(), track);
    }

    public void addCorrelation(TrackCorrelation corr) {
        correlations.add(corr);
    }

    public void sortCorrelations() {
        correlations.sort((c1, c2)->Long.compare(c1.getTimeOfCorrelation(), c2.getTimeOfCorrelation()));
    }
}
