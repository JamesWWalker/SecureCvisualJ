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
  public boolean[] stepsApplied = new boolean[UIUtils.SD_EV_MEMORYUNLOCKED+1];
  
  public SensitiveDataVariable() { }
  
  public SensitiveDataVariable(String scopeIn, String nameIn) {
    scope = scopeIn;
    name = nameIn;
    memoryLocked = valueSet = valueCleared = false;
    isSecure = true;
//    message = "Declared variable";
//    shortMessage = "Declared";
    for (int n = 0; n <= UIUtils.SD_EV_MEMORYUNLOCKED; ++n) stepsApplied[n] = false;
  }
  
  public SensitiveDataVariable newInstance() {
    SensitiveDataVariable var = new SensitiveDataVariable();
    var.scope = scope;
    var.name = name;
    var.shortMessage = shortMessage;
    var.message = message;
    var.memoryLocked = memoryLocked;
    var.valueSet = valueSet;
    var.valueCleared = valueCleared;
    var.isSecure = isSecure;
    for (int n = 0; n <= UIUtils.SD_EV_MEMORYUNLOCKED; ++n) var.stepsApplied[n] = stepsApplied[n];
    return var;
  }

}
