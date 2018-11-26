package main.part2;

import frontend.Asker;
import frontend.InvalidFeaturesException;
import network.InvalidNetworkFile;
import network.NetworkQueryHandler;
import network.NetworkWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Part2 {
    private static final String TRAINING_TABLE = "training_tables/trip_part2_1.csv";
    private static final String NETWORK_SAVE_FILE = "networks_generated/part2_1";
    private static List<String> defaultHeaders = new ArrayList<>(Arrays.asList("Short Stay","Penguins","Longest rivers",
            "Island","Seaside","Historical","Speaking Spanish","Food"));
    private static List<String> defaultOutputs = new ArrayList<>(Arrays.asList("Spain", "Greece", "Argentina", "Egypt",
            "Australia"));

    public static void main(String[] args) {
        if(args.length > 0 && args[0].equalsIgnoreCase("-n")) {
            try {
                System.out.println("Creating network");
                NetworkQueryHandler wrapper = new NetworkQueryHandler(TRAINING_TABLE, defaultHeaders, defaultOutputs);
                Asker asker = new Asker(defaultHeaders, defaultOutputs, wrapper, false);

                asker.cycle();
                wrapper.save(NETWORK_SAVE_FILE);
            } catch (InvalidFeaturesException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                System.out.println("Loading network: " + NETWORK_SAVE_FILE);

                NetworkQueryHandler load = NetworkQueryHandler.load(NETWORK_SAVE_FILE);

                Asker asker = new Asker(load, false);
                asker.cycle();
            } catch (IOException | InvalidNetworkFile | InvalidFeaturesException e) {
                e.printStackTrace();
            }

        }
    }
}
