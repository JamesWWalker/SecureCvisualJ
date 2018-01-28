import java.util.*;

public class TestVariableRepresentation {

  public static String runTest() {
    String header = "TestVariableRepresentation results:" + System.lineSeparator();
    String errors = "";
    String result = "";
    
    VariableRepresentation rep = new VariableRepresentation("FAFB");
    
    if (!rep.getValue(true).equals("FAFB"))
      errors += "  VALUE ERROR 1: Value is [" + rep.getValue(true) + "]" + System.lineSeparator();
    
    if (!rep.getValue(false).equals("FBFA"))
      errors += "  VALUE ERROR 2: Value is [" + rep.getValue(false) + "]" + System.lineSeparator();
    
    // CHAR
    result = rep.clampValue(VariableType.UNSIGNED_CHAR, "-50");
    if (!result.equals("255"))
      errors += "  CLAMP ERROR 3: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.UNSIGNED_CHAR, "300");
    if (!result.equals("0"))
      errors += "  CLAMP ERROR 4: Clamp is [" + result + "]" + System.lineSeparator();
      
    result = rep.clampValue(VariableType.SIGNED_CHAR, "-300");
    if (!result.equals("127"))
      errors += "  CLAMP ERROR 5: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.SIGNED_CHAR, "300");
    if (!result.equals("-128"))
      errors += "  CLAMP ERROR 6: Clamp is [" + result + "]" + System.lineSeparator();
      
    // SHORT
    result = rep.clampValue(VariableType.UNSIGNED_SHORT, "-50");
    if (!result.equals("65535"))
      errors += "  CLAMP ERROR 7: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.UNSIGNED_SHORT, "70000");
    if (!result.equals("0"))
      errors += "  CLAMP ERROR 8: Clamp is [" + result + "]" + System.lineSeparator();
    
    result = rep.clampValue(VariableType.SIGNED_SHORT, "-40000");
    if (!result.equals("32767"))
      errors += "  CLAMP ERROR 9: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.SIGNED_SHORT, "40000");
    if (!result.equals("-32768"))
      errors += "  CLAMP ERROR 10: Clamp is [" + result + "]" + System.lineSeparator();
      
    // INT
    result = rep.clampValue(VariableType.UNSIGNED_INT, "-50");
    if (!result.equals("4294967295"))
      errors += "  CLAMP ERROR 11: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.UNSIGNED_INT, "5294967295");
    if (!result.equals("0"))
      errors += "  CLAMP ERROR 12: Clamp is [" + result + "]" + System.lineSeparator();
    
    result = rep.clampValue(VariableType.SIGNED_INT, "-3147483648");
    if (!result.equals("2147483647"))
      errors += "  CLAMP ERROR 13: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.SIGNED_INT, "3147483648");
    if (!result.equals("-2147483648"))
      errors += "  CLAMP ERROR 14: Clamp is [" + result + "]" + System.lineSeparator();
      
    // LONG
    UIUtils.architecture = 64;
    
    result = rep.clampValue(VariableType.UNSIGNED_LONG, "-50");
    if (!result.equals("18446744073709551615"))
      errors += "  CLAMP ERROR 15: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.UNSIGNED_LONG, "28446744073709551615");
    if (!result.equals("0"))
      errors += "  CLAMP ERROR 16: Clamp is [" + result + "]" + System.lineSeparator();
    
    result = rep.clampValue(VariableType.SIGNED_LONG, "-9923372036854775807");
    if (!result.equals("9223372036854775807"))
      errors += "  CLAMP ERROR 17: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.SIGNED_LONG, "9923372036854775807");
    if (!result.equals("-9223372036854775808"))
      errors += "  CLAMP ERROR 18: Clamp is [" + result + "]" + System.lineSeparator();
    
    UIUtils.architecture = 32;
    
    result = rep.clampValue(VariableType.UNSIGNED_LONG, "-50");
    if (!result.equals("4294967295"))
      errors += "  CLAMP ERROR 19: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.UNSIGNED_LONG, "5294967295");
    if (!result.equals("0"))
      errors += "  CLAMP ERROR 20: Clamp is [" + result + "]" + System.lineSeparator();
    
    result = rep.clampValue(VariableType.SIGNED_LONG, "-3147483648");
    if (!result.equals("2147483647"))
      errors += "  CLAMP ERROR 21: Clamp is [" + result + "]" + System.lineSeparator();
    result = rep.clampValue(VariableType.SIGNED_LONG, "3147483648");
    if (!result.equals("-2147483648"))
      errors += "  CLAMP ERROR 22: Clamp is [" + result + "]" + System.lineSeparator();
      
    // Check hex values we get back when feeding in decimal values of various variable types
    // CHAR
    rep.setValueFromDecimal("201", VariableType.UNSIGNED_CHAR);
    result = rep.getValue(true);
    if (!result.equals("C9"))
      errors += "  HEX ERROR 23: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("C9"))
      errors += "  HEX ERROR 24: Hex is [" + result + "]" + System.lineSeparator();
    
    rep.setValueFromDecimal("-99", VariableType.SIGNED_CHAR);
    result = rep.getValue(true);
    if (!result.equals("9D"))
      errors += "  HEX ERROR 25: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("9D"))
      errors += "  HEX ERROR 26: Hex is [" + result + "]" + System.lineSeparator();

    // SHORT
    rep.setValueFromDecimal("201", VariableType.UNSIGNED_SHORT);
    result = rep.getValue(true);
    if (!result.equals("00C9"))
      errors += "  HEX ERROR 27: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("C900"))
      errors += "  HEX ERROR 28: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("65246", VariableType.UNSIGNED_SHORT);
    result = rep.getValue(true);
    if (!result.equals("FEDE"))
      errors += "  HEX ERROR 29: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("DEFE"))
      errors += "  HEX ERROR 30: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("-99", VariableType.SIGNED_SHORT);
    result = rep.getValue(true);
    if (!result.equals("FF9D"))
      errors += "  HEX ERROR 31: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("9DFF"))
      errors += "  HEX ERROR 32: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("-32111", VariableType.SIGNED_SHORT);
    result = rep.getValue(true);
    if (!result.equals("8291"))
      errors += "  HEX ERROR 33: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("9182"))
      errors += "  HEX ERROR 34: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("-32111", VariableType.SIGNED_SHORT);
    result = rep.getValue(true);
    if (!result.equals("8291"))
      errors += "  HEX ERROR 33: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("9182"))
      errors += "  HEX ERROR 34: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("129000", VariableType.SIGNED_SHORT);
    result = rep.getValue(true);
    if (!result.equals("F7E8"))
      errors += "  HEX ERROR 35: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("E8F7"))
      errors += "  HEX ERROR 36: Hex is [" + result + "]" + System.lineSeparator();
      
    // INTEGER
    rep.setValueFromDecimal("201", VariableType.UNSIGNED_INT);
    result = rep.getValue(true);
    if (!result.equals("000000C9"))
      errors += "  HEX ERROR 37: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("C9000000"))
      errors += "  HEX ERROR 38: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("4100050301", VariableType.UNSIGNED_INT);
    result = rep.getValue(true);
    if (!result.equals("F461CD7D"))
      errors += "  HEX ERROR 39: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("7DCD61F4"))
      errors += "  HEX ERROR 40: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("-99", VariableType.SIGNED_INT);
    result = rep.getValue(true);
    if (!result.equals("FFFFFF9D"))
      errors += "  HEX ERROR 41: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("9DFFFFFF"))
      errors += "  HEX ERROR 42: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("-2100050301", VariableType.SIGNED_INT);
    result = rep.getValue(true);
    if (!result.equals("82D3C683"))
      errors += "  HEX ERROR 43: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("83C6D382"))
      errors += "  HEX ERROR 44: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("-6100050301", VariableType.SIGNED_INT);
    result = rep.getValue(true);
    if (!result.equals("94689E83"))
      errors += "  HEX ERROR 45: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("839E6894"))
      errors += "  HEX ERROR 46: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("7100050301", VariableType.SIGNED_INT);
    result = rep.getValue(true);
    if (!result.equals("A7322B7D"))
      errors += "  HEX ERROR 47: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("7D2B32A7"))
      errors += "  HEX ERROR 48: Hex is [" + result + "]" + System.lineSeparator();
      
    // LONGS
      
    // I'm going to skip 64-bit longs because my own code is the only way I know of
    // to verify values that large (except the extremely tedious process of converting
    // by hand, not happening), which makes testing kind of pointless.
    
    // I will test longs on 32-bit architecture however to make sure they're
    // behaving the same as ints in that case.
    UIUtils.architecture = 32;
    
    rep.setValueFromDecimal("201", VariableType.UNSIGNED_LONG);
    result = rep.getValue(true);
    if (!result.equals("000000C9"))
      errors += "  HEX ERROR 49: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("C9000000"))
      errors += "  HEX ERROR 50: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("4100050301", VariableType.UNSIGNED_LONG);
    result = rep.getValue(true);
    if (!result.equals("F461CD7D"))
      errors += "  HEX ERROR 51: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("7DCD61F4"))
      errors += "  HEX ERROR 52: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("-99", VariableType.SIGNED_LONG);
    result = rep.getValue(true);
    if (!result.equals("FFFFFF9D"))
      errors += "  HEX ERROR 53: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("9DFFFFFF"))
      errors += "  HEX ERROR 54: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("-2100050301", VariableType.SIGNED_LONG);
    result = rep.getValue(true);
    if (!result.equals("82D3C683"))
      errors += "  HEX ERROR 55: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("83C6D382"))
      errors += "  HEX ERROR 56: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("-6100050301", VariableType.SIGNED_LONG);
    result = rep.getValue(true);
    if (!result.equals("94689E83"))
      errors += "  HEX ERROR 57: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("839E6894"))
      errors += "  HEX ERROR 58: Hex is [" + result + "]" + System.lineSeparator();
      
    rep.setValueFromDecimal("7100050301", VariableType.SIGNED_LONG);
    result = rep.getValue(true);
    if (!result.equals("A7322B7D"))
      errors += "  HEX ERROR 59: Hex is [" + result + "]" + System.lineSeparator();
    result = rep.getValue(false);
    if (!result.equals("7D2B32A7"))
      errors += "  HEX ERROR 60: Hex is [" + result + "]" + System.lineSeparator();
      
    // Actually, I'll do a LITTLE bit of 64-bit testing just to make sure the hex
    // is the right length, but I'll be taking its word for it on the values.
    UIUtils.architecture = 64;
    
    rep.setValueFromDecimal("18416744073109551616", VariableType.UNSIGNED_LONG);
    result = rep.getValue(true);
    if (!result.equals("FF956B288CF9BA00"))
      errors += "  HEX ERROR 61: Hex is [" + result + "]" + System.lineSeparator();
      
    // Make sure we get the same decimal number back out
    result = rep.convertHexToDecimal(result, VariableType.UNSIGNED_LONG);
    if (!result.equals("18416744073109551616"))
      errors += "  DEC ERROR 62: Decimal is [" + result + "]" + System.lineSeparator();

    if (errors.isEmpty()) errors = "  All tests passed!" + System.lineSeparator();
    return (header + errors);
    
  } // runTest()


} // TestVariableRepresentation class
