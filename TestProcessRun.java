import java.util.*;

public class TestProcessRun {

  public static String runTest() {
    String header = "TestProcessRun results:" + System.lineSeparator();
    String errors = "";

    ProcessRun run = new ProcessRun("./simple-output.txt");

    if (errors.isEmpty()) errors = "  All tests passed!" + System.lineSeparator();
    return (header + errors);
  } // runTest()


/*  static String verifyVariableState(
    VariableDelta var,
    String type,
    String scope,
    String name,
    String value,
    long address)
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
/* */
} // TestProcessRun class
