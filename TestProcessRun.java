import java.util.*;

public class TestProcessRun {

  public static String runTest() {
    String header = "TestProcessRun results:" + System.lineSeparator();
    String errors = "";

    try {

      ProcessRun run = new ProcessRun("./simple-output.txt");

      // Easy check: Current source line should be 10
      if (run.getSourceLine() != 10) errors += "  LINE ERROR 0: [10] and [" + run.getSourceLine() +
                                               "] are not equal." + System.lineSeparator();

      // Check starting stack contents for different detail levels
      List<ActivationRecord> checkStack = new ArrayList<>();
      checkStack.add(new ActivationRecord("simple", "main"));

      List<ActivationRecord> stack = run.getStack(DetailLevel.NOVICE);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 1: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      checkStack.add(0, new ActivationRecord("libc.so.6", "__libc_start_main(main=(simple"));
      checkStack.add(0, new ActivationRecord("simple", "_start"));

      stack = run.getStack(DetailLevel.ADVANCED);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 2: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      // Check a couple of registers at random
      TreeMap<String, String> registers = run.getRegisters(DetailLevel.INTERMEDIATE);
      if (!registers.get("r11").equals("0x00007ffff7a2d740"))
        errors += "  REGISTER ERROR 3: [r11] value is [" + registers.get("r11") + "]" + System.lineSeparator();
      if (!registers.get("r11").equals("0x00007ffff7a2d740"))
        errors += "  REGISTER ERROR 4: [rflags] value is [" + registers.get("rflags") + "]" + System.lineSeparator();

      // Check assembly
      String checkAssembly =
        "0x400579 <+8>:  movl   $0x0, %eax" + System.lineSeparator() +
        "0x40057e <+13>: callq  0x400566                  ; assignSize at simple.c:5" + System.lineSeparator() +
        "0x400583 <+18>: movq   %rax, %rdi" + System.lineSeparator() +
        "0x400586 <+21>: callq  0x400450                  ; ??? + 48" + System.lineSeparator() +
        "0x40058b <+26>: movq   %rax, -0x8(%rbp)";
      if (!checkAssembly.equals(run.getAssembly()))
        errors += "  ASSEMBLY ERROR 5: [" + checkAssembly + "]" + System.lineSeparator() + "  and" +
                  System.lineSeparator() + "[" + run.getAssembly() + "] are not equal." + System.lineSeparator();

      // Check variable
      TreeMap<String, VariableDelta> variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,words"),
                                                  "char *",
                                                  "main",
                                                  "words",
                                                  "0x0000000000000000",
                                                  140737488346744L);

      // Go to next event and check that things were updated correctly
      run.next();

      if (run.getSourceLine() != 6) errors += "  LINE ERROR 6: [6] and [" + run.getSourceLine() +
                                               "] are not equal." + System.lineSeparator();

      checkStack.clear();
      checkStack.add(new ActivationRecord("simple", "main"));
      checkStack.add(new ActivationRecord("simple", "assignSize"));

      stack = run.getStack(DetailLevel.NOVICE);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 7: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      registers = run.getRegisters(DetailLevel.INTERMEDIATE);
      if (!registers.get("rbp").equals("0x00007fffffffde60"))
        errors += "  REGISTER ERROR 8: [rbp] value is [" + registers.get("rbp") +
                  "] which is wrong." + System.lineSeparator();

      checkAssembly = "0x40056a <+4>: movl   $0xe, %eax";
      if (!checkAssembly.equals(run.getAssembly()))
        errors += "  ASSEMBLY ERROR 9: [" + checkAssembly + "]" + System.lineSeparator() + "  and" +
                  System.lineSeparator() + "[" + run.getAssembly() + "] are not equal." + System.lineSeparator();

      variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,words"),
                                                  "char *",
                                                  "main",
                                                  "words",
                                                  "0x0000000000000000",
                                                  140737488346744L);

      // Jump ahead twice -- assignSize should be popped off the stack; also check a register
      run.next();
      run.next();

      checkStack.clear();
      checkStack.add(new ActivationRecord("simple", "main"));

