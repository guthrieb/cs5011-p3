package frontend;

import network.NetworkQueryHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Asker {
    private List<String> features;
    private List<String> toAsk;
    private List<String> recentlyAsked = new ArrayList<>();
    private int questionsAsked = 0;

    private NetworkQueryHandler network;

    public Asker(List<String> features, NetworkQueryHandler wrapper) throws InvalidFeaturesException {
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
        List<Boolean> featuresMatch = new ArrayList<>();


        while(!askedAllQuestions()) {
            String question = getQuestion();
            boolean featureTrue = askQuestion(question);

            featuresMatch.add(featureTrue);
            System.out.println("Best guess: " + network.getOutputString(featuresMatch) );
        }

        String outputString = network.getOutputString(featuresMatch);

        if(correctGuess(outputString)) {
            System.out.println("Correct guess");
        } else {
            System.out.println("Incorrect guess, adjusting training table");
            String actualOuptut = getActualLocation();
            adjustTrainingTable(featuresMatch, actualOuptut);
        }


    }

    private String getActualLocation() {
        System.out.println("Where is your actual dream location?");
        Scanner in = new Scanner(System.in);

        return in.next();
    }

    private void adjustTrainingTable(List<Boolean> featuresMatch, String outputString) throws IOException {
        network.addNewRelation(featuresMatch, outputString);
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
