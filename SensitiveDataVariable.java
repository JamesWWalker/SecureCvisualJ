import java.util.*;

public class SensitiveDataVariable {

  public String scope;
  public String name;
  public String shortMessage;
  public String message;
  public boolean memoryLocked;
  public boolean valueSet;
  public boolean valueCleared;
  public boolean isSecure;
  public List<Boolean> stepsApplied = new ArrayList<>();
  
  public SensitiveDataVariable(String scopeIn, String nameIn) {
    scope = scopeIn;
    name = nameIn;
    memoryLocked = valueSet = valueCleared = false;
    isSecure = true;
    message = "Declared variable";
    shortMessage = "Declared";
    for (int n = 0; n <= UIUtils.SD_EV_MEMORYUNLOCKED; ++n) stepsApplied.add(false);
  }

}
