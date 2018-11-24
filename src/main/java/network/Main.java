package network;

import frontend.Asker;
import frontend.InvalidFeaturesException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static List<String> defaultHeaders = new ArrayList<>(Arrays.asList("Short Stay","Penguins","Longest rivers","Island","Seaside","Historical","Speaking Spanish","Food"));
    private static List<String> defaultOutputs = new ArrayList<>(Arrays.asList("Spain", "Greece", "Argentina", "Egypt", "Australia"));

    public static void main(String[] args) {
        boolean cycle = true;
        try {
            Asker asker = new Asker(defaultHeaders, new NetworkQueryHandler("trip_edit.csv", defaultHeaders, defaultOutputs));

            asker.cycle();


        } catch (InvalidFeaturesException | IOException e) {
            e.printStackTrace();
        }
    }
}
