package network;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.strategy.SmartLearningRate;
import org.encog.neural.networks.training.strategy.SmartMomentum;

public class NetworkTrainer {
    private static final int BIAS_UNIT = 1;

    BasicNetwork createNewNetwork(MLDataSet dataSet, double learningRate, double momentum, double maximumError) {
        int inputSize = dataSet.getInputSize();
        int idealSize = dataSet.getIdealSize();

        int hiddenUnits = Math.toIntExact(Math.round(((double) inputSize + (double) idealSize) * 2.0 / 3.0));

        BasicNetwork network = null;
        boolean cycle = true;
        while(cycle) {
            network = constructNetworkLayers(hiddenUnits, inputSize, idealSize);
            double error = smartBackPropagateTraining(dataSet, learningRate, momentum, maximumError, network);

            if(error < maximumError) {
                cycle = false;
            } else {
                cycle = true;
                hiddenUnits--;
            }
        }

        return network;
    }

    BasicNetwork createNewNetwork(MLDataSet dataSet, int hiddenUnits, double learningRate, double momentum, double maximumError) {
        int inputSize = dataSet.getInputSize();
        int idealSize = dataSet.getIdealSize();

        BasicNetwork network;
        network = constructNetworkLayers(hiddenUnits, inputSize, idealSize);
        smartBackPropagateTraining(dataSet, learningRate, momentum, maximumError, network);


        return network;
    }

    BasicNetwork constructNetworkLayers(int hiddenUnits, int inputSize, int idealSize) {
        BasicNetwork network = new BasicNetwork();
        BasicLayer inputLayer = new BasicLayer(null, true, inputSize);
        BasicLayer hiddenLayer = new BasicLayer(new ActivationSigmoid(), true, hiddenUnits + BIAS_UNIT);
        BasicLayer outputLayer = new BasicLayer(new ActivationSigmoid(), false, idealSize);

        network.addLayer(inputLayer);
        network.addLayer(hiddenLayer);
        network.addLayer(outputLayer);
        network.getStructure().finalizeStructure();
        network.reset();
        return network;
    }

    private double smartBackPropagateTraining(MLDataSet dataSet, double learningRate, double momentum, double maximumError, BasicNetwork network) {
        final Backpropagation trainer = new Backpropagation(network, dataSet, learningRate, momentum);
        trainer.addStrategy(new SmartLearningRate());
        trainer.addStrategy(new SmartMomentum());

        int epoch = 1;

        do {
            trainer.iteration();
            epoch++;
        } while (trainer.getError() > maximumError && epoch < 100000);

        return trainer.getError();
    }

}
