package frontend;

import network.ExhaustedAllOutputsException;
import network.NetworkQueryHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Asker {
    private static final double PROBABILITY_LIMIT_ON_GUESSES = 0.8;
    private String reportPath;
    private List<String> outputs;
    private final boolean adjustTrainingTable;
    private List<String> features;
    private List<String> toAsk;
    private List<String> recentlyAsked = new ArrayList<>();

    private NetworkQueryHandler networkQueryHandler;

    private Asker(List<String> features, List<String> outputs, NetworkQueryHandler wrapper, boolean adjustTrainingTable, String reportPath) throws InvalidFeaturesException {
        this.reportPath = reportPath;
        this.outputs = outputs;
        this.adjustTrainingTable = adjustTrainingTable;
        if (features.size() == 0) {
            throw new InvalidFeaturesException("Cannot handle 0 features");
        }
        wrapper.train();
        this.networkQueryHandler = wrapper;
        this.features = features;
        this.toAsk = new ArrayList<>(this.features);
    }

    public Asker(List<String> features, List<String> outputs, NetworkQueryHandler wrapper, boolean adjustTrainingTable) throws InvalidFeaturesException {
        this(features, outputs, wrapper, adjustTrainingTable, null);
    }

    public Asker(NetworkQueryHandler wrapper, boolean adjustTrainingTable, String reportPath) throws InvalidFeaturesException {
        this(wrapper.getFeatures(), wrapper.getOutputs(), wrapper, adjustTrainingTable, reportPath);
    }

    public Asker(NetworkQueryHandler wrapper, boolean adjustTrainingTable) throws InvalidFeaturesException {
        this(wrapper.getFeatures(), wrapper.getOutputs(), wrapper, adjustTrainingTable, null);
    }

    public String getQuestion() {
        return getFeature();
    }


    public void cycle() throws IOException {
        List<Boolean> inputFeatures = new ArrayList<>();

        boolean guessedCorrectly = false;

        List<String> previouslyAnswered = new ArrayList<>();
        //While not guessed correctly and still has questions to ask ask questions
        boolean exhaustedAllOptions = false;

        System.out.println("I'm going to list some features that you might want on your dream trip. \nTell me yes or no if you'd like them as features of the trip to your dream location.");
        while (!askedAllQuestions() && !guessedCorrectly) {
            String question = getQuestion();
            boolean featureTrue = askQuestion(question);

            inputFeatures.add(featureTrue);

            String currentGuess = null;


            if (askedAllQuestions()) {
                //If asked all questions get the best possible answer
                try {
                    currentGuess = networkQueryHandler.getOutputString(inputFeatures, previouslyAnswered, 0.0);
                } catch (ExhaustedAllOutputsException e) {
                    e.printStackTrace();
                }
            } else {
                //Else ask next question in loop
                try {
                    currentGuess = networkQueryHandler.getOutputString(inputFeatures, previouslyAnswered, PROBABILITY_LIMIT_ON_GUESSES);
                } catch (ExhaustedAllOutputsException e) {
                    exhaustedAllOptions = true;
                }
            }

            //If still have questions to ask and have guessed recently
            if (!exhaustedAllOptions && currentGuess != null) {
                guessedCorrectly = correctGuess(currentGuess);

                if (!guessedCorrectly) {
                    previouslyAnswered.add(currentGuess);
                }
            }
        }

        if (guessedCorrectly) {
            System.out.println("Good. Ending guessing.");
        } else {
            System.out.println("Incorrect guess");

            String bestGuess = null;
            try {
                bestGuess = networkQueryHandler.getOutputString(inputFeatures, new ArrayList<>(), 0.0);
            } catch (ExhaustedAllOutputsException e) {
                e.printStackTrace();
            }

            if (adjustTrainingTable) {
                System.out.println("Adjusting Training Table");
                String[] locationAndFeature = getActualLocationAndNewFeature(bestGuess);

                System.out.println("Adding Location and Feature " + Arrays.toString(locationAndFeature) + " to training table");

                if(reportPath != null) {
                    reportUpdate(bestGuess, locationAndFeature[0], locationAndFeature[1], reportPath);
                }
                List<Boolean> locationsExhibitingFeature = mapLocationsToNewFeatures(locationAndFeature[1], bestGuess);
                adjustTrainingTable(inputFeatures, locationsExhibitingFeature, locationAndFeature[1], locationAndFeature[0]);
            } else
                //No more guesses and not able to adjust training table
                System.out.println("Giving up ¯\\_(ツ)_/¯");
        }
    }

    private void reportUpdate(String bestGuess, String actualOutput, String criticalFeature, String reportPath) throws IOException {
        try (FileWriter writer = new FileWriter(reportPath, true)) {
            writer.write("Incorrect Location guess - " + "{BestGuess=\"" + bestGuess + "\", ActualLocation=\"" + actualOutput + "\", DistinguishingFeature=\"" + criticalFeature + "\"}");
        }
    }


    private List<Boolean> mapLocationsToNewFeatures(String feature, String excluded) {
        Scanner in = new Scanner(System.in);

        List<Boolean> featureExhibitsTrait = new ArrayList<>();
        for (String output : outputs) {
            if(output.equalsIgnoreCase(feature)) {
                //If feature already stored
                featureExhibitsTrait.add(true);
            } else if (!output.equalsIgnoreCase(excluded)) {

                System.out.println("Would you say the location " + output + " is known for the feature " + feature + "? [y/n]");

                if (in.next().equals("y")) {
                    featureExhibitsTrait.add(true);
                } else {
                    featureExhibitsTrait.add(false);
                }
            } else {
                //If answer best guess(es) exclude trait
                featureExhibitsTrait.add(false);
            }
        }


        return featureExhibitsTrait;
    }

    private String[] getActualLocationAndNewFeature(String bestGuess) {
        System.out.println("Where is your actual dream location?");
        Scanner in = new Scanner(System.in);
        String actualLocation = in.nextLine();

        System.out.println("Name a feature that would distinguish this from " + bestGuess + "?");
        String distinguishingFeature = in.nextLine();

        return new String[]{actualLocation, distinguishingFeature};
    }

    private void adjustTrainingTable(List<Boolean> inputFeature, List<Boolean> matchingNewFeature, String newFeature, String outputString) throws IOException {
        //Ad a new feature, add the new output, if it is a new output and add the relation between the new output and element as the final entry to the training table
        networkQueryHandler.addNewFeature(matchingNewFeature, newFeature);

        if (!outputs.contains(outputString)) {
            networkQueryHandler.addNewOutput(outputString);
        }
        inputFeature.add(true);
        networkQueryHandler.addNewRelation(inputFeature, outputString);
    }

    private boolean correctGuess(String outputString) {
        while (true) {
            System.out.println("Your dream location is " + outputString + ", is this right? [y/n]");
            Scanner scanner = new Scanner(System.in);
            String next = scanner.nextLine();
            if (next.equalsIgnoreCase("y")) {
                return true;
            } else if (next.equalsIgnoreCase("n")) {
                return false;
            }
        }
    }

    public String getFeature() {
        //If still have questions to ask retrieve feature to ask about
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
        while (true) {
            System.out.println(question + "? [y/n]");
            Scanner scanner = new Scanner(System.in);
            String next = scanner.next();
            if (next.equalsIgnoreCase("y")) {
                return true;
            } else if (next.equalsIgnoreCase("n")) {
                return false;
            } else {
                System.out.println("Please answer \"y\" or \"n\"");
            }
        }
    }

    public boolean askedAllQuestions() {
        return toAsk.size() <= 0;
    }

}