      stack = run.getStack(DetailLevel.NOVICE);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 10: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      registers = run.getRegisters(DetailLevel.INTERMEDIATE);
      if (!registers.get("rip").equals("0x0000000000400583"))
        errors += "  REGISTER ERROR 11: [rip] value is [" + registers.get("rip") +
                  "] which is wrong." + System.lineSeparator();

      // Next event: value of 'words' should have changed
      run.next(); // at index 4

      variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,words"),
                                                  "char *",
                                                  "main",
                                                  "words",
                                                  "\"\"",
                                                  140737488346744L);
      if (variables.get("main,words").pointsTo != 6299664L)
      errors += "  VARIABLE ERROR 12: words pointsTo is [" + variables.get("main,words").pointsTo +
               "] which is wrong." + System.lineSeparator();

      // Jump to event 6: value of 'words' should have changed again
      run.jumpToEvent(6);

      variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,words"),
                                                  "char *",
                                                  "main",
                                                  "words",
                                                  "\"Rappan Athuk\\n\"",
                                                  140737488346744L);

      // Jump to the end, check rcx and rax
      run.jumpToEnd();

      registers = run.getRegisters(DetailLevel.INTERMEDIATE);
      if (!registers.get("rcx").equals("0x000000007ffffff2"))
        errors += "  REGISTER ERROR 13: [rcx] value is [" + registers.get("rcx") +
                  "] which is wrong." + System.lineSeparator();

      stack = run.getStack(DetailLevel.NOVICE);
      if (!stack.isEmpty()) errors += "  STACK ERROR 14: Stack should be empty but isn't:" +
                                      stack + System.lineSeparator();

      // Go back one: Now main should be on the stack
      run.previous();

      stack = run.getStack(DetailLevel.NOVICE);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 15: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      // Jump back to index 4: words should be a blank string
      run.jumpToEvent(4);

      variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,words"),
                                                  "char *",
                                                  "main",
                                                  "words",
                                                  "\"\"",
                                                  140737488346744L);
      if (variables.get("main,words").pointsTo != 6299664L)
      errors += "  VARIABLE ERROR 16: words pointsTo is [" + variables.get("main,words").pointsTo +
                "] which is wrong." + System.lineSeparator();

      // Jump all the way back to event 1: assignSize() should be on the stack.
      run.previous();
      run.previous();
      run.previous();

      checkStack.add(new ActivationRecord("simple", "assignSize"));
      stack = run.getStack(DetailLevel.NOVICE);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 16: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      // Jump to beginning, check value of rax and 'words'
      run.jumpToBeginning();

      registers = run.getRegisters(DetailLevel.INTERMEDIATE);
      if (!registers.get("rax").equals("0x0000000000400571"))
        errors += "  REGISTER ERROR 17: [rax] value is [" + registers.get("rax") +
                  "] which is wrong." + System.lineSeparator();

      variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,words"),
                                                  "char *",
                                                  "main",
                                                  "words",
                                                  "0x0000000000000000",
                                                  140737488346744L);

      // TODO: Do a test where the function that gets called also has a variable.

    } catch (Exception ex) {
      System.err.println("Exception thrown during test:");
      ex.printStackTrace();
    }

    if (errors.isEmpty()) errors = "  All tests passed!" + System.lineSeparator();
    return (header + errors);
  } // runTest()


  static String verifyVariableState(
    VariableDelta var,
    String type,
    String scope,
    String name,
    String value,
    long address) throws Exception
  {
    String errors = "";
    if (!var.type.equals(type))
      errors += "  TYPE ERROR: [" + type + "] and [" + var.type +
                "] are different." + System.lineSeparator();
    if (!var.scope.equals(scope))
      errors += "  SCOPE ERROR: [" + scope + "] and [" + var.scope +
                "] are different." + System.lineSeparator();
    if (!var.name.equals(name))
      errors += "  NAME ERROR: [" + name + "] and [" + var.name +
                "] are different." + System.lineSeparator();
    if (!var.value.equals(value))
      errors += "  VALUE ERROR: [" + value + "] and [" + var.value +
                "] are different." + System.lineSeparator();
    if (var.address != address)
      errors += "  ADDRESS ERROR: [" + address + "] and [" + var.address +
                "] are different." + System.lineSeparator();
    return errors;
  } // verifyVariableState()

} // TestProcessRun class
