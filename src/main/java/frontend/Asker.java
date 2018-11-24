package frontend;

import network.NetworkQueryHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Asker {
    private List<String> outputs;
    private List<String> features;
    private List<String> toAsk;
    private List<String> recentlyAsked = new ArrayList<>();
    private int questionsAsked = 0;

    private NetworkQueryHandler network;

    public Asker(List<String> features, List<String> outputs, NetworkQueryHandler wrapper) throws InvalidFeaturesException {
        this.outputs = outputs;
        if (features.size() == 0) {
            throw new InvalidFeaturesException("Cannot handle 0 features");
        }
        wrapper.train();
        this.network = wrapper;
        this.features = features;
        this.toAsk = new ArrayList<>(this.features);
    }

    public String getQuestion() {
        return getFeature();
    }


    public void cycle() throws IOException {
        List<Boolean> inputFeatures = new ArrayList<>();


        while(!askedAllQuestions()) {
            String question = getQuestion();
            boolean featureTrue = askQuestion(question);

            inputFeatures.add(featureTrue);
            System.out.println("Best guess: " + network.getOutputString(inputFeatures) );
        }

        String outputString = network.getOutputString(inputFeatures);

        if(correctGuess(outputString)) {
            System.out.println("Correct guess");
        } else {
            System.out.println("Incorrect guess, adjusting training table");
            String[] locationAndFeature = getActualLocationAndNewFeature();

            System.out.println("Adding Location and Feature: " + Arrays.toString(locationAndFeature));

            List<Boolean> locationsExhibitingFeature = mapLocationsToNewFeatures(locationAndFeature[1]);
            adjustTrainingTable(inputFeatures, locationsExhibitingFeature, locationAndFeature[1], locationAndFeature[0]);
        }


    }

    private List<Boolean> mapLocationsToNewFeatures(String feature) {
        Scanner in = new Scanner(System.in);

        List<Boolean> featureExhibitsTrait = new ArrayList<>();
        for (String output : outputs) {
            System.out.println("Would you say the location " + output + " is known for the feature " + feature + "? [y/n]");

            if (in.next().equals("y")) {
                featureExhibitsTrait.add(true);
            } else {
                featureExhibitsTrait.add(false);
            }
        }


        return featureExhibitsTrait;
    }

    private String[] getActualLocationAndNewFeature() {
        System.out.println("Where is your actual dream location?");
        Scanner in = new Scanner(System.in);
        String actualLocation = in.next();

        System.out.println("Name a feature that would distinguish this from other locations?");
        String distinguishingFeature = in.next();

        return new String[]{actualLocation, distinguishingFeature};
    }

    private void adjustTrainingTable(List<Boolean> inputFeature, List<Boolean> matchingNewFeature, String newFeature, String outputString) throws IOException {
        network.addNewFeature(matchingNewFeature, newFeature);
        network.addNewOutput(outputString);
        inputFeature.add(true);
        network.addNewRelation(inputFeature, outputString);
    }

    private boolean correctGuess(String outputString) {
        while(true) {
            System.out.println("Your dream location is " + outputString + ", is this right? [y/n]");
            Scanner scanner = new Scanner(System.in);
            String next = scanner.next();
            if (next.equalsIgnoreCase("y")) {
                return true;
            } else if (next.equalsIgnoreCase("n")) {
                return false;
            }
        }
    }

    public String getFeature() {
        if (toAsk.size() > 0) {
            String feature = toAsk.get(0);
            toAsk.remove(0);
            recentlyAsked.add(feature);
            return feature;
        } else {
            toAsk = recentlyAsked;
            recentlyAsked = new ArrayList<>();
            return getFeature();
        }
    }


    public boolean askQuestion(String question) {
        questionsAsked++;


        while(true) {
            System.out.println(question + ": [y/n]");
            Scanner scanner = new Scanner(System.in);
            String next = scanner.next();
            if (next.equalsIgnoreCase("y")) {
                return true;
            } else if (next.equalsIgnoreCase("n")) {
                return false;
            }
        }
    }

    public boolean askedAllQuestions() {
        return toAsk.size() <= 0;
    }

}
