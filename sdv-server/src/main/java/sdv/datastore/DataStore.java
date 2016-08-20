package sdv.datastore;

import com.google.common.collect.*;
import sdv.Server;
import sdv.datastructures.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class DataStore {

    private Map<DataId, String> cesiumIdsOfTraces = new HashMap<>();
    private ListMultimap<Integer, SensorReading> sensorReadings = LinkedListMultimap.create();
    private TreeMultimap<DataId, FusedTrack> fusedTracks = TreeMultimap.create();
    private List<TrackCorrelation> correlations = new LinkedList<>();

    private long startTime;
    private long endTime;


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

    public Collection<SensorReading> getReadingsAtTime(long startTime, long endTime){
        return sensorReadings.values().stream().filter(r -> r.getData().getTime()>=startTime && r.getData().getTime()<endTime).collect(Collectors.toList());
    }

    public Map<DataId, Collection<FusedTrack>> getTrackUpdatesAtTime(long startTime, long endTime){
        LinkedListMultimap<DataId, FusedTrack> copyOfMap = LinkedListMultimap.create(fusedTracks.size());
        copyOfMap.putAll(fusedTracks);
        copyOfMap.asMap().values().forEach(x -> x.removeIf(update -> update.getData().getTime()<startTime || update.getData().getTime()>endTime));
        return copyOfMap.asMap();
    }

    public Collection<FusedTrackState> getTrackStatesAtTime(long currentTime) {
        List<FusedTrackState> states = fusedTracks.asMap().entrySet().stream().map(updates -> getFusedTrackState(updates, startTime, currentTime)).collect(Collectors.toList());
        states.forEach(state -> {
            String cesiumId = cesiumIdsOfTraces.get(state.getId());
            System.out.println("checking cesium id for " + state.getId() + " and it was " + cesiumId);
            if(cesiumId!=null){
                state.setCesiumId(cesiumId);
            }
        });
        return states;
    }

    private FusedTrackState getFusedTrackState(Map.Entry<DataId, Collection<FusedTrack>> updates, long firstTime, long currentTime) {
        List<FusedTrack> relevantUpdates = updates.getValue().stream().filter(update -> update.getData().getTime()>=firstTime &&
                    update.getData().getTime()< currentTime).collect(Collectors.toList());
        return new FusedTrackState(updates.getKey()).init(relevantUpdates);
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


    public long getStartTime() {
        Optional<Long> firstReading = sensorReadings.values().stream().map(r -> r.getData().getTime()).min(Long::compare);
        Optional<Long> firstTrack = fusedTracks.values().stream().map(t -> t.getData().getTime()).min(Long::compare);
        startTime = firstReading.orElse(firstTrack.orElse(0L));
        return startTime;
    }

    public long getEndTime() {
        Optional<Long> lastReading = sensorReadings.values().stream().map(r -> r.getData().getTime()).max(Long::compare);
        Optional<Long> lastTrack = fusedTracks.values().stream().map(t -> t.getData().getTime()).max(Long::compare);
        endTime = lastReading.orElse(lastTrack.orElse(Long.MAX_VALUE));
        return endTime;
    }

    public void updateReadingWithCesiumId(Server.UpdateKey key, String cesiumId) {
        sensorReadings.get(key.getId().getSensorId()).stream().filter(r -> r.getReadingId().getId() == key.getId().getId()).findAny().ifPresent(reading -> reading.setCesiumId(cesiumId));
    }

    public void updateTrackWithCesiumId(Server.UpdateKey key, String cesiumId) {
        fusedTracks.get(key.getId()).stream().filter(t -> t.getData().getTime()==key.getTime()).findAny().ifPresent(track -> track.setCesiumId(cesiumId));
    }


    public void updateTrackStateWithCesiumId(Server.UpdateKey updateKey, String cesiumId) {
        if(cesiumId.equals("removed")){
            System.out.println("removing " + updateKey.getId());
            cesiumIdsOfTraces.remove(updateKey.getId());
        }else {
            System.out.println("putting " + updateKey.getId() + cesiumId);

            cesiumIdsOfTraces.put(updateKey.getId(), cesiumId);
        }
    }
}
