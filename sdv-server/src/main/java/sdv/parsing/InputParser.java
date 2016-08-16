package sdv.parsing;

import sdv.datastore.DataStore;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.List;
import java.util.Set;

/**
 * Created by mich8bsp on 15-Aug-16.
 */
public abstract class InputParser {

    protected abstract String[] getMandatoryFields();

    private Set<String> header;

    protected List<CSVRecord> parseCSV(String source) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(source);
        Reader in = new InputStreamReader(is);

        CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        CSVParser parser = new CSVParser(in, format);
        header = parser.getHeaderMap().keySet();
        return parser.getRecords();
    }

    public abstract void addToStore(DataStore store, String source);

    public Set<String> getHeader() {
        return header;
    }

}
