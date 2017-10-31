import java.util.*;

public class SensitiveDataState {

  public boolean coreSizeZeroed = false;
  public boolean coreSizeZeroedHere = false;
  public List<SensitiveDataVariable> variables = new ArrayList<>();
  
  public SensitiveDataState newInstance() {
    SensitiveDataState state = new SensitiveDataState();
    state.coreSizeZeroed = coreSizeZeroed;
    state.coreSizeZeroedHere = false;
    for (SensitiveDataVariable v : variables) state.variables.add(v);
    return state;
  }
  
  public SensitiveDataVariable getVariable(String scope, String name) {
    for (SensitiveDataVariable v : variables) {
      if (v.scope.equals(scope) && v.name.equals(name)) return v;
    }
    return null;
  }

}
