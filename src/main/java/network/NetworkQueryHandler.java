package network;

import com.opencsv.CSVReader;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class NetworkQueryHandler implements NetworkQuerierI {
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

        MLData input = new BasicMLData(convertedInputs);

        wrapper.getOutput(input);
        double[] output = wrapper.getOutput(input);

        return convertToString(output);
    }

    @Override
    public void addNewOutput(String newOutput) {

    }

    public void addNewRelation(List<Boolean> inputs, String newOutput) throws IOException {
        if (!outputStrings.contains(newOutput)) {
            outputStrings.add(newOutput);
            wrapper.addOutput(newOutput);
        }

        addToTrainingTable(inputs, newOutput);
        //TODO add to training data file
        //TODO retrain network
    }

    private void addToTrainingTable(List<Boolean> inputs, String output) throws IOException {
        String trainingFile = wrapper.getTrainingFile();

        try (CSVReader reader = new CSVReader(new FileReader(trainingFile))) {
            for (String[] nextRecord : reader) {

            }
        }
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
}
