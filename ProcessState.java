import java.util.*;

public class ProcessState {

  public int sourceLine;
  public TreeMap<String, String> registers;
  public TreeMap<String, VariableDelta> variables;
  public List<ActivationRecord> stack; // Important note: stack is complete for each state, not a delta.


  public ProcessState() {
    registers = new TreeMap<>();
    variables = new TreeMap<>();
    stack = new ArrayList<>();
  }


  public ProcessState(
    int sourceLineIn,
    TreeMap<String, String> registersIn,
    TreeMap<String, VariableDelta> variablesIn,
    List<ActivationRecord> stackIn)
  {
    sourceLine = sourceLineIn;
    registers = registersIn;
    variables = variablesIn;
    stack = stackIn;
  }


  // Copy constructor
  public static ProcessState newInstance(ProcessState state) {
    TreeMap<String, String> copyRegisters = new TreeMap<>();
    Set<String> registerKeys = state.registers.keySet();
    for (String key : registerKeys) copyRegisters.put(key, state.registers.get(key));

    TreeMap<String, VariableDelta> copyVariables = new TreeMap<>();
    Set<String> variableKeys = state.variables.keySet();
    for (String key : variableKeys) copyVariables.put(key, new VariableDelta(state.variables.get(key)));

    List<ActivationRecord> copyStack = new ArrayList<>();
    for (ActivationRecord ar : state.stack) copyStack.add(new ActivationRecord(ar));

    return new ProcessState(state.sourceLine, copyRegisters, copyVariables, copyStack);
  }


  public static void applyDelta(ProcessState target, ProcessState delta) {

    Set<String> varKeys = target.variables.keySet();
    Set<String> varKeysDelta = delta.variables.keySet();

    // Remove variables whose scope isn't present in the delta's activation records
    // EXCEPT GLOBALS; THOSE ARE ALWAYS PRESENT.
    ArrayList<String> variablesToRemove = new ArrayList<>();;
    for (String key : varKeys) {
      if (!target.variables.get(key).scope.equals(UIUtils.GLOBAL) &&
          delta.stack.stream().filter(
          x -> x.function.equals(target.variables.get(key).scope))
          .findFirst().orElse(null) == null)
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

    if (target.stack.size() != delta.stack.size()) {
      while (target.stack.size() > delta.stack.size()) target.stack.remove(target.stack.size()-1);
      if (target.stack.size() < delta.stack.size()) {
        for (int n = target.stack.size(); n < delta.stack.size(); ++n) {
          target.stack.add(new ActivationRecord(delta.stack.get(n)));
        }
      }
    }
    else {
      for (int n = 0; n < target.stack.size(); ++n) {
        if (!target.stack.get(n).equals(delta.stack.get(n))) target.stack.set(n, delta.stack.get(n));
      }
    }

  } // applyDelta()


  @Override
  public String toString() {
    return "" + sourceLine + System.lineSeparator() +
           registers + System.lineSeparator() +
           variables + System.lineSeparator() +
           stack + System.lineSeparator();
  }



}

