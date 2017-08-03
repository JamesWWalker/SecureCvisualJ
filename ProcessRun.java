import java.io.*;
import java.util.*;
import java.util.stream.*;

public class ProcessRun {

  String programName;
  ArrayList<ProcessState> runSequenceForward;
  ArrayList<ProcessState> runSequenceReverse;
  int index;
  ProcessState current;
  HashMap<Integer, String> assembly;


  public ProcessRun(String filename) {
    loadRun(filename);
  }


  public int getSourceLine() {
    return current.sourceLine;
  }


  public String getAssembly() {
    if (assembly.containsKey(current.sourceLine)) return assembly.get(current.sourceLine);
    else return "";
  }


  public List<ActivationRecord> getStack(DetailLevel detailLevel) {
    if (detailLevel == DetailLevel.ADVANCED || detailLevel == DetailLevel.EXPERT) return current.stack;
    else { // In basic and intermediate mode, remove external & 'invisible' functions
      List<ActivationRecord> filteredStack = new ArrayList<>();
      for (ActivationRecord ar : current.stack) {
        if (ar.file.equals(programName) && !ar.function.equals("_start")) filteredStack.add(ar);
      }
      return filteredStack;
    }
  }


  public TreeMap<String, String> getRegisters(DetailLevel detailLevel) {
    if (detailLevel == DetailLevel.NOVICE) {
      System.err.println("WARNING: Request for registers in novice mode.");
      return null;
    }
    return current.registers;
  }


  public TreeMap<String, VariableDelta> getVariables() {
    return current.variables;
  }


  public void jumpToEvent(int jump) {
    if (jump < 0 || jump >= runSequenceForward.size()) {
      System.err.println("Illegal jump to " + jump + " (size " + runSequenceForward.size() + ").");
      return;
    }
    while (jump > index) ProcessState.applyDelta(current, runSequenceForward.get(++index));
    while (jump < index) ProcessState.applyDelta(current, runSequenceReverse.get(--index));
  }


  public boolean next() {
    if (index+1 < runSequenceForward.size()) {
      ProcessState.applyDelta(current, runSequenceForward.get(++index));
      return true;
    }
    return false;
  }


  public boolean previous() {
    if (index-1 >= 0) {
      ProcessState.applyDelta(current, runSequenceReverse.get(--index));
      return true;
    }
    return false;
  }


  public void jumpToEnd() {
    while (index < runSequenceForward.size()-1) ProcessState.applyDelta(current, runSequenceForward.get(++index));
  }


  public void jumpToBeginning() {
    while (index > 0) ProcessState.applyDelta(current, runSequenceReverse.get(--index));
  }


  public void loadRun(String filename) {
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

    runSequenceForward = new ArrayList<>();
    runSequenceReverse = new ArrayList<>();
    index = 0;
    current = new ProcessState();
    assembly = new HashMap<>();

    int previousEvent = 0;
    int currentEvent = 0;

    ProcessState stateToAdd = new ProcessState();
    List<ActivationRecord> runningStack = new ArrayList<>();
    HashMap<String, String> variableTypes = new HashMap<>();

    String line = "";

    try {

      // We need to make deltas forwards and backwards so that we can navigate in both directions
      for (int n = 0; n < contents.size(); ++n) {
        line = contents.get(n);
        String[] typeAndParameters = line.split("~!~");
        String type = typeAndParameters[0];
        String[] parameters = typeAndParameters[1].split("\\|");

        if (!type.equals("assembly")) currentEvent = Integer.parseInt(parameters[0]);

        if (currentEvent != previousEvent) {
          previousEvent = currentEvent;
          addProcessState(runningStack, stateToAdd, false, n);
          stateToAdd = new ProcessState();
        }

        parseAnalysisLine(contents, type, parameters, n, line, stateToAdd, runningStack,
                          variableTypes, false);
      }

      stateToAdd = new ProcessState();

      for (int n = contents.size()-1; n >= 0; --n) {
        line = contents.get(n);
        String[] typeAndParameters = line.split("~!~");
        String type = typeAndParameters[0];
        String[] parameters = typeAndParameters[1].split("\\|");

        if (!type.equals("assembly")) currentEvent = Integer.parseInt(parameters[0]);

        if (currentEvent != previousEvent) {
          previousEvent = currentEvent;
          addProcessState(runningStack, stateToAdd, true, n);
          stateToAdd = new ProcessState();
        }

        parseAnalysisLine(contents, type, parameters, n, line, stateToAdd, runningStack,
                          variableTypes, true);
      }

      Collections.reverse(runSequenceReverse);

      // Since each state has complete call stack info (not a delta), set all the reverse equal to the forward
      for (int n = 0; n < runSequenceForward.size(); ++n) {
        ProcessState forwardState = runSequenceForward.get(n);
        ProcessState reverseState = runSequenceReverse.get(n);
        for (ActivationRecord ar : forwardState.stack) reverseState.stack.add(new ActivationRecord(ar));
      }

      assert runSequenceForward.size() == runSequenceReverse.size();

      ProcessState.applyDelta(current, runSequenceForward.get(index));

    } catch (Exception ex) {
      System.err.println("Analysis parsing failure on line: " + line);
      ex.printStackTrace();
      return;
    }
  } // loadRun()


