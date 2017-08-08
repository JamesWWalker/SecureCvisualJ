
public class TestMaster {

  public static void main(String[] args) {

    String report = "";
    report += TestVariable.runTest();
    report += TestProcessState.runTest();
    report += TestProcessRun.runTest();
    report += TestVariableRepresentation.runTest();
    // TODO: Add tests here

    System.out.println(report);
  }

}
