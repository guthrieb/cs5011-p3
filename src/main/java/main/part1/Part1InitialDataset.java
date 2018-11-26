package main.part1;

import network.InvalidNetworkFile;
import network.NetworkWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Part1InitialDataset {
    private static final String TRAINING_TABLE_CSV = "training_tables/trip_part1_1.csv";
    private static final String NETWORK_SAVE_FILE = "networks_generated/part1_1";
    private static List<String> defaultHeaders = new ArrayList<>(Arrays.asList("Short Stay","Penguins","Longest rivers",
            "Island","Seaside","Historical","Speaking Spanish","Food"));
    private static List<String> defaultOutputs = new ArrayList<>(Arrays.asList("Spain", "Greece", "Argentina", "Egypt",
            "Australia"));

    public static void main(String[] args) {

        if(args.length > 0 && args[0].equalsIgnoreCase("-n")) {
            //Construct new network
            System.out.println("Creating network");

            int iterations = getIterations(args, 1);

            NetworkWrapper wrapper = new NetworkWrapper(TRAINING_TABLE_CSV, defaultHeaders, defaultOutputs);
            try {


                wrapper.trainAndTest(0.1, 0.1, 0.01, 0.2);
                wrapper.saveNetworkToFile(NETWORK_SAVE_FILE);

                calculateAverageClassificationError(iterations, wrapper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //Load existing network
            try {
                int iterations = getIterations(args, 0);

                System.out.println("Loading network: " + NETWORK_SAVE_FILE);

                NetworkWrapper wrapper = NetworkWrapper.loadNetworkFromFile(NETWORK_SAVE_FILE);

                calculateAverageClassificationError(iterations, wrapper);

            } catch (IOException | InvalidNetworkFile e) {
                e.printStackTrace();
            }
        }

    }

    private static int getIterations(String[] args, int index) {
        int iterations = 1;

        if (args.length > index) {
            iterations = Integer.parseInt(args[index]);
        }
        return iterations;
    }

    private static void calculateAverageClassificationError(int iterations, NetworkWrapper wrapper) {
        double totalError = 0;
        for (int i = 0; i < iterations; i++) {
            wrapper.trainAndTest(9, 0.1, 0.1, 0.1, 0.2);
            totalError += wrapper.getClassificationError();
        }

        System.out.println("Average Classification Error: "+ totalError / (double) iterations);
    }
}
