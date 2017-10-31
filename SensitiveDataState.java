import java.util.*;

public class SensitiveDataState {

  public boolean coreSizeZeroed = false;
  public boolean coreSizeZeroedHere = false;
  public Map<String, SensitiveDataVariable> variables = new TreeMap<>();
  
  public SensitiveDataState newInstance() {
    SensitiveDataState state = new SensitiveDataState();
    state.coreSizeZeroed = coreSizeZeroed;
    state.coreSizeZeroedHere = false;
    for (String s : variables.keySet()) state.variables.put(s, variables.get(s).newInstance());
    return state;
  }
  

}
