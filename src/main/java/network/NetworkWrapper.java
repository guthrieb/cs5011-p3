package network;

import com.opencsv.CSVWriter;
import org.encog.ml.data.MLData;
import org.encog.ml.data.specific.CSVNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NetworkWrapper {
    public static final String FEATURES_JSON_KEY = "features";
    public static final String OUTPUTS_JSON_KEY = "outputs";
    public static final String DATASET_JSON_KEY = "dataset";
    public static final String HEADERS_JSON_POSTFIX = "_headers.json";
    private String dataSetFilePath;
    private List<String> features;
    private List<String> outputs;
    private BasicNetwork network;

    public NetworkWrapper(String dataSetFilePath, List<String> features, List<String> outputs) {
        this.dataSetFilePath = dataSetFilePath;
        this.features = features;
        this.outputs = outputs;
    }

    private NetworkWrapper() {
    }

    public static NetworkWrapper loadNetworkFromFile(String filePath) throws IOException, InvalidNetworkFile {
        BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File(filePath));

        NetworkWrapper wrapper = new NetworkWrapper();
        wrapper.network = network;
        wrapper.readOutputFile(filePath);
        return wrapper;
    }

    public void retrain() {
        NetworkTrainer trainer = new NetworkTrainer();
        int featureSize = features.size();
        int outputSize = outputs.size();
        CSVNeuralDataSet trainingSet = new CSVNeuralDataSet(dataSetFilePath, featureSize, outputSize, true, CSVFormat.ENGLISH, false);

        int avgSize = (featureSize + outputSize) / 2;

        this.network = trainer.createNewNetwork(avgSize, trainingSet, 0.0001, 0.1, 0.11);
    }

    public double[] getOutput(MLData input) {
        MLData output = network.compute(input);
        return output.getData();
    }

    public void addOutput(String newOutput) {
        outputs.add(newOutput);
    }

    public void saveNetworkToFile(String filePath) throws IOException {
        EncogDirectoryPersistence.saveObject(new File(filePath), network);
        writeOutputFile(filePath);
    }

    private void writeOutputFile(String filePath) throws IOException {
        JSONObject object = new JSONObject();
        JSONArray featuresJson = new JSONArray();
        JSONArray outputsJson = new JSONArray();

        featuresJson.put(features);
        outputsJson.put(outputs);
        object.put(OUTPUTS_JSON_KEY, outputs);
        object.put(FEATURES_JSON_KEY, features);

        object.put(DATASET_JSON_KEY, dataSetFilePath);

        try (FileWriter file = new FileWriter(filePath + HEADERS_JSON_POSTFIX)) {
            file.write(object.toString());
        }
    }

    private void readOutputFile(String filePath) throws IOException, InvalidNetworkFile {
        String content = new String(Files.readAllBytes(Paths.get(filePath + "_headers.json")));

        JSONObject object = new JSONObject(content);
        JSONArray featuresJson = object.getJSONArray(FEATURES_JSON_KEY);
        JSONArray outputsJson = object.getJSONArray(OUTPUTS_JSON_KEY);

        this.features = new ArrayList<>();
        for (int i = 0; i < featuresJson.length(); i++) {
            this.features.add((String) featuresJson.get(i));
        }

        this.outputs = new ArrayList<>();
        for (int i = 0; i < outputsJson.length(); i++) {
            this.outputs.add((String) outputsJson.get(i));
        }

        String dataSetFilePath = object.getString(DATASET_JSON_KEY);
        if (dataSetFilePath != null) {
            this.dataSetFilePath = dataSetFilePath;
        } else {
            throw new InvalidNetworkFile("Data file corrupted");
        }
    }

    public String getTrainingFile() {
        return dataSetFilePath;
    }

    public void addEntry(List<String> inputStrings) throws IOException {
        DataSetEditor dataSetEditor = new DataSetEditor();
        dataSetEditor.addEntry(inputStrings, dataSetFilePath);
    }
}
