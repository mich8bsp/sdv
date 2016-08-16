package sdv.parsing;

import sdv.datastore.DataStore;
import org.apache.commons.csv.CSVRecord;
import sdv.datastructures.DataId;
import sdv.datastructures.TrackCorrelation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public class TrackCorrelationInputParser extends InputParser {

    @Override
    protected String[] getMandatoryFields() {
        return new String[]{"sourceId", "sourceSensorId", "correlatedId", "correlatedSensorId", "time"};
    }

    @Override
    public void addToStore(DataStore store, String source) {
        try {
            Iterable<CSVRecord> records = parseCSV(source);
            for (CSVRecord record : records) {
                TrackCorrelation corr = toCorrelation(record);
                if (corr != null) {
                    store.addCorrelation(corr);
                }
            }
            store.sortCorrelations();
        } catch (IOException e) {
            System.out.println("Failed to parse CSV " + source);
        }
    }

    protected TrackCorrelation toCorrelation(CSVRecord record) {
        try {
            TrackCorrelation correlation = new TrackCorrelation();

            int id = Integer.parseInt(record.get("sourceId"));
            int sensorId = Integer.parseInt(record.get("sourceSensorId"));
            DataId dataId = new DataId(id, sensorId);
            correlation.setSource(dataId);

            id = Integer.parseInt(record.get("correlatedId"));
            sensorId = Integer.parseInt(record.get("correlatedSensorId"));
            dataId = new DataId(id, sensorId);
            correlation.setCorrelated(dataId);

            long time = Long.parseLong(record.get("time"));

            correlation.setTimeOfCorrelation(time);

            Set<String> header = getHeader();
            header.removeAll(Arrays.asList(getMandatoryFields()));

            for (String field : header) {
                correlation.setAdditionalData(field, record.get(field));
            }
            return correlation;
        } catch (Exception e) {
            System.out.println("Failed to parse record " + record);
            return null;
        }
    }
}
