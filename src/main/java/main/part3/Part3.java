package main.part3;

import frontend.Asker;
import frontend.InvalidFeaturesException;
import network.InvalidNetworkFile;
import network.NetworkQueryHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Part3 {
    private static final String TRAINING_TABLE = "training_tables/trip_part3_1.csv";
    private static final String SAVE_LOCATION = "networks_generated/part2_1";
    private static List<String> defaultHeaders = new ArrayList<>(Arrays.asList("Short Stay","Penguins","Longest rivers",
            "Island","Seaside","Historical","Speaking Spanish","Food"));
    private static List<String> defaultOutputs = new ArrayList<>(Arrays.asList("Spain", "Greece", "Argentina", "Egypt",
            "Australia"));

    public static void main(String[] args) {
        if(args.length > 0 && args[0].equalsIgnoreCase("-n")) {
            try {
                System.out.println("Creating new network");
                NetworkQueryHandler wrapper = new NetworkQueryHandler(TRAINING_TABLE, defaultHeaders, defaultOutputs);
                wrapper.train();
                Asker asker = new Asker(defaultHeaders, defaultOutputs, wrapper, true);

                asker.cycle();
                wrapper.save(SAVE_LOCATION);
            } catch (InvalidFeaturesException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                System.out.println("Loading network: " + SAVE_LOCATION);
                NetworkQueryHandler wrapper = NetworkQueryHandler.load(SAVE_LOCATION);
                Asker asker = new Asker(wrapper, true);

                asker.cycle();
                wrapper.save(SAVE_LOCATION);
            } catch (IOException | InvalidNetworkFile | InvalidFeaturesException e) {
                e.printStackTrace();
            }
        }
    }
}
