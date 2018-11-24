package network;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkQueryHandler implements NetworkQueryHandlerI {
    private NetworkWrapper wrapper;
    private List<String> features;
    private List<String> outputStrings;

    public NetworkQueryHandler(String dataSetFilePath, List<String> features, List<String> outputStrings) {
        this.wrapper = new NetworkWrapper(dataSetFilePath, features, outputStrings);
        this.features = features;
        this.outputStrings = outputStrings;
    }


    public String getOutputString(List<Boolean> inputs) {

        double[] convertedInputs = convertInputs(inputs);
        System.out.println(Arrays.toString(convertedInputs));

        MLData input = new BasicMLData(convertedInputs);

        double[] output = wrapper.getOutput(input);

        return convertToString(output);
    }


    @Override
    public void addNewRelation(List<Boolean> inputs, String newOutput) throws IOException {
        if (!outputStrings.contains(newOutput)) {
            outputStrings.add(newOutput);
            wrapper.addOutput(newOutput);
        }

        addToTrainingTable(inputs, newOutput);
        train();
    }

    /**
     * Adds
     *
     * @param inputs
     * @param output
     * @throws IOException
     */
    private void addToTrainingTable(List<Boolean> inputs, String output) throws IOException {

        List<String> entry = new ArrayList<>();
        if(outputStrings.contains(output)) {
            for(Boolean bool : inputs) {
                entry.add(bool ? "1.0" : "0.0");
            }

            for(String outputString : outputStrings) {
                if(outputString.equals(output)) {
                    entry.add("1.0");
                } else {
                    entry.add("0.0");
                }
            }
        } else {
//            addNewColumn(inputs, output);
        }
        wrapper.addEntry(entry);
    }

    private String convertToString(double[] outputData) {
        double maxVal = Double.NEGATIVE_INFINITY;
        int maxValIndex = -1;

        for (int i = 0; i < outputData.length; i++) {
            if (outputData[i] > maxVal) {
                maxVal = outputData[i];
                maxValIndex = i;
            }
        }

        return outputStrings.get(maxValIndex);
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
}
