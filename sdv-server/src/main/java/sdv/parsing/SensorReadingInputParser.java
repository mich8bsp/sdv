package sdv.parsing;

import sdv.datastore.DataStore;
import org.apache.commons.csv.CSVRecord;
import sdv.datastructures.DataId;
import sdv.datastructures.SensorReading;
import sdv.datastructures.SpatialData;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class SensorReadingInputParser extends InputParser {

    @Override
    protected String[] getMandatoryFields() {
        return new String[]{"id", "sensorId", "time", "lon","lat","alt"};
    }

    @Override
    public void addToStore(DataStore store, String source) {
        try {
            Iterable<CSVRecord> records = parseCSV(source);
            for (CSVRecord record : records) {
                SensorReading reading = toSensorReading(record);
                if (reading != null) {
                    store.addSensorReading(reading);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to parse CSV " + source);
        }
    }

    protected SensorReading toSensorReading(CSVRecord record) {
        try {
            SensorReading reading = new SensorReading();

            int id = Integer.parseInt(record.get("id"));
            int sensorId = Integer.parseInt(record.get("sensorId"));
            DataId dataId = new DataId(id, sensorId);
            reading.setReadingId(dataId);

            long time = Long.parseLong(record.get("time"));
            double lon = Double.parseDouble(record.get("lon"));
            double lat = Double.parseDouble(record.get("lat"));
            double alt = Double.parseDouble(record.get("alt"));

            SpatialData spatialData = new SpatialData(time, lon, lat, alt);

            reading.setData(spatialData);

            Set<String> header = getHeader();
            header.removeAll(Arrays.asList(getMandatoryFields()));

            for(String field : header){
                reading.setAdditionalData(field, record.get(field));
            }
            return reading;
        } catch (Exception e) {
            System.out.println("Failed to parse record " + record);
            return null;
        }
    }

}
