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
          value = valueIn.replaceAll("'", "");
          if (value.startsWith("\\x")) value = value.substring(2);
          else {
            value = Long.toHexString(Long.parseLong(value) & 0xff);
            if (value.length() > 2) value = value.substring(2);
          }
          value = signExtend(2, value);
          break;
        case UNSIGNED_CHAR:
          value = valueIn.replaceAll("'", "");
          if (value.startsWith("\\x")) value = value.substring(2);
          else {
            value = Long.toHexString(Long.parseUnsignedLong(value) & 0xff);
            if (value.length() > 2) value = value.substring(2);
          }
          value = signExtend(2, value);
          break;
        case SIGNED_SHORT:
          value = Long.toHexString(Long.parseLong(valueIn) & 0xffff);
          if (value.length() > 4) value = value.substring(4);
          value = signExtend(4, value);
          break;
        case UNSIGNED_SHORT:
          value = Long.toHexString(Long.parseUnsignedLong(valueIn) & 0xffff);
          if (value.length() > 4) value = value.substring(4);
          value = signExtend(4, value);
          break;
        case SIGNED_INT:
          value = Long.toHexString(Long.parseLong(valueIn) & 0xffffffff);
          if (value.length() > 8) value = value.substring(8);
          value = signExtend(8, value);
          break;
        case UNSIGNED_INT:
          value = Long.toHexString(Long.parseUnsignedLong(valueIn) & 0xffffffff);
          if (value.length() > 8) value = value.substring(8);
          value = signExtend(8, value);
          break;
        case SIGNED_LONG:
          value = Long.toHexString(Long.parseLong(valueIn));
          value = signExtend(16, value);
          break;
        case UNSIGNED_LONG:
          value = Long.toHexString(Long.parseUnsignedLong(valueIn));
          value = signExtend(16, value);
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
    
    boolean signed = false;
    
    if (convertTo == VariableType.SIGNED_CHAR || convertTo == VariableType.SIGNED_INT ||
        convertTo == VariableType.SIGNED_LONG || convertTo == VariableType.SIGNED_SHORT)
    {
      signed = true;
    }
    
    return convertHexToDecimal(hex, signed).toString();
  }
  
  
  // Can't get Java's built-ins to do this consistently, so let's do it manually
  private BigInteger convertHexToDecimal(String hex, boolean signed) {
    int place = 0;
    BigInteger runningTally = BigInteger.ZERO;
    for (int digit = hex.length()-1; digit >= 0; --digit) {
      BigInteger multiplier = BigInteger.valueOf(16).pow(place);
      BigInteger dec = BigInteger.ZERO;
      switch (hex.toUpperCase().charAt(digit)) {
        case '1': dec = BigInteger.valueOf(1); break;
        case '2': dec = BigInteger.valueOf(2); break;
        case '3': dec = BigInteger.valueOf(3); break;
        case '4': dec = BigInteger.valueOf(4); break;
        case '5': dec = BigInteger.valueOf(5); break;
        case '6': dec = BigInteger.valueOf(6); break;
        case '7': dec = BigInteger.valueOf(7); break;
        case '8': dec = BigInteger.valueOf(8); break;
        case '9': dec = BigInteger.valueOf(9); break;
        case 'A': dec = BigInteger.valueOf(10); break;
        case 'B': dec = BigInteger.valueOf(11); break;
        case 'C': dec = BigInteger.valueOf(12); break;
        case 'D': dec = BigInteger.valueOf(13); break;
        case 'E': dec = BigInteger.valueOf(14); break;
        case 'F': dec = BigInteger.valueOf(15); break;
      }
      dec = dec.multiply(multiplier);
      // if signed, break the last digit into individual bits
      if (digit == 0 && signed) {
        String bits = "0000";
        switch (hex.toUpperCase().charAt(digit)) {
          case '1': bits = "0001"; break;
          case '2': bits = "0010"; break;
          case '3': bits = "0011"; break;
          case '4': bits = "0100"; break;
          case '5': bits = "0101"; break;
          case '6': bits = "0110"; break;
          case '7': bits = "0111"; break;
          case '8': bits = "1000"; break;
          case '9': bits = "1001"; break;
          case 'A': bits = "1010"; break;
          case 'B': bits = "1011"; break;
          case 'C': bits = "1100"; break;
          case 'D': bits = "1101"; break;
          case 'E': bits = "1110"; break;
          case 'F': bits = "1111"; break;
        }
        int binPlace = (hex.length()-1)*4;
        for (int binDigit = bits.length()-1; binDigit >= 0; --binDigit) {
          multiplier = BigInteger.valueOf(2).pow(binPlace);
          if (binDigit == 0) multiplier = multiplier.multiply(BigInteger.valueOf(-1));
          dec = BigInteger.ZERO;
          if (bits.charAt(binDigit) == '1') dec = BigInteger.valueOf(1);
          dec = dec.multiply(multiplier);
          runningTally = runningTally.add(dec);
          ++binPlace;
        }
      }
      else {
        runningTally = runningTally.add(dec);
        ++place;
      }
    }
    return runningTally;
  }
  
  
  public String getHex(VariableType convertTo, boolean convertToBigEndian) {
    if (type == VariableType.STRING || convertTo == VariableType.STRING) return value;
    List<String> bytes = divideHexIntoBytes(value.substring(2));
    if (convertToBigEndian != isBigEndian) Collections.reverse(bytes);
    String joinedBytes = getBytesByType(String.join("", bytes), convertTo);
    
    return "0x" + joinedBytes;
  }
  
  
  public void reverseEndianness(boolean newEndianness) {
    if (isBigEndian != newEndianness) {
      List<String> bytes = divideHexIntoBytes(value.substring(2));
      Collections.reverse(bytes);
      String joinedBytes = getBytesByType(String.join("", bytes), type);
      setValue("0x" + joinedBytes, type);
      isBigEndian = !isBigEndian;
    }
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
    return signExtend(numZeroes, new BigInteger(hex, 16).toString(2));
  }
  
  
  private String getBytesByType(String digits, VariableType convertTo) {
    int numDigits;
    switch (convertTo) {
      case SIGNED_CHAR:
      case UNSIGNED_CHAR:
        numDigits = 2;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = signExtend(numDigits, digits);
        break;
      case SIGNED_SHORT:
      case UNSIGNED_SHORT:
        numDigits = 4;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = signExtend(numDigits, digits);
        break;
      case SIGNED_INT:
      case UNSIGNED_INT:
        numDigits = 8;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = signExtend(numDigits, digits);
        break;
      case SIGNED_LONG:
      case UNSIGNED_LONG:
        numDigits = 16;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = signExtend(numDigits, digits);
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
  
  
  private String signExtend(int numDigits, String str) {
    boolean leadingOne =
      (str.charAt(0) == '8' || str.charAt(0) == '9' || str.charAt(0) == 'A' ||
       str.charAt(0) == 'B' || str.charAt(0) == 'C' || str.charAt(0) == 'D' ||
       str.charAt(0) == 'E' || str.charAt(0) == 'F');
    
    String extend = "";
    if (!leadingOne) {
      for (int n = 0; n < numDigits; ++n) extend += "0";
    }
    else {
      for (int n = 0; n < numDigits; ++n) extend += "F";
    }
    return extend.substring(str.length()) + str;
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
