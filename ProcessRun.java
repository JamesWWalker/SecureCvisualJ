import java.io.*;
import java.util.*;
import java.util.stream.*;

public class ProcessRun {

  public String programName;
  
  private ArrayList<ProcessState> runSequence;
  private int index;
  private ProcessState current;
  private HashMap<Integer, String> assembly;
  private HashMap<Integer, ProcessState> seedStates;
  private ArrayList<ProgramSection> sections;


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


  public void jumpToEvent(int jump) {
    if (jump < 0 || jump >= runSequence.size()) {
      System.err.println("Illegal jump to " + jump + " (size " + runSequence.size() + ").");
      return;
    }
    while (jump > index+1) next();
    while (jump < index-1) previous();
  }


  public boolean next() {
  System.err.println("CALLED NEXT");
    if (index+1 < runSequence.size()) {
      ProcessState.applyDelta(current, runSequence.get(++index));
      return true;
    }
    return false;
  }


  public boolean previous() {
  System.err.println("CALLED PREVIOUS");
    if (index-1 >= 0) {
      int seedIndex = getClosestSeedIndex(index-1);
      ProcessState seedState = ProcessState.newInstance(seedStates.get(seedIndex));
      while (seedIndex+1 < index-1) ProcessState.applyDelta(seedState, runSequence.get(++seedIndex));
      current = seedState;
      --index;
      return true;
    }
    return false;
  }


  public void jumpToEnd() {
  System.err.println("CALLED END");
    while (index < runSequence.size()-1) next();
  }


  public void jumpToBeginning() {
  System.err.println("CALLED BEGINNING");
    index = 0;
    current = new ProcessState();
    ProcessState.applyDelta(current, runSequence.get(index));
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
    ArrayList<String> contents = new ArrayList<>();
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
      String line = null;
      while ((line = bufferedReader.readLine()) != null) contents.add(line);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }

    String[] decomposedPath = filename.replaceAll("./", "").split("/");
    String fn = decomposedPath[decomposedPath.length-1];
    programName = fn.contains(".") ? fn.substring(0, fn.lastIndexOf('.')) : fn;
    programName = programName.substring(0, programName.length()-7); // get rid of -output

    runSequence = new ArrayList<>();
    index = 0;
    current = new ProcessState();
    assembly = new HashMap<>();
    sections = new ArrayList<>();

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

        if (!type.equals("assembly") && !type.equals("section")) 
          currentEvent = Integer.parseInt(parameters[0]);

        if (currentEvent != previousEvent) {
          previousEvent = currentEvent;
          addProcessState(runningStack, stateToAdd);
          stateToAdd = new ProcessState();
        }

        if (type.equals("function_invocation")) parseFunctionInvocation(stateToAdd, runningStack, parameters);
        else if (type.equals("return")) runningStack.remove(runningStack.size()-1);
        else if (type.equals("variable_access")) parseVariableAccess(stateToAdd, variableTypes, parameters);
        else if (type.equals("register")) stateToAdd.registers.put(parameters[2], parameters[3]);
        else if (type.equals("assembly")) parseAssembly(parameters);
        else if (type.equals("section")) parseSection(parameters);
        else {
          System.err.println("Unrecognized event type " + type + ". Terminating parse.");
          return;
        }

        if (n == contents.size()-1) addProcessState(runningStack, stateToAdd);

      } // Done making forward deltas

      // We need to navigate in both directions.
      // Correctly making reverse deltas turns out to be incredibly hard, so what we're actually
      // going to do is seed the run sequence with periodic complete state information; and when
      // traveling backwards, we start from the nearest seed that's "behind" us and apply deltas
      // forward from that point until we reach the desired index. This will serve as a compromise
      // between time and space.

      seedStates = new HashMap<>();
      int seedIndex = 0;
      ProcessState seedState = new ProcessState();
      assert index == 0;
      ProcessState.applyDelta(seedState, runSequence.get(index));
      seedStates.put(0, ProcessState.newInstance(seedState));

      while (seedIndex + seedDistance < runSequence.size()) {
        int targetIndex = seedIndex + seedDistance;
        while (seedIndex <= targetIndex) {
          ++seedIndex;
          ProcessState.applyDelta(seedState, runSequence.get(seedIndex));
        }
        seedStates.put(seedIndex, ProcessState.newInstance(seedState));
      }

      // Done: initialize "current" state
      ProcessState.applyDelta(current, runSequence.get(index));

    } catch (Exception ex) {
      System.err.println("Analysis parsing failure on line: " + line);
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
    stack.add(new ActivationRecord(function[0], function[1]));
  }


  private void parseVariableAccess(ProcessState state,
                           HashMap<String, String> variableTypes,
                           String[] parameters) throws Exception
  {
    String type = parameters[4];
    String name = parameters[5];
    String scope = parameters[2];
    if (type.equals("null")) type = getVariableType(name, variableTypes);
    else variableTypes.put(name, type);
    long address;
    if (parameters[3].startsWith("0x")) address = Long.parseUnsignedLong(parameters[3].substring(2), 16);
    else address = Long.parseUnsignedLong(parameters[3], 16);

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


  private String getVariableType(String name, HashMap<String, String> variableTypes) throws Exception {
    return variableTypes.containsKey(name.split(":")[0]) ?
                variableTypes.get(name).split("\\s+")[0] :
                                              "Unknown";
  }


  private void parseAssembly(String[] parameters) throws Exception {
    int lineNumber = Integer.parseInt(parameters[0]);
    String assemblyLine = parameters[1];
    if (assembly.containsKey(lineNumber)) {
      if (!assembly.get(lineNumber).contains(assemblyLine))
        assembly.put(lineNumber, assembly.get(lineNumber) + System.lineSeparator() + assemblyLine);
    }
    else assembly.put(lineNumber, assemblyLine);
  }
  
  
  private void parseSection(String[] parameters) throws Exception {
    String[] addressStr = parameters[2].split("-");
    long address = Long.parseUnsignedLong(addressStr[0].substring(3), 16) -
                   Long.parseUnsignedLong(addressStr[1].substring(2, addressStr[1].length()-1), 16);
  
    sections.add(new ProgramSection(parameters[0],
                                    parameters[1],
                                    address,
                                    Long.parseUnsignedLong(parameters[3].substring(2), 16),
                                    Long.parseUnsignedLong(parameters[4].substring(2), 16),
                                    parameters[5],
                                    parameters[6]));
  }
  

}
