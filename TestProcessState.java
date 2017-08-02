import java.util.*;

public class TestProcessState {

  public static String runTest() {
    String header = "TestProcessState results:" + System.lineSeparator();
    String errors = "";

    // Simulate the following run:
    // Stack & variables             Line   Asm    Registers
    // ------------------------------------------------------------------
    // main:x1                       1      asm1   jax=10, jbx=11, jcx=12
    // main:x1, alpha:y2             5      asm2   jax=10, jbx=21, jcx=12
    // main:x1, alpha:y2, beta:z3    9      asm3   jax=10, jbx=21, jcx=22
    // main:x1, alpha:y2             6      asm4   jax=20, jbx=21, jcx=22
    // main:x2                       2      asm5   jax=30, jbx=21, jcx=22
    //
    // Go forwards and backwards and make sure the state is correct all the time
    // NOTE WELL: It will be necessary to build two sets of deltas: one for forward, one for reverse

    VariableDelta x1 = new VariableDelta("int", "main", "x", "1", 500);
    VariableDelta y = new VariableDelta("int", "alpha", "y", "2", 600);
    VariableDelta z = new VariableDelta("int", "beta", "z", "3", 700);
    VariableDelta x2 = new VariableDelta("int", "main", "x2", "1", 500);

    ActivationRecord main = new ActivationRecord("main");
    ActivationRecord alpha = new ActivationRecord("alpha");
    ActivationRecord beta = new ActivationRecord("beta");

    TreeMap<String, String> delta1regs = new TreeMap<>();
    delta1regs.put("jax", "10");
    delta1regs.put("jbx", "11");
    delta1regs.put("jcx", "12");
    TreeMap<String, VariableDelta> delta1vars = new TreeMap<>();
    delta1vars.put("main,x", x1);
    List<ActivationRecord> delta1stack = new ArrayList<>();
    delta1stack.add(main);
    ProcessState delta1 = new ProcessState(1, delta1regs, delta1vars, delta1stack);

    TreeMap<String, String> delta2regs = new TreeMap<>();
    delta2regs.put("jbx", "21");
    TreeMap<String, VariableDelta> delta2vars = new TreeMap<>();
    delta2vars.put("alpha,y", y);
    List<ActivationRecord> delta2stack = new ArrayList<>();
    delta2stack.add(main);
    delta2stack.add(alpha);
    ProcessState delta2 = new ProcessState(5, delta2regs, delta2vars, delta2stack);

    TreeMap<String, String> delta3regs = new TreeMap<>();
    delta3regs.put("jcx", "22");
    TreeMap<String, VariableDelta> delta3vars = new TreeMap<>();
    delta3vars.put("beta,z", z);
    List<ActivationRecord> delta3stack = new ArrayList<>();
    delta3stack.add(main);
    delta3stack.add(alpha);
    delta3stack.add(beta);
    ProcessState delta3 = new ProcessState(9, delta3regs, delta3vars, delta3stack);

    TreeMap<String, String> delta4regs = new TreeMap<>();
    delta4regs.put("jax", "20");
    TreeMap<String, VariableDelta> delta4vars = new TreeMap<>();
    List<ActivationRecord> delta4stack = new ArrayList<>();
    delta4stack.add(main);
    delta4stack.add(alpha);
    ProcessState delta4 = new ProcessState(6, delta4regs, delta4vars, delta4stack);

    TreeMap<String, String> delta5regs = new TreeMap<>();
    delta5regs.put("jax", "30");
    TreeMap<String, VariableDelta> delta5vars = new TreeMap<>();
    delta5vars.put("main,x", x2);
    List<ActivationRecord> delta5stack = new ArrayList<>();
    delta5stack.add(main);
    ProcessState delta5 = new ProcessState(2, delta5regs, delta5vars, delta5stack);

    TreeMap<String, VariableDelta> delta4Rvars = new TreeMap<>();
    delta4Rvars.put("alpha,y", y);
    ProcessState delta4R = new ProcessState(6, delta4regs, delta4Rvars, delta4stack);

    TreeMap<String, VariableDelta> delta3Rvars = new TreeMap<>();
    TreeMap<String, String> delta3Rregs = new TreeMap<>();
    delta3Rregs.put("jax", "10");
    delta3Rvars.put("beta,z", z);
    ProcessState delta3R = new ProcessState(9, delta3Rregs, delta3Rvars, delta3stack);

    TreeMap<String, VariableDelta> delta2Rvars = new TreeMap<>();
    TreeMap<String, String> delta2Rregs = new TreeMap<>();
    delta2Rregs.put("jcx", "12");
    ProcessState delta2R = new ProcessState(5, delta2Rregs, delta2Rvars, delta2stack);

    TreeMap<String, VariableDelta> delta1Rvars = new TreeMap<>();
    ProcessState delta1R = new ProcessState(1, delta1regs, delta1Rvars, delta1stack);

    // Construction complete, now start applying deltas and checking the results
    ProcessState current = new ProcessState();

    TreeMap<String, String> checkRegisters = new TreeMap<>();
    TreeMap<String, VariableDelta> checkVariables = new TreeMap<>();
    List<ActivationRecord> checkStack = new ArrayList<>();

    ProcessState.applyDelta(current, delta1);
    checkRegisters.put("jax", "10");
    checkRegisters.put("jbx", "11");
    checkRegisters.put("jcx", "12");
    checkVariables.put("main,x", new VariableDelta(x1));
    checkStack.add(new ActivationRecord("main"));
    errors += verifyProcessState(current, 1, checkRegisters, checkVariables, checkStack);

    ProcessState.applyDelta(current, delta2);
    checkRegisters.put("jbx", "21");
    checkVariables.put("alpha,y", new VariableDelta(y));
    checkStack.add(new ActivationRecord("alpha"));
    errors += verifyProcessState(current, 5, checkRegisters, checkVariables, checkStack);

    ProcessState.applyDelta(current, delta3);
    checkRegisters.put("jcx", "22");
    checkVariables.put("beta,z", new VariableDelta(z));
    checkStack.add(new ActivationRecord("beta"));
    errors += verifyProcessState(current, 9, checkRegisters, checkVariables, checkStack);

    ProcessState.applyDelta(current, delta4);
    checkRegisters.put("jax", "20");
    checkVariables.remove("beta,z");
    checkStack.remove(checkStack.size()-1);
    errors += verifyProcessState(current, 6, checkRegisters, checkVariables, checkStack);

    ProcessState.applyDelta(current, delta5);
    checkRegisters.put("jax", "30");
    checkVariables.remove("alpha,y");
    checkVariables.put("main,x", new VariableDelta(x2));
    checkStack.remove(checkStack.size()-1);
    errors += verifyProcessState(current, 2, checkRegisters, checkVariables, checkStack);

    // Now go backwards
    ProcessState.applyDelta(current, delta4R);
    checkRegisters.put("jax", "20");
    checkVariables.put("alpha,y", new VariableDelta(y));
    checkStack.add(new ActivationRecord("alpha"));
    errors += verifyProcessState(current, 6, checkRegisters, checkVariables, checkStack);

    ProcessState.applyDelta(current, delta3R);
    checkRegisters.put("jax", "10");
    checkRegisters.put("jcx", "22");
    checkVariables.put("beta,z", new VariableDelta(z));
    checkStack.add(new ActivationRecord("beta"));
    errors += verifyProcessState(current, 9, checkRegisters, checkVariables, checkStack);

    ProcessState.applyDelta(current, delta2R);
    checkRegisters.put("jbx", "21");
    checkRegisters.put("jcx", "12");
    checkVariables.remove("beta,z");
    checkStack.remove(checkStack.size()-1);
    errors += verifyProcessState(current, 5, checkRegisters, checkVariables, checkStack);

    ProcessState.applyDelta(current, delta1R);
    checkRegisters.put("jbx", "11");
    checkVariables.remove("alpha,y");
    checkStack.remove(checkStack.size()-1);
    errors += verifyProcessState(current, 1, checkRegisters, checkVariables, checkStack);

    if (errors.isEmpty()) errors = "  All tests passed!" + System.lineSeparator();
    return (header + errors);
  } // runTest()


  static String verifyProcessState(
    ProcessState state,
    int sourceLine,
    TreeMap<String, String> registers,
    TreeMap<String, VariableDelta> variables,
    List<ActivationRecord> stack)
  {
    String errors = "";
    if (sourceLine != state.sourceLine)
      errors += "  SRC ERROR: [" + sourceLine + "] and [" + state.sourceLine +
                "] are different." + System.lineSeparator();
    if (!registers.equals(state.registers))
      errors += "  REGISTER ERROR: [" + registers + "] and [" + state.registers +
                "] are different." + System.lineSeparator();
    if (!variables.equals(state.variables))
      errors += "  VARIABLE ERROR: [" + variables + "] and [" + state.variables +
                "] are different." + System.lineSeparator();
    if (!stack.equals(state.stack))
      errors += "  STACK ERROR: [" + stack + "] and [" + state.stack +
                "] are different." + System.lineSeparator();
    return errors;
  } // verifyProcessState()

} // TestProcessState class
