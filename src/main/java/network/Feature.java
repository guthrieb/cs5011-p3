package network;

public class Feature {
    String id;
    String questionString = "Is your dream destination well for it's " + id + "?";

    public Feature(String id) {
        this.id = id;
    }

    public Feature(String id, String questionPrefix) {
        this.id = id;
        this.questionString = questionPrefix + id + "?";
    }

    public String getQuestionString() {
        return questionString;
    }
}
