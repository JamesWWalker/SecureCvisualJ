import java.util.*;

public class TestVariableRepresentation {

  public static String runTest() {
    String header = "TestVariableRepresentation results:" + System.lineSeparator();
    String errors = "";
    
    // First, simply check that inputted values are converted to correct hex values

    // shorts
    VariableRepresentation rep = new VariableRepresentation(VariableType.SIGNED_SHORT, "-5");
    if (!rep.getValue().equals("0xFFFB"))
      errors += "  VALUE ERROR 1: Value is [" + rep.getValue() + "]" + System.lineSeparator();

    rep.type = VariableType.UNSIGNED_SHORT;
    rep.setValue("133");
    if (!rep.getValue().equals("0x0085"))
      errors += "  VALUE ERROR 2: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    rep.type = VariableType.UNSIGNED_SHORT;
    rep.setValue("65000");
    if (!rep.getValue().equals("0xFDE8"))
      errors += "  VALUE ERROR 3: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // ints
    rep.type = VariableType.SIGNED_INT;
    rep.setValue("-80000");
    if (!rep.getValue().equals("0xFFFEC780"))
      errors += "  VALUE ERROR 4: Value is [" + rep.getValue() + "]" + System.lineSeparator();

    rep.type = VariableType.UNSIGNED_INT;
    rep.setValue("32767");
    if (!rep.getValue().equals("0x00007FFF"))
      errors += "  VALUE ERROR 5: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    rep.type = VariableType.UNSIGNED_INT;
    rep.setValue("4294967200");
    if (!rep.getValue().equals("0xFFFFFFA0"))
      errors += "  VALUE ERROR 6: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // longs
    rep.type = VariableType.SIGNED_LONG;
    rep.setValue("-789123456789");
    if (!rep.getValue().equals("0xFFFFFF484493A0EB"))
      errors += "  VALUE ERROR 7: Value is [" + rep.getValue() + "]" + System.lineSeparator();

    rep.type = VariableType.UNSIGNED_LONG;
    rep.setValue("74175");
    if (!rep.getValue().equals("0x00000000000121BF"))
      errors += "  VALUE ERROR 8: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    rep.type = VariableType.UNSIGNED_LONG;
    rep.setValue("9123372036854775807");
    if (!rep.getValue().equals("0x7E9CBA87A275FFFF"))
      errors += "  VALUE ERROR 9: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // strings
    rep.type = VariableType.STRING;
    rep.setValue("Sonic, Tails, & Knuckles");
    if (!rep.getValue().equals("Sonic, Tails, & Knuckles"))
      errors += "  VALUE ERROR 10: Value is [" + rep.getValue() + "]" + System.lineSeparator();
    
    rep.type = VariableType.STRING;
    rep.setValue("0xCAFE");
    if (!rep.getValue().equals("0xCAFE"))
      errors += "  VALUE ERROR 11: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // chars
    rep.type = VariableType.SIGNED_CHAR;
    rep.setValue("'t'");
    if (!rep.getValue().equals("0x74"))
      errors += "  VALUE ERROR 12: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    rep.type = VariableType.SIGNED_CHAR;
    rep.setValue("'\\x19'");
    if (!rep.getValue().equals("0x19"))
      errors += "  VALUE ERROR 13: Value is [" + rep.getValue() + "]" + System.lineSeparator();
    
    
    rep.type = VariableType.SIGNED_CHAR;
    rep.setValue("'5'");
    if (!rep.getValue().equals("0x35"))
      errors += "  VALUE ERROR 14: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // TODO
    
    /*
0x00007fffffffdd6d: (char) x = 't'
0x00007fffffffdd6e: (char) y = '\x19'
0x00007fffffffdd6f: (char) z = '5'

*/

    if (errors.isEmpty()) errors = "  All tests passed!" + System.lineSeparator();
    return (header + errors);
  } // runTest()


} // TestVariableRepresentation class