  void parseAnalysisLine(ArrayList<String> contents,
                         String type,
                         String[] parameters,
                         int n,
                         String line,
                         ProcessState stateToAdd,
                         List<ActivationRecord> runningStack,
                         HashMap<String, String> variableTypes,
                         boolean reverse) throws Exception
  {
    if (type.equals("function_invocation")) {
      if (!reverse) parseFunctionInvocation(stateToAdd, runningStack, parameters);
    }
    else if (type.equals("return")) {
      if (!reverse) runningStack.remove(runningStack.size()-1);
    }
    else if (type.equals("variable_access")) parseVariableAccess(stateToAdd, variableTypes, parameters);
    else if (type.equals("register")) stateToAdd.registers.put(parameters[2], parameters[3]);
    else if (type.equals("assembly")) {
      if (!reverse) parseAssembly(parameters);
    }
    else {
      System.err.println("Unrecognized event type " + type + ". Terminating parse.");
      return;
    }

    if (!reverse && n == contents.size()-1) addProcessState(runningStack, stateToAdd, reverse, n);
    else if (reverse && n == 0) addProcessState(runningStack, stateToAdd, reverse, n);

  } // parseAnalysisLine()


  void addProcessState(List<ActivationRecord> runningStack,
                       ProcessState stateToAdd,
                       boolean reverse,
                       int n)
  {
    if (!reverse) {
      for (ActivationRecord ar : runningStack) stateToAdd.stack.add(new ActivationRecord(ar));
      runSequenceForward.add(ProcessState.newInstance(stateToAdd));
    }
    else runSequenceReverse.add(ProcessState.newInstance(stateToAdd));
  }


  void parseFunctionInvocation(ProcessState state,
                               List<ActivationRecord> stack,
                               String[] parameters) throws Exception
  {
    state.sourceLine = Integer.parseInt(parameters[1]);
    String[] function = parameters[2].split("`");
    stack.add(new ActivationRecord(function[0], function[1]));
  }


  void parseVariableAccess(ProcessState state,
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


  String getVariableType(String name, HashMap<String, String> variableTypes) throws Exception {
    return variableTypes.containsKey(name.split(":")[0]) ?
                variableTypes.get(name).split("\\s+")[0] :
                                              "Unknown";
  }


  void parseAssembly(String[] parameters) throws Exception {
    int lineNumber = Integer.parseInt(parameters[0]);
    String assemblyLine = parameters[1];
    if (assembly.containsKey(lineNumber)) {
      if (!assembly.get(lineNumber).contains(assemblyLine))
        assembly.put(lineNumber, assembly.get(lineNumber) + System.lineSeparator() + assemblyLine);
    }
    else assembly.put(lineNumber, assemblyLine);
  }

}
