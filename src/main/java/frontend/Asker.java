package frontend;

import network.Feature;

import java.util.ArrayList;
import java.util.List;

public class Asker {
    private List<Feature> features;
    private List<Feature> recentlyAsked = new ArrayList<Feature>();

    public Asker(List<Feature> features) throws InvalidFeaturesException {
        if (features.size() == 0) {
            throw new InvalidFeaturesException("Cannot handle 0 features");
        }
        this.features = features;
    }

    public String getQuestion() {
        return getFeature().getQuestionString();
    }

    public Feature getFeature() {
        if (features.size() > 0) {
            Feature feature = features.get(features.size() - 1);
            features.remove(features.size() - 1);
            recentlyAsked.add(feature);
            return feature;
        } else {
            features = recentlyAsked;
            recentlyAsked = new ArrayList<Feature>();
            return getFeature();
        }
    }


}
