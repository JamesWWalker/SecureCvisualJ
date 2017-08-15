import java.util.*;

public class TestProcessRun {

  public static String runTest() {
    String header = "TestProcessRun results:" + System.lineSeparator();
    String errors = "";

    try {

      ProcessRun run = new ProcessRun("./simple-output.vaccs", 3);
      ProcessRunFilter filter = new ProcessRunFilter();

      // Easy check: Current source line should be 10
      if (filter.getSourceLine(run) != 10) errors += "  LINE ERROR 0: [10] and [" +
                                                     filter.getSourceLine(run) +
                                                     "] are not equal." + System.lineSeparator();

      // Check starting stack contents for different detail levels
      List<ActivationRecord> checkStack = new ArrayList<>();
      checkStack.add(new ActivationRecord("simple", "main"));

      filter.setDetailLevel(DetailLevel.NOVICE);
      List<ActivationRecord> stack = filter.getStack(run);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 1: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      checkStack.add(0, new ActivationRecord("libc.so.6", "__libc_start_main(main=(simple"));
      checkStack.add(0, new ActivationRecord("simple", "_start"));

      filter.setDetailLevel(DetailLevel.ADVANCED);
      stack = filter.getStack(run);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 2: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      // Check a couple of registers at random
      filter.setDetailLevel(DetailLevel.INTERMEDIATE);
      TreeMap<String, String> registers = filter.getRegisters(run);
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
      if (!checkAssembly.equals(filter.getAssembly(run)))
        errors += "  ASSEMBLY ERROR 5: [" + checkAssembly + "]" + System.lineSeparator() + "  and" +
                  System.lineSeparator() + "[" + filter.getAssembly(run) + "] are not equal." + System.lineSeparator();

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

      if (filter.getSourceLine(run) != 6) errors += "  LINE ERROR 6: [6] and [" + filter.getSourceLine(run) +
                                               "] are not equal." + System.lineSeparator();

      checkStack.clear();
      checkStack.add(new ActivationRecord("simple", "main"));
      checkStack.add(new ActivationRecord("simple", "assignSize"));

      filter.setDetailLevel(DetailLevel.NOVICE);
      stack = filter.getStack(run);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 7: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      filter.setDetailLevel(DetailLevel.INTERMEDIATE);
      registers = filter.getRegisters(run);
      if (!registers.get("rbp").equals("0x00007fffffffde60"))
        errors += "  REGISTER ERROR 8: [rbp] value is [" + registers.get("rbp") +
                  "] which is wrong." + System.lineSeparator();

      checkAssembly = "0x40056a <+4>: movl   $0xe, %eax";
      if (!checkAssembly.equals(filter.getAssembly(run)))
        errors += "  ASSEMBLY ERROR 9: [" + checkAssembly + "]" + System.lineSeparator() + "  and" +
                  System.lineSeparator() + "[" + filter.getAssembly(run) + "] are not equal." + System.lineSeparator();

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

      filter.setDetailLevel(DetailLevel.NOVICE);
      stack = filter.getStack(run);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 10: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      filter.setDetailLevel(DetailLevel.INTERMEDIATE);
      registers = filter.getRegisters(run);
      if (!registers.get("rip").equals("0x0000000000400583"))
        errors += "  REGISTER ERROR 11: [rip] value is [" + registers.get("rip") +
                  "] which is wrong." + System.lineSeparator();

      // value of 'words' should have changed
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

      filter.setDetailLevel(DetailLevel.INTERMEDIATE);
      registers = filter.getRegisters(run);
      if (!registers.get("rcx").equals("0x000000007ffffff2"))
        errors += "  REGISTER ERROR 13: [rcx] value is [" + registers.get("rcx") +
                  "] which is wrong." + System.lineSeparator();

      filter.setDetailLevel(DetailLevel.NOVICE);
      stack = filter.getStack(run);
      if (!stack.isEmpty()) errors += "  STACK ERROR 14: Stack should be empty but isn't:" +
                                      stack + System.lineSeparator();

      // Go back one: Now main should be on the stack
      run.previous();

      filter.setDetailLevel(DetailLevel.NOVICE);
      stack = filter.getStack(run);
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
      filter.setDetailLevel(DetailLevel.NOVICE);
      stack = filter.getStack(run);
      if (!stack.equals(checkStack)) errors += "  STACK ERROR 17: [" + checkStack + "] and [" +
                                               stack + "] are not equal." + System.lineSeparator();

      // Jump to beginning, check value of rax and 'words'
      run.jumpToBeginning();

      filter.setDetailLevel(DetailLevel.INTERMEDIATE);
      registers = filter.getRegisters(run);
      if (!registers.get("rax").equals("0x0000000000400571"))
        errors += "  REGISTER ERROR 18: [rax] value is [" + registers.get("rax") +
                  "] which is wrong." + System.lineSeparator();

      variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,words"),
                                                  "char *",
                                                  "main",
                                                  "words",
                                                  "0x0000000000000000",
                                                  140737488346744L); /* */

      //////////////////////////////////////////////////////////////////////////////////////////////
      // Try a run with multiple variables to make sure it's handling them correctly
      //////////////////////////////////////////////////////////////////////////////////////////////

      run = new ProcessRun("./simple2-output.vaccs", 3);

      // Check variable
      variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,a"),
                                                  "int",
                                                  "main",
                                                  "a",
                                                  "32767",
                                                  140737488346740L);
      errors += verifyVariableState(variables.get("main,b"),
                                                  "int",
                                                  "main",
                                                  "b",
                                                  "0",
                                                  140737488346744L);
      errors += verifyVariableState(variables.get("main,c"),
                                                  "int",
                                                  "main",
                                                  "c",
                                                  "0",
                                                  140737488346748L);

