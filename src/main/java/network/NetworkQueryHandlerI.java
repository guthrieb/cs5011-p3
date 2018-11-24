package network;

import java.io.IOException;
import java.util.List;

public interface NetworkQueryHandlerI {
    public String getOutputString(List<Boolean> inputs);

    public void addNewRelation(List<Boolean> inputs, String newOutput) throws IOException;
}
