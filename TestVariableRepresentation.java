import java.util.*;

public class TestVariableRepresentation {

  public static String runTest() {
    String header = "TestVariableRepresentation results:" + System.lineSeparator();
    String errors = "";
    
    // First, simply check that inputted values are converted to correct hex values

    // shorts
    VariableRepresentation rep = new VariableRepresentation(VariableType.SIGNED_SHORT, "-5", true);
    if (!rep.getValue().equals("0xFFFB"))
      errors += "  VALUE ERROR 1: Value is [" + rep.getValue() + "]" + System.lineSeparator();

    rep.setValue("133", VariableType.UNSIGNED_SHORT);
    if (!rep.getValue().equals("0x0085"))
      errors += "  VALUE ERROR 2: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    rep.setValue("65000", VariableType.UNSIGNED_SHORT);
    if (!rep.getValue().equals("0xFDE8"))
      errors += "  VALUE ERROR 3: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // ints
    rep.setValue("-80000", VariableType.SIGNED_INT);
    if (!rep.getValue().equals("0xFFFEC780"))
      errors += "  VALUE ERROR 4: Value is [" + rep.getValue() + "]" + System.lineSeparator();

    rep.setValue("32767", VariableType.UNSIGNED_INT);
    if (!rep.getValue().equals("0x00007FFF"))
      errors += "  VALUE ERROR 5: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    rep.setValue("4294967200", VariableType.UNSIGNED_INT);
    if (!rep.getValue().equals("0xFFFFFFA0"))
      errors += "  VALUE ERROR 6: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // longs
    rep.setValue("-789123456789", VariableType.SIGNED_LONG);
    if (!rep.getValue().equals("0xFFFFFF484493A0EB"))
      errors += "  VALUE ERROR 7: Value is [" + rep.getValue() + "]" + System.lineSeparator();

    rep.setValue("74175", VariableType.UNSIGNED_LONG);
    if (!rep.getValue().equals("0x00000000000121BF"))
      errors += "  VALUE ERROR 8: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    rep.setValue("9123372036854775807", VariableType.UNSIGNED_LONG);
    if (!rep.getValue().equals("0x7E9CBA87A275FFFF"))
      errors += "  VALUE ERROR 9: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // strings
    rep.setValue("Sonic, Tails, & Knuckles", VariableType.STRING);
    if (!rep.getValue().equals("Sonic, Tails, & Knuckles"))
      errors += "  VALUE ERROR 10: Value is [" + rep.getValue() + "]" + System.lineSeparator();
    
    rep.setValue("0xcafe", VariableType.STRING);
    if (!rep.getValue().equals("0xCAFE"))
      errors += "  VALUE ERROR 11: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // chars
    rep.setValue("'t'", VariableType.SIGNED_CHAR);
    if (!rep.getValue().equals("0x74"))
      errors += "  VALUE ERROR 12: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    rep.setValue("'\\x19'", VariableType.SIGNED_CHAR);
    if (!rep.getValue().equals("0x19"))
      errors += "  VALUE ERROR 13: Value is [" + rep.getValue() + "]" + System.lineSeparator();
    
    
    rep.setValue("'5'", VariableType.SIGNED_CHAR);
    if (!rep.getValue().equals("0x35"))
      errors += "  VALUE ERROR 14: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    // Now check conversions
    String conversion;
    
    rep.setValue("2345678901", VariableType.UNSIGNED_INT);
    if (!rep.getValue().equals("0x8BD03835"))
      errors += "  VALUE ERROR 15: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    conversion = rep.getDecimal(VariableType.UNSIGNED_INT, true);
    if (!conversion.equals("2345678901"))
      errors += "  VALUE ERROR 16: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getHex(VariableType.UNSIGNED_INT, true);
    if (!conversion.equals("0x8BD03835"))
      errors += "  VALUE ERROR 17: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getBinary(VariableType.UNSIGNED_INT, true);
    if (!conversion.equals("10001011110100000011100000110101"))
      errors += "  VALUE ERROR 18: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getDecimal(VariableType.UNSIGNED_INT, false);
    if (!conversion.equals("892915851"))
      errors += "  VALUE ERROR 19: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getHex(VariableType.UNSIGNED_INT, false);
    if (!conversion.equals("0x3538D08B"))
      errors += "  VALUE ERROR 20: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getBinary(VariableType.UNSIGNED_INT, false);
    if (!conversion.equals("00110101001110001101000010001011"))
      errors += "  VALUE ERROR 21: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getDecimal(VariableType.UNSIGNED_SHORT, true);
    if (!conversion.equals("14389"))
      errors += "  VALUE ERROR 22: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getHex(VariableType.UNSIGNED_SHORT, true);
    if (!conversion.equals("0x3835"))
      errors += "  VALUE ERROR 23: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getBinary(VariableType.UNSIGNED_SHORT, true);
    if (!conversion.equals("0011100000110101"))
      errors += "  VALUE ERROR 24: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getDecimal(VariableType.UNSIGNED_CHAR, true);
    if (!conversion.equals("53"))
      errors += "  VALUE ERROR 25: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getHex(VariableType.UNSIGNED_CHAR, true);
    if (!conversion.equals("0x35"))
      errors += "  VALUE ERROR 26: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getBinary(VariableType.UNSIGNED_CHAR, true);
    if (!conversion.equals("00110101"))
      errors += "  VALUE ERROR 27: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getDecimal(VariableType.SIGNED_SHORT, true);
    if (!conversion.equals("14389"))
      errors += "  VALUE ERROR 28: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getHex(VariableType.SIGNED_SHORT, true);
    if (!conversion.equals("0x3835"))
      errors += "  VALUE ERROR 29: Value is [" + conversion + "]" + System.lineSeparator();
      
    // Now let's try a big negative value.
    rep.isBigEndian = false;
    rep.setValue("-999999999", VariableType.SIGNED_LONG);
    if (!rep.getValue().equals("0xFFFFFFFFC4653601"))
      errors += "  VALUE ERROR 30: Value is [" + rep.getValue() + "]" + System.lineSeparator();
      
    conversion = rep.getDecimal(VariableType.SIGNED_LONG, false);
    if (!conversion.equals("-999999999"))
      errors += "  VALUE ERROR 31: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getDecimal(VariableType.UNSIGNED_LONG, false);
    if (!conversion.equals("18446744072709551617"))
      errors += "  VALUE ERROR 32: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getHex(VariableType.SIGNED_LONG, true);
    if (!conversion.equals("0x013665C4FFFFFFFF"))
      errors += "  VALUE ERROR 33: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getHex(VariableType.UNSIGNED_LONG, true);
    if (!conversion.equals("0x013665C4FFFFFFFF"))
      errors += "  VALUE ERROR 34: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getDecimal(VariableType.SIGNED_LONG, true);
    if (!conversion.equals("87369139563266047"))
      errors += "  VALUE ERROR 35: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getDecimal(VariableType.UNSIGNED_LONG, true);
    if (!conversion.equals("87369139563266047"))
      errors += "  VALUE ERROR 36: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getHex(VariableType.SIGNED_INT, true);
    if (!conversion.equals("0xFFFFFFFF"))
      errors += "  VALUE ERROR 37: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getDecimal(VariableType.UNSIGNED_INT, true);
    if (!conversion.equals("4294967295"))
      errors += "  VALUE ERROR 38: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getBinary(VariableType.SIGNED_INT, true);
    if (!conversion.equals("11111111111111111111111111111111"))
      errors += "  VALUE ERROR 39: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getDecimal(VariableType.SIGNED_INT, true);
    if (!conversion.equals("-1"))
      errors += "  VALUE ERROR 40: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getHex(VariableType.SIGNED_SHORT, false);
    if (!conversion.equals("0x3601"))
      errors += "  VALUE ERROR 41: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getDecimal(VariableType.SIGNED_SHORT, false);
    if (!conversion.equals("13825"))
      errors += "  VALUE ERROR 42: Value is [" + conversion + "]" + System.lineSeparator();
    
    // Check to make sure small values get padded out when converted to larger ones
    rep.isBigEndian = true;
    rep.setValue("'#'", VariableType.SIGNED_CHAR); // ASCII code 35
    if (!rep.getValue().equals("0x23"))
      errors += "  VALUE ERROR 43: Value is [" + rep.getValue() + "]" + System.lineSeparator();
    
    conversion = rep.getHex(VariableType.SIGNED_SHORT, true);
    if (!conversion.equals("0x0023"))
      errors += "  VALUE ERROR 44: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getBinary(VariableType.SIGNED_SHORT, true);
    if (!conversion.equals("0000000000100011"))
      errors += "  VALUE ERROR 45: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getHex(VariableType.SIGNED_INT, true);
    if (!conversion.equals("0x00000023"))
      errors += "  VALUE ERROR 46: Value is [" + conversion + "]" + System.lineSeparator();
      
    conversion = rep.getBinary(VariableType.SIGNED_INT, true);
    if (!conversion.equals("00000000000000000000000000100011"))
      errors += "  VALUE ERROR 47: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getHex(VariableType.SIGNED_LONG, true);
    if (!conversion.equals("0x0000000000000023"))
      errors += "  VALUE ERROR 48: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getBinary(VariableType.SIGNED_LONG, true);
    if (!conversion.equals("0000000000000000000000000000000000000000000000000000000000100011"))
      errors += "  VALUE ERROR 49: Value is [" + conversion + "]" + System.lineSeparator();
    
    conversion = rep.getDecimal(VariableType.UNSIGNED_LONG, true);
    if (!conversion.equals("35"))
      errors += "  VALUE ERROR 50: Value is [" + conversion + "]" + System.lineSeparator();
    
    // TODO: add address

    if (errors.isEmpty()) errors = "  All tests passed!" + System.lineSeparator();
    return (header + errors);
  } // runTest()


} // TestVariableRepresentation class