      run.next();
      run.next();
      run.next();
      variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,a"),
                                                  "int",
                                                  "main",
                                                  "a",
                                                  "2",
                                                  140737488346740L);
      errors += verifyVariableState(variables.get("main,b"),
                                                  "int",
                                                  "main",
                                                  "b",
                                                  "8",
                                                  140737488346744L);
      errors += verifyVariableState(variables.get("subFunc,x"),
                                                  "int",
                                                  "subFunc",
                                                  "x",
                                                  "0",
                                                  140737488346708L);
      errors += verifyVariableState(variables.get("subFunc,y"),
                                                  "int",
                                                  "subFunc",
                                                  "y",
                                                  "0",
                                                  140737488346712L);
      errors += verifyVariableState(variables.get("subFunc,z"),
                                                  "int",
                                                  "subFunc",
                                                  "z",
                                                  "0",
                                                  140737488346716L);

      run.next();
      run.next();
      run.next();
      variables = run.getVariables();

      errors += verifyVariableState(variables.get("subFunc,z"),
                                                  "int",
                                                  "subFunc",
                                                  "z",
                                                  "7",
                                                  140737488346716L);

      run.next();
      run.next();
      variables = run.getVariables(); /* */

      if (variables.size() != 3)
      errors += "  VARIABLE ERROR 19: Should be [3] variables but there are [" + variables.size() +
                "]." + System.lineSeparator();
      errors += verifyVariableState(variables.get("main,c"),
                                                  "int",
                                                  "main",
                                                  "c",
                                                  "10",
                                                  140737488346748L);

      run.previous();
      run.previous();
      variables = run.getVariables();

      errors += verifyVariableState(variables.get("main,a"),
                                                  "int",
                                                  "main",
                                                  "a",
                                                  "2",
                                                  140737488346740L);
      errors += verifyVariableState(variables.get("main,b"),
                                                  "int",
                                                  "main",
                                                  "b",
                                                  "8",
                                                  140737488346744L);
      errors += verifyVariableState(variables.get("main,c"),
                                                  "int",
                                                  "main",
                                                  "c",
                                                  "0",
                                                  140737488346748L);
      errors += verifyVariableState(variables.get("subFunc,x"),
                                                  "int",
                                                  "subFunc",
                                                  "x",
                                                  "4",
                                                  140737488346708L);
      errors += verifyVariableState(variables.get("subFunc,y"),
                                                  "int",
                                                  "subFunc",
                                                  "y",
                                                  "3",
                                                  140737488346712L);
      errors += verifyVariableState(variables.get("subFunc,z"),
                                                  "int",
                                                  "subFunc",
                                                  "z",
                                                  "0",
                                                  140737488346716L);

      run.jumpToBeginning();
      variables = run.getVariables();
      errors += verifyVariableState(variables.get("main,a"),
                                                  "int",
                                                  "main",
                                                  "a",
                                                  "32767",
                                                  140737488346740L);
      errors += verifyVariableState(variables.get("main,b"),
                                                  "int",
                                                  "main",
                                                  "b",
                                                  "0",
                                                  140737488346744L);
      errors += verifyVariableState(variables.get("main,c"),
                                                  "int",
                                                  "main",
                                                  "c",
                                                  "0",
                                                  140737488346748L);
                                                  
      //////////////////////////////////////////////////////////////////////////////////////////////
      // Verify that sections filtering is working
      //////////////////////////////////////////////////////////////////////////////////////////////
      
      filter.setDetailLevel(DetailLevel.ADVANCED);
      ArrayList<ProgramSection> sections = filter.getSections(run);
      
      if (!sections.stream().anyMatch(s -> s.name.endsWith(".data")))
        errors += "  SECTION ERROR 1: .data section not found." + System.lineSeparator();
      if (!sections.stream().anyMatch(s -> s.name.endsWith(".rodata")))
        errors += "  SECTION ERROR 2: .rodata section not found." + System.lineSeparator();
      if (!sections.stream().anyMatch(s -> s.name.endsWith(".text")))
        errors += "  SECTION ERROR 3: .text section not found." + System.lineSeparator();
      if (sections.stream().anyMatch(s -> s.name.endsWith(".interp")))
        errors += "  SECTION ERROR 4: .interp section should not be present, but is." + System.lineSeparator();
      
      if (sections.size() != 3)
        errors += "  SECTION ERROR 5: Section size should be [3], but is [" + sections.size() +
                  "]." + System.lineSeparator();
        
      filter.setDetailLevel(DetailLevel.NOVICE);
      sections = filter.getSections(run);
      
      if (sections.size() > 0)
        errors += "  SECTION ERROR 6: Sections should be empty, but is size [" + sections.size() +
                  "]." + System.lineSeparator();
                  
      filter.setDetailLevel(DetailLevel.EXPERT);
      sections = filter.getSections(run);
      
      if (sections.size() != 26)
        errors += "  SECTION ERROR 7: Section size should be [26], but is [" + sections.size() +
                  "]." + System.lineSeparator();

    } catch (Exception ex) {
      System.err.println("Exception thrown during test:");
      ex.printStackTrace();
    }

    if (errors.isEmpty()) errors = "  All tests passed!" + System.lineSeparator();
    return (header + errors);
    
  } // runTest()


  private static String verifyVariableState(
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
