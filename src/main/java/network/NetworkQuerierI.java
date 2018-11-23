package network;

import java.util.List;

public interface NetworkQuerierI {
    public String getOutputString(List<Boolean> inputs);

    public void addNewOutput(String newOutput);
}
