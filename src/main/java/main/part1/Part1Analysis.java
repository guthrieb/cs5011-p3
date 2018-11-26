package main.part1;

import network.TrainingWriter;

import java.io.IOException;

public class Part1Analysis {
    public static void main(String[] args) {
        if(args.length < 6) {
            System.out.println("USAGE");
        } else {
            String inputFile = args[0];
            String outputFile = args[1];
            int featureSize = Integer.parseInt(args[2]);
            int outputsSize = Integer.parseInt(args[3]);
            int iterations = Integer.parseInt(args[4]);

            for(int i = 1; i <= 20; i++) {
                TrainingWriter writer = new TrainingWriter(iterations);
                String trimmedOutputFile = outputFile.substring(0, outputFile.lastIndexOf("."));

                try {
                    writer.writeTrainingErrorsToFile(inputFile, trimmedOutputFile + "_" + i + ".csv",
                            featureSize,
                            outputsSize,
                            i,
                            0.1,
                            0,
                            0.01,
                            10000,
                            iterations
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
