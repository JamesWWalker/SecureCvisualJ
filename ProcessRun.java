import java.io.*;
import java.util.*;
import java.util.stream.*;
import javafx.beans.property.*;

public class ProcessRun {

  public boolean isNull() { return isNull; }
  public String programName;
  
  private IntegerProperty numberOfEvents = new SimpleIntegerProperty();
  public final int getNumberOfEvents() { return numberOfEvents.get(); }
  public final void setNumberOfEvents(int value) { numberOfEvents.set(value); }
  public IntegerProperty numberOfEventsProperty() { return numberOfEvents; }
  
  private final ReadOnlyIntegerWrapper index = new ReadOnlyIntegerWrapper();
  public final int getIndex() { return index.get(); }
  public final ReadOnlyIntegerProperty indexProperty() { return index.getReadOnlyProperty(); }
  
  public String invocation = "";
  private HashMap<Integer, String> assembly;
  private HashMap<Integer, String> outputs;
  private ProcessState current;
  private boolean isNull;
  private ArrayList<ProcessState> runSequence;
  private ArrayList<ProgramSection> sections;
  private HashMap<Integer, ProcessState> seedStates;
  private HashMap<Integer, SensitiveDataState> sdStates;
  private SensitiveDataState runningSdState;
  
  
  public ProcessRun() {
    isNull = true;
    setNumberOfEvents(0);
  }


  public ProcessRun(String filename) {
    loadRun(filename, 100);
  }


  public ProcessRun(String filename, int seedDistance) {
    loadRun(filename, seedDistance);
  }


  public int getSourceLine() {
    return current.sourceLine;
  }


  public String getAssembly() {
    if (assembly.containsKey(current.sourceLine)) return assembly.get(current.sourceLine);
    else return "";
  }
  
  
  public List<String> getOutput() {
    Set<Integer> keys = outputs.keySet();
    List<Integer> list = new ArrayList<>(keys);
    Collections.sort(list);
    List<String> rets = new ArrayList<>();
    for (Integer i : list) {
      if (i <= index.get()) rets.add(outputs.get(i));
    }
    return rets;
  }


  public List<ActivationRecord> getStack() {
    List<ActivationRecord> stack = new ArrayList<>();
    for (ActivationRecord ar : current.stack) stack.add(new ActivationRecord(ar));
    return stack;
  }


  public TreeMap<String, String> getRegisters() {
    TreeMap<String, String> registers = new TreeMap<>();
    Set<String> keys = current.registers.keySet();
    for (String key : keys) registers.put(key, current.registers.get(key));
    return registers;
  }


  public TreeMap<String, VariableDelta> getVariables() {
    TreeMap<String, VariableDelta> variables = new TreeMap<>();
    Set<String> keys = current.variables.keySet();
    for (String key : keys) variables.put(key, new VariableDelta(current.variables.get(key)));
    return variables;
  }
  
  
  public ArrayList<ProgramSection> getSections() {
    ArrayList<ProgramSection> retSections = new ArrayList<>();
    for (ProgramSection section : sections) retSections.add(new ProgramSection(section));
    return retSections;
  }
  
  
  public SensitiveDataState getSensitiveDataState() {
    Set<Integer> keys = sdStates.keySet();
    List<Integer> list = new ArrayList<>(keys);
    Collections.sort(list);
    for (int n = list.size()-1; n >=0; --n) {
      if (n <= index.get()) return sdStates.get(n);
    }
    return null;
  }


  public boolean jumpToEvent(int jump) {
    if (jump < 0 || jump >= runSequence.size()) {
      System.err.println("WARNING: Illegal jump to " + jump + " (size " + runSequence.size() + ").");
      return false;
    }
    if (jump == index.get()) return false;
    while (jump > index.get()+1) next();
    while (jump < index.get()-1) previous();
    return true;
  }


  public boolean next() {
    if (index.get()+1 < runSequence.size()) {
      index.set(index.get()+1);
      ProcessState.applyDelta(current, runSequence.get(index.get()));
      return true;
    }
    return false;
  }


  public boolean previous() {
    if (index.get()-1 >= 0) {
      int seedIndex = getClosestSeedIndex(index.get()-1);
      ProcessState seedState = ProcessState.newInstance(seedStates.get(seedIndex));
      while (seedIndex+1 < index.get()) ProcessState.applyDelta(seedState, runSequence.get(++seedIndex));
      current = seedState;
      index.set(index.get()-1);
      return true;
    }
    return false;
  }


  public void jumpToEnd() {
    while (index.get() < runSequence.size()-1) next();
  }


  public void jumpToBeginning() {
    index.set(0);
    current = new ProcessState();
    ProcessState.applyDelta(current, runSequence.get(index.get()));
  }


  private Integer getClosestSeedIndex(int target) {
    Set<Integer> seeds = seedStates.keySet();
    Integer closest = 0;
    for (Integer i : seeds) {
      if (i > closest && i < target) closest = i;
    }
    return closest;
  }


  public void loadRun(String filename, int seedDistance) {
    List<String> contents = new ArrayList<>();
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
      String line = null;
      while ((line = bufferedReader.readLine()) != null) contents.add(line);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }

