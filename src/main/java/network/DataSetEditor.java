package network;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import frontend.InvalidFeaturesException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DataSetEditor {
    public void addColumn(int index, String header, double defaultValue, String filePath) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> csvBody = reader.readAll();

            boolean titleWritten = false;

            try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
                for(String[] entries : csvBody) {

                    List<String> entryList = new ArrayList<>(Arrays.asList(entries));
                    if(titleWritten) {
                        entryList.add(index, String.valueOf(defaultValue));
                    } else {
                        entryList.add(index, header);
                        titleWritten = true;
                    }
                    entries = entryList.toArray(entries);
                    writer.writeNext(entries);
                }
            }

        }
    }

    public void translateColumns(String filePath) throws IOException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> csvBody = reader.readAll();

            boolean headersRead = false;

            try (CSVWriter writer = new CSVWriter(new FileWriter(filePath), ',', CSVWriter.NO_QUOTE_CHARACTER)) {
                for(String[] entries : csvBody) {
                    if(!headersRead) {
                        headersRead = true;
                    } else {

                        for (int i = 0; i < entries.length; i++) {
                            if (entries[i].equalsIgnoreCase("yes")) {
                                entries[i] = "1.0";
                            } else if (entries[i].equalsIgnoreCase("no")) {
                                entries[i] = "0.0";
                            } else {
                                System.out.println("Unrecognisable element: " + entries[i]);
                            }
                        }
                    }

                    writer.writeNext(entries);
                }
            }

        }

    }

    public void addEntry(List<String> elements, String filePath) throws IOException {
        System.out.println("Adding element");
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, true), ',',CSVWriter.NO_QUOTE_CHARACTER)) {
            String[] entries = new String[elements.size()];
            entries = elements.toArray(entries);
            writer.writeNext(entries);
        }
    }

    public static void main(String[] args) {
        DataSetEditor editor = new DataSetEditor();
        try {
            editor.translateColumns("trip_edit.csv");
            editor.addEntry(Collections.singletonList("1.0"), "trip_edit.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
