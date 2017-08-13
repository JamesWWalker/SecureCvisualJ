import java.util.*;

public class TestVariable {

  public static String runTest() {
    String header = "TestVariable results:" + System.lineSeparator();
    String errors = "";

    // var1 starts at { 1, 2, 3}, then { 9, 8, 7 }, then reverts
    TreeMap<String, VariableDelta> variables = new TreeMap<>();
    variables.put("main,var1", new VariableDelta("int [3]", "main", "var1", "<multielement>", 500));
    variables.put("main,var1:[0]", new VariableDelta("int", "main", "var1:[0]", "1", 500));
    variables.put("main,var1:[1]", new VariableDelta("int", "main", "var1:[1]", "2", 504));
    variables.put("main,var1:[2]", new VariableDelta("int", "main", "var1:[2]", "3", 508));

    // first=old, second=new
    ArrayList<Pair<VariableDelta, VariableDelta>> deltaSequence = new ArrayList<>();
    deltaSequence.add(new Pair<>(
      new VariableDelta(variables.get("main,var1:[0]")),
      new VariableDelta("int", "main", "var1:[0]", "9", 500)));
    deltaSequence.add(new Pair<>(
      new VariableDelta(variables.get("main,var1:[1]")),
      new VariableDelta("int", "main", "var1:[1]", "8", 504)));
    deltaSequence.add(new Pair<>(
      new VariableDelta(variables.get("main,var1:[2]")),
      new VariableDelta("int", "main", "var1:[2]", "7", 508)));

    // Construction done, now apply changes and check them each in turn
    VariableDelta.applyDelta(variables.get(deltaSequence.get(0).getSecond().getKey()),
      deltaSequence.get(0).getSecond());
    errors += verifyVariableState(
      variables.get(deltaSequence.get(0).getSecond().getKey()),
      "int", "main", "var1:[0]", "9", 500);

    VariableDelta.applyDelta(variables.get(deltaSequence.get(1).getSecond().getKey()),
      deltaSequence.get(1).getSecond());
    errors += verifyVariableState(
      variables.get(deltaSequence.get(1).getSecond().getKey()),
      "int", "main", "var1:[1]", "8", 504);

    VariableDelta.applyDelta(variables.get(deltaSequence.get(2).getSecond().getKey()),
      deltaSequence.get(2).getSecond());
    errors += verifyVariableState(
      variables.get(deltaSequence.get(2).getSecond().getKey()),
      "int", "main", "var1:[2]", "7", 508);

    // Now make sure we can reverse the changes
    VariableDelta.applyDelta(variables.get(deltaSequence.get(2).getFirst().getKey()),
      deltaSequence.get(2).getFirst());
    errors += verifyVariableState(
      variables.get(deltaSequence.get(2).getFirst().getKey()),
      "int", "main", "var1:[2]", "3", 508);

    VariableDelta.applyDelta(variables.get(deltaSequence.get(1).getFirst().getKey()),
      deltaSequence.get(1).getFirst());
    errors += verifyVariableState(
      variables.get(deltaSequence.get(1).getFirst().getKey()),
      "int", "main", "var1:[1]", "2", 504);

    VariableDelta.applyDelta(variables.get(deltaSequence.get(0).getFirst().getKey()),
      deltaSequence.get(0).getFirst());
    errors += verifyVariableState(
      variables.get(deltaSequence.get(0).getFirst().getKey()),
      "int", "main", "var1:[0]", "1", 500);

    if (errors.isEmpty()) errors = "  All tests passed!" + System.lineSeparator();
    return (header + errors);
  } // runTest()


  private static String verifyVariableState(
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

} // TestVariable class
