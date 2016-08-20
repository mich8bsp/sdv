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

    private ListMultimap<Integer, SensorReading> sensorReadings = LinkedListMultimap.create();
    private TreeMultimap<DataId, FusedTrack> fusedTracks = TreeMultimap.create();
    private List<TrackCorrelation> correlations = new LinkedList<>();

    private long startTime;
    private long endTime;


    public Collection<Collection<DataId>> getCorrelationsAtTime(long startTime, long endTime){
        List<TrackCorrelation> relevantCorrelations = correlations.stream().filter(corr -> corr.getTimeOfCorrelation()>=startTime && corr.getTimeOfCorrelation()<=endTime).collect(Collectors.toList());
        System.out.println("Checking relevant correlations for " +startTime + "," + endTime + " there were " + relevantCorrelations.size());
        //each track can only be correlated to one track
        Map<DataId, TrackCorrelation> updatedCorrelations = new HashMap<>();
        for(TrackCorrelation correlation : relevantCorrelations){
            if(correlation.getCorrelated().getId()==DataId.INVALID_ID || correlation.getCorrelated().getSensorId()==DataId.INVALID_ID){
                //this means it's no longer correlated
                updatedCorrelations.remove(correlation.getSource());
            }else {
                TrackCorrelation existing = updatedCorrelations.get(correlation.getSource());
                if (existing == null || existing.getTimeOfCorrelation() < correlation.getTimeOfCorrelation()) {
                    updatedCorrelations.put(correlation.getSource(), correlation);
                }
            }
        }

        HashMultimap<Integer, DataId> groupToTracksInCorrelation = HashMultimap.create();
        Map<DataId, Integer> idToGroup = new HashMap<>();
        int counter = 1;
        for(TrackCorrelation correlation : updatedCorrelations.values()){
            if(!groupToTracksInCorrelation.values().contains(correlation.getCorrelated())){
                groupToTracksInCorrelation.put(counter, correlation.getCorrelated());
                groupToTracksInCorrelation.put(counter, correlation.getSource());
                idToGroup.put(correlation.getSource(), counter);
                idToGroup.put(correlation.getCorrelated(), counter);
                counter++;
            }else{
                int groupIdToAddTo = idToGroup.get(correlation.getCorrelated());
                groupToTracksInCorrelation.put(groupIdToAddTo, correlation.getSource());
                idToGroup.put(correlation.getSource(), groupIdToAddTo);
            }
        }
        return groupToTracksInCorrelation.asMap().values();
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
}
