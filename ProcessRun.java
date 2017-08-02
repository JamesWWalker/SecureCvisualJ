import java.io.*;
import java.util.*;
import java.util.stream.*;

public class ProcessRun {

  ArrayList<ProcessState> runSequence;
  int index;
  ProcessState current;
  HashMap<Integer, String> assembly;


  public ProcessRun(String filename) {
    loadRun(filename);
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

    runSequence = new ArrayList<>();
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
      for (int n = 0; n < contents.size(); ++n) {
        line = contents.get(n);

        String[] typeAndParameters = line.split("~!~");
        String type = typeAndParameters[0];

        if (type.equals("return")) runningStack.remove(runningStack.size()-1);
        else {
          String[] parameters = typeAndParameters[1].split("\\|");

          if (!type.equals("assembly") && !type.equals("return"))
            currentEvent = Integer.parseInt(parameters[0]);

          if (currentEvent != previousEvent) {
            previousEvent = currentEvent;
            stateToAdd.stack = runningStack.stream().map(ActivationRecord::new).collect(Collectors.toList());
            runSequence.add(ProcessState.newInstance(stateToAdd));
            stateToAdd = new ProcessState();
          }

          if (type.equals("function_invocation")) parseFunctionInvocation(stateToAdd, runningStack, parameters);
          else if (type.equals("variable_access")) parseVariableAccess(stateToAdd, variableTypes, parameters);
          else if (type.equals("register")) stateToAdd.registers.put(parameters[1], parameters[2]);
          else if (type.equals("assembly")) parseAssembly(parameters);
          else {
            System.err.println("Unrecognized event type " + type + ". Terminating parse.");
            return;
          }
        }

        if (n == contents.size()-1) {
          stateToAdd.stack = runningStack.stream().map(ActivationRecord::new).collect(Collectors.toList());
          runSequence.add(ProcessState.newInstance(stateToAdd));
        }
      }

    } catch (Exception ex) {
      System.err.println("Analysis parsing failure on line: " + line);
      ex.printStackTrace();
      return;
    }
  } // loadRun()


  public void parseFunctionInvocation(ProcessState state,
                                      List<ActivationRecord> stack,
                                      String[] parameters) throws Exception
  {
    state.sourceLine = Integer.parseInt(parameters[1]);
    stack.add(new ActivationRecord(parameters[2]));
  }


  public void parseVariableAccess(ProcessState state,
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

    state.sourceLine = Integer.parseInt(parameters[1]);
    state.variables.put(scope + "," + name, var);
  }


  String getVariableType(String name, HashMap<String, String> variableTypes) throws Exception {
    return variableTypes.containsKey(name.split(":")[0]) ?
                variableTypes.get(name).split("\\s+")[0] :
                                              "Unknown";
  }


  public void parseAssembly(String[] parameters) throws Exception {
    int lineNumber = Integer.parseInt(parameters[0]);
    String assemblyLine = parameters[1];
    if (assembly.containsKey(lineNumber))
      assembly.put(lineNumber, assembly.get(lineNumber) + System.lineSeparator() + assemblyLine);
    else assembly.put(lineNumber, assemblyLine);
  }

}
