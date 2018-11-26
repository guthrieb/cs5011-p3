package network;

import com.opencsv.CSVWriter;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.specific.CSVNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.util.csv.CSVFormat;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainingWriter {

    private List<Double> totalErrors = new ArrayList<>();
    private int currentIteration = 0;
    private int totalIterations;

    public TrainingWriter(int totalIterations) {
        this.totalIterations = totalIterations;
    }

    public void writeTrainingErrorsToFile(String dataSetFilePath, String outputFile, int featureSize, int outputSize,
                                          int hiddenUnits, double learningRate, double momentum, double maximumError,
                                          int epochLimit, int iterations) throws IOException {
        CSVNeuralDataSet trainingSet = new CSVNeuralDataSet(dataSetFilePath, featureSize, outputSize, true, CSVFormat.ENGLISH, false);

        for(int i = 0; i < iterations; i++) {
            writeNetworkTrainingResults(new FileWriter(outputFile), trainingSet, hiddenUnits, learningRate, momentum, epochLimit);
            currentIteration++;
        }

    }

    public void writeNetworkTrainingResults(FileWriter writer, MLDataSet dataSet, int hiddenUnits, double learningRate, double momentum, int epochLimit) throws IOException {
        int inputSize = dataSet.getInputSize();
        int idealSize = dataSet.getIdealSize();

        BasicNetwork network;
        try (CSVWriter csvWriter = new CSVWriter(writer, ',', CSVWriter.NO_QUOTE_CHARACTER)) {

            if(currentIteration == totalIterations - 1) {
                csvWriter.writeNext(new String[]{"epoch", "hidden units", "learning rate", "momentum", "error"});
            }
            NetworkTrainer networkTrainer = new NetworkTrainer();

            network = networkTrainer.constructNetworkLayers(hiddenUnits, inputSize, idealSize);
            writeBackPropagateTraining(dataSet, csvWriter, hiddenUnits, learningRate, momentum, network, epochLimit);
        }
    }

    private void writeBackPropagateTraining(MLDataSet dataSet, CSVWriter writer, int hiddenUnits, double learningRate, double momentum, BasicNetwork network, int epochLimit) {
        final Backpropagation trainer = new Backpropagation(network, dataSet, learningRate, momentum);

        int epoch = 0;

        do {
            //Train until epoch limit reached
            trainer.iteration();
            String[] entry = new String[5];

            if(currentIteration > 0) {
                totalErrors.set(epoch, totalErrors.get(epoch) + trainer.getError());
            } else {
                totalErrors.add(trainer.getError());
            }

            if(currentIteration == totalIterations - 1) {
                //If reached final iteration average total errors and write to file
                entry[4] = String.valueOf(totalErrors.get(epoch)/totalIterations);
                entry[0] = String.valueOf(epoch);
                entry[1] = String.valueOf(hiddenUnits);
                entry[2] = String.valueOf(learningRate);
                entry[3] = String.valueOf(momentum);
                writer.writeNext(entry);
            }
            epoch++;


        } while (epoch < epochLimit);
    }
}