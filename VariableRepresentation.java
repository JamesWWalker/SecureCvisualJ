import java.math.*;
import java.util.*;

public class VariableRepresentation {

  String value;
  VariableType type;
  public boolean isBigEndian;
  public long address;
  
  
  public String getValue() { return value; }
  
  
  public void setValue(String valueIn, VariableType typeIn) {
    type = typeIn;
    if (valueIn.startsWith("0x") && type == VariableType.STRING)
      value = "0x" + valueIn.substring(2).toUpperCase();
    else if (!valueIn.startsWith("0x") && type != VariableType.STRING) {
      switch (type) {
        case SIGNED_CHAR:
        case UNSIGNED_CHAR:
          value = valueIn.replaceAll("'", "");
          if (value.startsWith("\\x")) value = value.substring(2);
          else value = Integer.toHexString((int)(value.charAt(0)));
          value = padWithZeroes(2, value);
          break;
        case SIGNED_SHORT:
          value = Integer.toHexString(Short.parseShort(valueIn) & 0xffff);
          value = padWithZeroes(4, value);
          break;
        case UNSIGNED_SHORT:
          value = Integer.toHexString(Integer.parseUnsignedInt(valueIn) & 0xffff);
          value = padWithZeroes(4, value);
          break;
        case SIGNED_INT:
          value = Integer.toHexString(Integer.parseInt(valueIn));
          value = padWithZeroes(8, value);
          break;
        case UNSIGNED_INT:
          value = Integer.toHexString(Integer.parseUnsignedInt(valueIn));
          value = padWithZeroes(8, value);
          break;
        case SIGNED_LONG:
          value = Long.toHexString(Long.parseLong(valueIn));
          value = padWithZeroes(16, value);
          break;
        case UNSIGNED_LONG:
          value = Long.toHexString(Long.parseUnsignedLong(valueIn));
          value = padWithZeroes(16, value);
          break;
        default:
          value = valueIn;
          break;
      }
      value = "0x" + value.toUpperCase();
    }
    else value = valueIn;
  }
  
  
  public VariableRepresentation(VariableType typeIn, String valueIn, boolean isBigEndianIn) {
    setValue(valueIn, typeIn);
    isBigEndian = isBigEndianIn;
  }
  
  
  public String getDecimal(VariableType convertTo, boolean convertToBigEndian) {
    if (type == VariableType.STRING || convertTo == VariableType.STRING) return value;
    String hex = getHex(convertTo, convertToBigEndian).substring(2);
    
    switch (convertTo) {
      case SIGNED_CHAR:
        return "" + (byte)(new BigInteger(hex, 16).intValue());
      case SIGNED_SHORT:
        return Short.toString((short)(new BigInteger(hex, 16).intValue()));
      case SIGNED_INT:
        return Integer.toString(new BigInteger(hex, 16).intValue());
      case SIGNED_LONG:
        return Long.toString(new BigInteger(hex, 16).longValue());
      case UNSIGNED_CHAR:
        return "" + (byte)(new BigInteger(hex, 16).intValue());
      case UNSIGNED_SHORT:
        return Integer.toUnsignedString((short)(new BigInteger(hex, 16).intValue()));
      case UNSIGNED_INT:
        return Integer.toUnsignedString(new BigInteger(hex, 16).intValue());
      case UNSIGNED_LONG:
        return Long.toUnsignedString(new BigInteger(hex, 16).longValue());
      default:
        return value;
    }
  }
  
  
  public String getHex(VariableType convertTo, boolean convertToBigEndian) {
    if (type == VariableType.STRING || convertTo == VariableType.STRING) return value;
    List<String> bytes = divideHexIntoBytes(value.substring(2));
    if (convertToBigEndian != isBigEndian) Collections.reverse(bytes);
    String joinedBytes = getBytesByType(String.join("", bytes), convertTo);
    
    return "0x" + joinedBytes;
  }
  
  
  public String getBinary(VariableType convertTo, boolean convertToBigEndian) {
    if (type == VariableType.STRING || convertTo == VariableType.STRING) return value;
    String hex = getHex(convertTo, convertToBigEndian).substring(2);
    
    int numZeroes = 0;
    switch (convertTo) {
      case UNSIGNED_CHAR:
      case SIGNED_CHAR: numZeroes = 8; break;
      case SIGNED_SHORT: 
      case UNSIGNED_SHORT: numZeroes = 16; break;
      case SIGNED_INT: 
      case UNSIGNED_INT: numZeroes = 32; break;
      case SIGNED_LONG: 
      case UNSIGNED_LONG: numZeroes = 64; break;
    }
    return padWithZeroes(numZeroes, new BigInteger(hex, 16).toString(2));
  }
  
  
  private String getBytesByType(String digits, VariableType convertTo) {
    int numDigits;
    switch (convertTo) {
      case SIGNED_CHAR:
      case UNSIGNED_CHAR:
        numDigits = 2;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = padWithZeroes(numDigits, digits);
        break;
      case SIGNED_SHORT:
      case UNSIGNED_SHORT:
        numDigits = 4;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = padWithZeroes(numDigits, digits);
        break;
      case SIGNED_INT:
      case UNSIGNED_INT:
        numDigits = 8;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = padWithZeroes(numDigits, digits);
        break;
      case SIGNED_LONG:
      case UNSIGNED_LONG:
        numDigits = 16;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = padWithZeroes(numDigits, digits);
        break;        
    }
    return digits;
  }
  
  
  private List<String> divideHexIntoBytes(String hex) {
    List<String> bytes = new ArrayList<>();
    while (hex.length() > 0) {
      bytes.add(hex.substring(0, 2));
      hex = hex.substring(2, hex.length());
    }
    return bytes;
  }
  
  
  private String padWithZeroes(int numZeroes, String str) {
    String zeroes = "";
    for (int n = 0; n < numZeroes; ++n) zeroes += "0";
    return zeroes.substring(str.length()) + str;
  }

}



/*
public enum VariableType {
  SIGNED_CHAR, UNSIGNED_CHAR,
  SIGNED_SHORT, UNSIGNED_SHORT,
  SIGNED_INT, UNSIGNED_INT,
  SIGNED_LONG, UNSIGNED_LONG,
  STRING; 
}
*/
