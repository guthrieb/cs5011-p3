package network;

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;

import java.io.IOException;
import java.util.*;

public class NetworkQueryHandler {
    private NetworkWrapper wrapper;
    private List<String> features;
    private List<String> outputStrings;

    public NetworkQueryHandler(String dataSetFilePath, List<String> features, List<String> outputStrings) {
        this.wrapper = new NetworkWrapper(dataSetFilePath, features, outputStrings);
        this.features = features;
        this.outputStrings = outputStrings;
    }

    public static NetworkQueryHandler load(String saveLocation) throws IOException, InvalidNetworkFile {
        NetworkWrapper wrapper = NetworkWrapper.loadNetworkFromFile(saveLocation);
        List<String> features = wrapper.getFeatures();
        List<String> outputs = wrapper.getOutputs();

        return new NetworkQueryHandler(wrapper.getTrainingFile(), features, outputs);
    }


    public String getOutputString(List<Boolean> inputs, List<String> excludedOutputs, double probabilityLimit) throws ExhaustedAllOutputsException {

        double[] convertedInputs = convertInputs(inputs);

        MLData input = new BasicMLData(convertedInputs);

        double[] output = wrapper.getOutput(input);


        return convertToString(output, excludedOutputs, probabilityLimit);
    }


    public void addNewRelation(List<Boolean> inputs, String newOutput) throws IOException {
        if (!outputStrings.contains(newOutput)) {
            outputStrings.add(newOutput);
            wrapper.addOutput(newOutput);
        }

        addToTrainingTable(inputs, newOutput);
        train();
    }

    private void addToTrainingTable(List<Boolean> inputs, String output) throws IOException {

        List<String> entry = new ArrayList<>();
        if (outputStrings.contains(output)) {
            for (Boolean bool : inputs) {
                entry.add(bool ? "1.0" : "0.0");
            }

            for (String outputString : outputStrings) {
                if (outputString.equals(output)) {
                    entry.add("1.0");
                } else {
                    entry.add("0.0");
                }
            }
        }
        wrapper.addEntry(entry);
    }

    private String convertToString(double[] outputData, List<String> excludedOutputs, double probabilityLimit) throws ExhaustedAllOutputsException {
        //Map match output probability to the index of an output string as specified for the system
        TreeMap<Double, Integer> valueIndexOrdered = new TreeMap<>();
        for (int i = 0; i < outputData.length; i++) {
            valueIndexOrdered.put(outputData[i], i);
        }


        NavigableMap<Double, Integer> doubleIntegerNavigableMap = valueIndexOrdered.descendingMap();

        for (Map.Entry<Double, Integer> entry : doubleIntegerNavigableMap.entrySet()) {
            if (entry.getKey() < probabilityLimit) {
                return null;
            } else {
                String potentialVal = outputStrings.get(entry.getValue());

                if (!excludedOutputs.contains(potentialVal)) {
                    return potentialVal;
                }
            }

        }

        throw new ExhaustedAllOutputsException("All outputs exhausted");
    }

    private double[] convertInputs(List<Boolean> inputs) {
        double[] convertedInputs = new double[features.size()];

        for (int i = 0; i < convertedInputs.length; i++) {
            if (i < inputs.size() && inputs.get(i)) {
                convertedInputs[i] = 1.0;
            } else {
                convertedInputs[i] = 0.0;
            }
        }

        return convertedInputs;
    }

    public void train() {
        wrapper.retrain();
    }


    public void addNewFeature(List<Boolean> matchingNewFeature, String newFeature) throws IOException {
        wrapper.addNewFeature(matchingNewFeature, newFeature);
    }

    public void addNewOutput(String outputString) throws IOException {
        wrapper.addNewOutput(outputString);
    }

    public void save(String s) throws IOException {
        wrapper.saveNetworkToFile(s);
    }

    public List<String> getFeatures() {
        return features;
    }

    public List<String> getOutputs() {
        return outputStrings;
    }


}
