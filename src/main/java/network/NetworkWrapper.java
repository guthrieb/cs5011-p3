package network;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.data.specific.CSVNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
    private double classificationError;


    public double getClassificationError() {
        return classificationError;
    }

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

    void retrain() {
        NetworkTrainer trainer = new NetworkTrainer();
        int featureSize = features.size();
        int outputSize = outputs.size();
        CSVNeuralDataSet trainingSet = new CSVNeuralDataSet(dataSetFilePath, featureSize, outputSize, true, CSVFormat.ENGLISH, false);


        this.network = trainer.createNewNetwork(trainingSet, 0.011111111111111112, 0, 0.01);
    }

    double[] getOutput(MLData input) {
        MLData output = network.compute(input);
        return output.getData();
    }

    void addOutput(String newOutput) {
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

        //Get data set filepath, feature and output names from json files
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

    public void addNewOutput(String newColumn) throws IOException {
        DataSetEditor editor = new DataSetEditor();
        editor.addColumn(features.size() + outputs.size(), newColumn, 0.0, dataSetFilePath);
        addOutput(newColumn);
    }

    public void addNewFeature(List<Boolean> matchingNewFeature, String newFeature) throws IOException {
        DataSetEditor editor = new DataSetEditor();
        editor.addColumn(features.size(), newFeature, 0.0, dataSetFilePath);
        editor.initialiseValues(features.size(), features.size() + 1, matchingNewFeature, dataSetFilePath);
        features.add(newFeature);
    }

    public void trainAndTest(double learningRate, double momentum, double maximumError, double testingDataSize) {
        NetworkTrainer trainer = new NetworkTrainer();
        int featureSize = features.size();
        int outputSize = outputs.size();
        CSVNeuralDataSet dataSet = new CSVNeuralDataSet(dataSetFilePath, featureSize, outputSize, true, CSVFormat.ENGLISH, false);

        //Split by percentage specified by testing data size
        long recordCount = dataSet.getRecordCount();
        long numberOfTestingPairs = Math.round((double) recordCount * testingDataSize);

        BasicMLDataSet trainingSet = new BasicMLDataSet();
        BasicMLDataSet testingSet = new BasicMLDataSet();

        populateTestingAndTrainingSet(dataSet, recordCount, numberOfTestingPairs, trainingSet, testingSet);

        this.network = trainer.createNewNetwork(trainingSet, learningRate, momentum, maximumError);
        this.classificationError = test(network, testingSet);
    }

    public void trainAndTest(int hiddenUnits, double learningRate, double momentum, double maximumError, double testingDataSize) {
        NetworkTrainer trainer = new NetworkTrainer();
        int featureSize = features.size();
        int outputSize = outputs.size();
        CSVNeuralDataSet dataSet = new CSVNeuralDataSet(dataSetFilePath, featureSize, outputSize, true, CSVFormat.ENGLISH, false);

        //Split by percentage specified by testing data size
        long recordCount = dataSet.getRecordCount();
        long numberOfTestingPairs = Math.round((double) recordCount * testingDataSize);

        MLDataSet trainingSet = new BasicMLDataSet();
        BasicMLDataSet testingSet = new BasicMLDataSet();

        populateTestingAndTrainingSet(dataSet, recordCount, numberOfTestingPairs, trainingSet, testingSet);


        this.network = trainer.createNewNetwork(trainingSet, hiddenUnits, learningRate, momentum, maximumError);
        this.classificationError = test(network, testingSet);
    }

    private void populateTestingAndTrainingSet(CSVNeuralDataSet dataSet, long recordCount, long numberOfTestingPairs, MLDataSet trainingSet, MLDataSet testingSet) {
        List<Integer> testIndices = getTestingIndices(numberOfTestingPairs, recordCount);

        //Split data set into training an testing
        for (int i = 0; i < dataSet.size(); i++) {
            MLDataPair pair = dataSet.get(i);
            if (testIndices.contains(i)) {
                testingSet.add(pair);
                numberOfTestingPairs--;
            } else {
                trainingSet.add(pair);
            }
        }
    }

    private List<Integer> getTestingIndices(long numberOfTestingPairs, long totalPairs) {

        //Randomize indices then pull number equal to  number of testing pairs to get testing indices
        List<Integer> indices = new ArrayList<>();

        for (int i = 0; i < totalPairs; i++) {
            indices.add(i);
        }

        Collections.shuffle(indices);

        List<Integer> testingIndices = new ArrayList<>();
        for (int i = 0; i < numberOfTestingPairs; i++) {
            testingIndices.add(indices.get(i));
        }
        return testingIndices;
    }

    public double test(BasicNetwork network, MLDataSet testData) {

        double totalPairs = testData.size();
        double pairsMisclassified = 0;
        for (MLDataPair pair : testData) {
            double[] actualOutput = network.compute(pair.getInput()).getData();
            double[] expectedOutput = pair.getIdeal().getData();

            if (!correctlyClassified(expectedOutput, actualOutput)) {
                pairsMisclassified++;
            }
        }


        return pairsMisclassified / totalPairs;
    }

    private boolean correctlyClassified(double[] expectedOutput, double[] actualOutput) {
        int expectedIndex = -1;
        double maxExpected = Double.NEGATIVE_INFINITY;

        int actualIndex = -1;
        double maxActual = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < expectedOutput.length; i++) {
            if (expectedOutput[i] > maxExpected) {
                maxExpected = expectedOutput[i];
                expectedIndex = i;
            }

            if (actualOutput[i] > maxActual) {
                maxActual = actualOutput[i];
                actualIndex = i;
            }
        }

        //If expected output indice max probability then correctly classified

        return expectedIndex == actualIndex;
    }

    public List<String> getFeatures() {
        return features;
    }

    public List<String> getOutputs() {
        return outputs;
    }
}