    String[] decomposedPath = filename.split("/");
    String fn = decomposedPath[decomposedPath.length-1];
    programName = fn.contains(".") ? fn.substring(0, fn.lastIndexOf('.')) : fn;
    programName = programName.substring(0, programName.length()-7); // get rid of -output

    runSequence = new ArrayList<>();
    index.set(0);
    current = new ProcessState();
    assembly = new HashMap<>();
    outputs = new HashMap<>();
    sections = new ArrayList<>();
    sdStates = new HashMap<>();
    runningSdState = new SensitiveDataState();

    int previousEvent = 0;
    int currentEvent = 0;

    ProcessState stateToAdd = new ProcessState();
    List<ActivationRecord> runningStack = new ArrayList<>();
    HashMap<String, String> variableTypes = new HashMap<>();

    String line = "";

    try {

      // Make forward deltas
      for (int n = 0; n < contents.size(); ++n) {
        line = contents.get(n);
        String[] typeAndParameters = line.split("~!~");
        String type = typeAndParameters[0];
        String[] parameters = typeAndParameters[1].split("\\|");

        if (!type.equals("assembly") && 
            !type.equals("section") && 
            !type.equals("return_address") &&
            !type.equals("invocation") &&
            !type.equals("output"))
          currentEvent = Integer.parseInt(parameters[0]);

        if (currentEvent != previousEvent) {
          previousEvent = currentEvent;
          addProcessState(runningStack, stateToAdd);
          stateToAdd = new ProcessState();
        }

        if (type.equals("function_invocation")) 
          parseFunctionInvocation(stateToAdd, runningStack, parameters);
        else if (type.equals("return")) runningStack.remove(runningStack.size()-1);
        else if (type.equals("variable_access")) 
          parseVariableAccess(stateToAdd, runningStack, variableTypes, parameters);
        else if (type.equals("register")) stateToAdd.registers.put(parameters[2], parameters[3]);
        else if (type.equals("assembly")) parseAssembly(parameters);
        else if (type.equals("output")) outputs.put(Integer.parseInt(parameters[0]), parameters[1]);
        else if (type.equals("section")) parseSection(parameters);
        else if (type.equals("return_address")) parseReturnAddress(stateToAdd, runningStack, parameters);
        else if (type.equals("invocation")) invocation = parameters[0];
        else if (type.equals("sd_corezero")) parseCoreZero(parameters);
        else if (type.equals("sd_lock")) parseSdLock(parameters);
        else if (type.equals("sd_unlock")) parseSdUnlock(parameters);
        else if (type.equals("sd_clear")) parseSdClear(parameters);
        else if (type.equals("sd_set")) parseSdSet(parameters);
        else {
          System.err.println("ERROR: Unrecognized event type " + type + ". Terminating parse.");
          return;
        }

        if (n == contents.size()-1) addProcessState(runningStack, stateToAdd);

      } // Done making forward deltas

      // We need to navigate in both directions.
      // Correctly making reverse deltas turns out to be incredibly hard, so what we're actually
      // going to do is seed the run sequence with periodic complete state information; and when
      // traveling backwards, we start from the nearest seed that's "behind" us and apply deltas
      // forward from that point until we reach the desired indexInternal. This will serve as a compromise
      // between time and space.

      seedStates = new HashMap<>();
      int seedIndex = 0;
      ProcessState seedState = new ProcessState();
      assert index.get() == 0;
      ProcessState.applyDelta(seedState, runSequence.get(index.get()));
      seedStates.put(0, ProcessState.newInstance(seedState));

      while (seedIndex + seedDistance < runSequence.size()) {
        int targetIndex = seedIndex + seedDistance;
        while (seedIndex <= targetIndex) {
          ++seedIndex;
          ProcessState.applyDelta(seedState, runSequence.get(seedIndex));
        }
        seedStates.put(seedIndex, ProcessState.newInstance(seedState));
      }

      // Done: initialize "current" state & perform other initializations
      ProcessState.applyDelta(current, runSequence.get(index.get()));
      setNumberOfEvents(runSequence.size() - 1);
      isNull = false;

    } catch (Exception ex) {
      System.err.println("ERROR: Analysis parsing failure on line: " + line);
      ex.printStackTrace();
      return;
    }
  } // loadRun()


  private void addProcessState(List<ActivationRecord> runningStack, ProcessState stateToAdd) {
    for (ActivationRecord ar : runningStack) stateToAdd.stack.add(new ActivationRecord(ar));
    runSequence.add(ProcessState.newInstance(stateToAdd));
  }


  private void parseFunctionInvocation(ProcessState state,
                                       List<ActivationRecord> stack,
                                       String[] parameters) throws Exception
  {
    state.sourceLine = Integer.parseInt(parameters[1]);
    String[] function = parameters[2].split("`");
    
    long address = 0;
    if (!parameters[3].contains("----")) address = Long.parseLong(parameters[3].substring(2), 16);
    
    // If the same function name+address already exists in the stack, then don't add the new one
    // because it's a duplicate
    for (ActivationRecord ar : stack) {
      if ((ar.function.equals(function[1]) || ar.function.startsWith(function[1] + "{")) &&
          ar.address == address)
        return;
    }
    
    // Handle recursion by detecting multiple calls to same function and assigning them numbers
    String alteredFunction = function[1];
    for (int n = stack.size()-1; n >= 0; --n) {
      String compareFunction = stack.get(n).function;
      if (stack.get(n).file.equals(function[0]) && 
          (compareFunction.equals(function[1]) || 
          compareFunction.startsWith(function[1] + "{")))
      {
        if (!compareFunction.contains("{")) alteredFunction += "{2}";
        else {
          String[] inter1 = compareFunction.split("\\{");
          String[] inter2 = inter1[1].split("\\}");
          int number = Integer.parseInt(inter2[0]);
          alteredFunction += ("{" + (number+1) + "}");
        }
        break;
      }
    }
    stack.add(new ActivationRecord(function[0], alteredFunction, address));
  }


  private void parseVariableAccess(ProcessState state,
                                   List<ActivationRecord> stack,
                                   HashMap<String, String> variableTypes,
                                   String[] parameters) throws Exception
  {
    String type = parameters[4];
    if (type.contains("[")) type = type.split("\\[")[0].trim();
    String name = parameters[5];
    String scope = parameters[2];
    if (type.equals("null")) type = getVariableType(name, variableTypes);
    else variableTypes.put(name, type);
    long address;
    if (parameters[3].startsWith("0x")) address = Long.parseUnsignedLong(parameters[3].substring(2), 16);
    else address = Long.parseUnsignedLong(parameters[3], 16);
    
    // Detect recursive calls and assign variable to most recent call of that function
    if (!scope.equals(UIUtils.GLOBAL)) {
      for (int n = stack.size()-1; n >= 0; --n) {
        String compareFunction = stack.get(n).function;
        if (compareFunction.startsWith(scope + "{")) {
          scope = compareFunction;
          break;
        }
      }
    }

    VariableDelta var = new VariableDelta(
      type,
      scope,
      name,
      parameters[6], // value
      address
    );

    if (parameters.length == 8) {
      var.value = parameters[7];
      var.pointsTo = Long.parseUnsignedLong(parameters[6].substring(2), 16);
    }

    state.sourceLine = Integer.parseInt(parameters[1]);
    state.variables.put(scope + "," + name, var);
  }


  private String getVariableType(String nameIn, HashMap<String, String> variableTypes) throws Exception {
    String name = nameIn.split(":")[0];
    return variableTypes.containsKey(name) ? variableTypes.get(name).split("\\s+")[0] : "Unknown";
  }


  private void parseAssembly(String[] parameters) throws Exception {
    if (parameters.length < 2) return;
    int lineNumber = Integer.parseInt(parameters[0]);
    String assemblyLine = parameters[1];
    if (assembly.containsKey(lineNumber)) {
      if (!assembly.get(lineNumber).contains(assemblyLine))
        assembly.put(lineNumber, assembly.get(lineNumber) + System.lineSeparator() + assemblyLine);
    }
    else assembly.put(lineNumber, assemblyLine);
  }
  
  
  private void parseSection(String[] parameters) throws Exception {
    sections.add(new ProgramSection(Arrays.asList(parameters)));
  }
  
  
  private void parseReturnAddress(ProcessState state,
                                  List<ActivationRecord> stack, 
                                  String[] parameters) 
               throws Exception 
  {
    String scope = parameters[0];
    for (int n = stack.size()-1; n >= 0; --n) {
      String compareFunction = stack.get(n).function;
      if (compareFunction.equals(scope) || compareFunction.startsWith(scope + "{")) {
        stack.get(n).dynamicLink = parameters[1];
        stack.get(n).returnAddress = parameters[2];
//        if (parameters.length > 2) state.sourceLine = Integer.parseInt(parameters[2]);
        return;
      }
    }
  }
  
  
  private void parseCoreZero(String[] parameters) throws Exception {
    runningSdState = runningSdState.newInstance();
    runningSdState.coreSizeZeroed = true;
    runningSdState.coreSizeZeroedHere = true;
    sdStates.put(Integer.parseInt(parameters[0]), runningSdState);
  }
  
  
  private void parseSdLock(String[] parameters) throws Exception {
//    SensitiveDataVariable var = runningSdState.getVariable(parameters[2], parameters[3]);
//    if (var == null) 
  }
  
  
  private void parseSdUnlock(String[] parameters) throws Exception {
  }
  
  
  private void parseSdClear(String[] parameters) throws Exception {
  }
  
  
  private void parseSdSet(String[] parameters) throws Exception {
  }
  
  
  /*
  parseCoreZero(parameters);
        else if (type.equals("sd_lock")) parseSdLock(parameters);
        else if (type.equals("sd_unlock")) parseSdUnlock(parameters);
        else if (type.equals("sd_clear")) parseSdClear(parameters);
        else if (type.equals("sd_set")) parseSdSet(parameters);
  */
  

}
