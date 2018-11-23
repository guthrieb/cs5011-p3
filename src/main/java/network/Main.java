package network;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        NetworkWrapper wrapper = new NetworkWrapper("trip.csv", Arrays.asList("i0", "i1", "i2", "i3", "i4", "i5", "i6", "i7"), Arrays.asList("o0", "o1", "o2", "o3", "o4"));
        wrapper.retrain();
        try {
            wrapper.saveNetworkToFile("basic_save");
            NetworkWrapper.loadNetworkFromFile("basic_save");

        } catch (IOException | InvalidNetworkFile e) {
            e.printStackTrace();
        }
    }
}
