package sdv.parsing;

import sdv.datastore.DataStore;
import org.apache.commons.csv.CSVRecord;
import sdv.datastructures.DataId;
import sdv.datastructures.FusedTrack;
import sdv.datastructures.SpatialData;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class FusedTrackInputParser extends InputParser {

    @Override
    protected String[] getMandatoryFields() {
        return new String[]{"id", "sensorId", "time", "lon", "lat", "alt"};
    }

    @Override
    public void addToStore(DataStore store, String source) {
        try {
            Iterable<CSVRecord> records = parseCSV(source);
            for (CSVRecord record : records) {
                FusedTrack track = toFusedTrack(record);
                if (track != null) {
                    store.addFusedTrack(track);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to parse CSV " + source);
        }
    }

    protected FusedTrack toFusedTrack(CSVRecord record) {
        try {
            FusedTrack fusedTrack = new FusedTrack();

            int id = Integer.parseInt(record.get("id"));
            int sensorId = Integer.parseInt(record.get("sensorId"));
            DataId dataId = new DataId(id, sensorId);
            fusedTrack.setId(dataId);

            long time = Long.parseLong(record.get("time"));
            double lon = Double.parseDouble(record.get("lon"));
            double lat = Double.parseDouble(record.get("lat"));
            double alt = Double.parseDouble(record.get("alt"));

            SpatialData spatialData = new SpatialData(time, lon, lat, alt);

            fusedTrack.setData(spatialData);

            Set<String> header = getHeader();
            header.removeAll(Arrays.asList(getMandatoryFields()));

            for (String field : header) {
                fusedTrack.setAdditionalData(field, record.get(field));
            }
            return fusedTrack;
        } catch (Exception e) {
            System.out.println("Failed to parse record " + record);
            return null;
        }
    }
}
