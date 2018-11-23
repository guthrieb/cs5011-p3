package network;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.back.Backpropagation;

public class NetworkTrainer {
    public BasicNetwork createNewNetwork(int hiddenLayers, MLDataSet dataSet, double learningRate, double momentum, double maximumError) {
        BasicNetwork network = new BasicNetwork();


        int inputSize = dataSet.getInputSize();
        int idealSize = dataSet.getIdealSize();

        BasicLayer inputLayer = new BasicLayer(null, true, inputSize);
        BasicLayer hiddenLayer = new BasicLayer(new ActivationSigmoid(), true, hiddenLayers);
        BasicLayer outputLayer = new BasicLayer(new ActivationSigmoid(), false, idealSize);

        network.addLayer(inputLayer);
        network.addLayer(hiddenLayer);
        network.addLayer(outputLayer);
        network.getStructure().finalizeStructure();


        final Backpropagation trainer = new Backpropagation(network, dataSet, learningRate, momentum);

        int epoch = 1;

        do {
            trainer.iteration();
            epoch++;
            System.out.println(trainer.getError());
        } while (trainer.getError() > maximumError);
        return network;
    }
}
