import java.util.*;

public class ProcessState {

  public int sourceLine;
  public String assemblyCode;
  public TreeMap<String, String> registers;
  public TreeMap<String, VariableDelta> variables;
  public ArrayList<ActivationRecord> stack;


  public ProcessState() {
    registers = new TreeMap<>();
    variables = new TreeMap<>();
    stack = new ArrayList<>();
  }


  public ProcessState(
    int sourceLineIn,
    String assemblyCodeIn,
    TreeMap<String, String> registersIn,
    TreeMap<String, VariableDelta> variablesIn,
    ArrayList<ActivationRecord> stackIn)
  {
    sourceLine = sourceLineIn;
    assemblyCode = assemblyCodeIn;
    registers = registersIn;
    variables = variablesIn;
    stack = stackIn;
  }


  public static void applyDelta(ProcessState target, ProcessState delta) {

    Set<String> varKeys = target.variables.keySet();
    Set<String> varKeysDelta = delta.variables.keySet();

    // Remove variables whose scope isn't present in the delta's activation records
    ArrayList<String> variablesToRemove = new ArrayList<>();;
    for (String key : varKeys) {
      if (delta.stack.stream().filter(
          x -> x.function.equals(target.variables.get(key).scope)).findFirst().orElse(null) == null)
      {
        variablesToRemove.add(key);
      }
    }
    for (String key : variablesToRemove) target.variables.remove(key);

    // Overwrite target variable values with delta's
    for (String key : varKeysDelta) target.variables.put(key, delta.variables.get(key));

    Set<String> regKeys = delta.registers.keySet();
    for (String key : regKeys) target.registers.put(key, delta.registers.get(key));

    target.sourceLine = delta.sourceLine;
    target.assemblyCode = delta.assemblyCode;

    while (target.stack.size() > delta.stack.size()) target.stack.remove(target.stack.size()-1);
    if (target.stack.size() < delta.stack.size()) {
      for (int n = target.stack.size(); n < delta.stack.size(); ++n) {
        target.stack.add(delta.stack.get(n));
      }
    }

  } // applyDelta()


}

