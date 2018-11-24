package network;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.back.Backpropagation;

public class NetworkTrainer {
    private int bias = 1;

    public BasicNetwork createNewNetwork(MLDataSet dataSet, double learningRate, double momentum, double maximumError) {


        int inputSize = dataSet.getInputSize();
        int idealSize = dataSet.getIdealSize();

        System.out.println(inputSize);
        System.out.println(idealSize);

        int hiddenUnits = Math.toIntExact(Math.round(((double) inputSize + (double) idealSize) * 2.0 / 3.0));

//        int hiddenUnits = 10;

        BasicNetwork network = null;
        boolean cycle = true;
        while(cycle) {
            System.out.println("Hidden Units: " + hiddenUnits);
            network = constructNetworkLayers(hiddenUnits, inputSize, idealSize);
            double error = backPropagateTraining(dataSet, learningRate, momentum, maximumError, network);


            System.out.println("ERROR: " + error);
            System.out.println("MAX ERROR: " + maximumError);
            if(error < maximumError) {
                cycle = false;
            } else {
                cycle = true;
                hiddenUnits--;
            }
        }

        return network;
    }

    private BasicNetwork constructNetworkLayers(int hiddenUnits, int inputSize, int idealSize) {
        BasicNetwork network = new BasicNetwork();
        BasicLayer inputLayer = new BasicLayer(null, true, inputSize);
        BasicLayer hiddenLayer = new BasicLayer(new ActivationSigmoid(), true, hiddenUnits + bias);
        BasicLayer outputLayer = new BasicLayer(new ActivationSigmoid(), false, idealSize);

        network.addLayer(inputLayer);
        network.addLayer(hiddenLayer);
        network.addLayer(outputLayer);
        network.getStructure().finalizeStructure();
        network.reset();
        return network;
    }

    private double backPropagateTraining(MLDataSet dataSet, double learningRate, double momentum, double maximumError, BasicNetwork network) {
        final Backpropagation trainer = new Backpropagation(network, dataSet, learningRate, momentum);

        int epoch = 1;

        do {
            trainer.iteration();
            epoch++;
        } while (trainer.getError() > maximumError && epoch < 100000);

        System.out.println("Iteration: " + epoch + ", error: " + trainer.getError());

        return trainer.getError();
    }
}
