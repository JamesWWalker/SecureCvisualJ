import java.math.*;
import java.util.*;

public class VariableRepresentation {

  String value;
  VariableType type;
  public boolean isBigEndian;
  public long address;
  
  
  public String getValue() { return value; }
  
  
  public String typeConversion(String valueIn, VariableType typeIn) {
    if (valueIn.startsWith("0x") && typeIn == VariableType.STRING)
      return "0x" + valueIn.substring(2).toUpperCase();
    else if (!valueIn.startsWith("0x") && typeIn != VariableType.STRING) {
      switch (typeIn) {
        case SIGNED_CHAR:
          valueIn = valueIn.replaceAll("'", "");
          if (valueIn.startsWith("\\x")) valueIn = valueIn.substring(2);
          else {
            valueIn = Long.toHexString(Long.parseLong(valueIn) & 0xff);
            if (valueIn.length() > 2) valueIn = valueIn.substring(2);
          }
          valueIn = extend(2, valueIn, new BigInteger(valueIn, 16).compareTo(BigInteger.ZERO) >= 0);
          break;
        case UNSIGNED_CHAR:
          valueIn = valueIn.replaceAll("'", "");
          if (valueIn.startsWith("\\x")) valueIn = valueIn.substring(2);
          else {
            valueIn = Long.toHexString(Long.parseUnsignedLong(valueIn) & 0xff);
            if (valueIn.length() > 2) valueIn = valueIn.substring(2);
          }
          valueIn = extend(2, valueIn, true);
          break;
        case SIGNED_SHORT:
          valueIn = Long.toHexString(Long.parseLong(valueIn) & 0xffff);
          if (valueIn.length() > 4) valueIn = valueIn.substring(4);
          valueIn = extend(4, valueIn, new BigInteger(valueIn, 16).compareTo(BigInteger.ZERO) >= 0);
          break;
        case UNSIGNED_SHORT:
          valueIn = Long.toHexString(Long.parseUnsignedLong(valueIn) & 0xffff);
          if (valueIn.length() > 4) valueIn = valueIn.substring(4);
          valueIn = extend(4, valueIn, true);
          break;
        case SIGNED_INT:
          valueIn = Long.toHexString(Long.parseLong(valueIn) & 0xffffffff);
          if (valueIn.length() > 8) valueIn = valueIn.substring(8);
          valueIn = extend(8, valueIn, new BigInteger(valueIn, 16).compareTo(BigInteger.ZERO) >= 0);
          break;
        case UNSIGNED_INT:
          valueIn = Long.toHexString(Long.parseUnsignedLong(valueIn) & 0xffffffff);
          if (valueIn.length() > 8) valueIn = valueIn.substring(8);
          valueIn = extend(8, valueIn, true);
          break;
        case SIGNED_LONG:
          if (UIUtils.architecture == 64) {
            valueIn = Long.toHexString(Long.parseLong(valueIn));
            valueIn = extend(16, valueIn, new BigInteger(valueIn, 16).compareTo(BigInteger.ZERO) >= 0);
          }
          else if (UIUtils.architecture == 32) {
            valueIn = Long.toHexString(Long.parseLong(valueIn) & 0xffffffff);
            if (valueIn.length() > 8) valueIn = valueIn.substring(8);
            valueIn = extend(8, valueIn, new BigInteger(valueIn, 16).compareTo(BigInteger.ZERO) >= 0);
          } 
          break;
        case UNSIGNED_LONG:
          if (UIUtils.architecture == 64) {
            valueIn = Long.toHexString(Long.parseUnsignedLong(valueIn));
            valueIn = extend(16, valueIn, true);
          }
          else if (UIUtils.architecture == 32) {
            valueIn = Long.toHexString(Long.parseUnsignedLong(valueIn) & 0xffffffff);
            if (valueIn.length() > 8) valueIn = valueIn.substring(8);
            valueIn = extend(8, valueIn, true);
          }
          break;
        default:
          valueIn = valueIn;
          break;
      }
      return "0x" + valueIn.toUpperCase();
    }
    else return valueIn;
  }
  
  
  public void setValue(String valueIn, VariableType typeIn) {
    type = typeIn;
    value = typeConversion(valueIn, typeIn);
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
  public static BigInteger convertHexToDecimal(String hex, boolean signed) {
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
      case UNSIGNED_LONG: numZeroes = UIUtils.architecture; break;
    }
    return extend(numZeroes, new BigInteger(hex, 16).toString(2),
     new BigInteger(hex, 16).compareTo(BigInteger.ZERO) >= 0);
  }
  
  
  private String getBytesByType(String digits, VariableType convertTo) {
    int numDigits;
    switch (convertTo) {
      case SIGNED_CHAR:
      case UNSIGNED_CHAR:
        numDigits = 2;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = extend(numDigits, digits, new BigInteger(digits, 16).compareTo(BigInteger.ZERO) >= 0);
        break;
      case SIGNED_SHORT:
      case UNSIGNED_SHORT:
        numDigits = 4;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = extend(numDigits, digits, new BigInteger(digits, 16).compareTo(BigInteger.ZERO) >= 0);
        break;
      case SIGNED_INT:
      case UNSIGNED_INT:
        numDigits = 8;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = extend(numDigits, digits, new BigInteger(digits, 16).compareTo(BigInteger.ZERO) >= 0);
        break;
      case SIGNED_LONG:
      case UNSIGNED_LONG:
        numDigits = UIUtils.architecture / 4;
        if (digits.length() >= numDigits) digits = digits.substring(digits.length()-numDigits);
        else digits = extend(numDigits, digits, new BigInteger(digits, 16).compareTo(BigInteger.ZERO) >= 0);
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
  
  
  /*private String signExtend(int numDigits, String str) {
  System.err.println("DEBUG: " + numDigits + ", " + str);
  if (str.equals("8")) System.err.println("DEBUG2: " + new BigInteger(str).toString(16));
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
  } */
  
  
  private String extend(int numDigits, String str, boolean positive) {
    String extend = "";
    if (positive) {
      for (int n = 0; n < numDigits; ++n) extend += "0";
    }
    else {
      for (int n = 0; n < numDigits; ++n) extend += "F";
    }
    return extend.substring(str.length()) + str;
  }
  
  
  public String clampValue(VariableType typeIn, String valueIn) {
    switch (type) {
      case UNSIGNED_CHAR:
        if (Long.parseLong(valueIn) < 0) {
          setValue(new BigInteger("255").toString(), typeIn);
          return "255";
        }
        if (Long.parseLong(valueIn) > 255) {
          setValue("0x00", typeIn);
          return "0";
        }
        break;
      case SIGNED_CHAR:
        if (Long.parseLong(valueIn) < -128) {
          setValue(new BigInteger("127").toString(), typeIn);
          return "127";
        }
        if (Long.parseLong(valueIn) > 127) {
          setValue(new BigInteger("-128").toString(), typeIn);
          return "-128";
        }
        break;
      case UNSIGNED_SHORT:
        if (Long.parseLong(valueIn) < 0) {
          setValue(new BigInteger("65535").toString(), typeIn);
          return "65535";
        }
        if (Long.parseLong(valueIn) > 65535) {
          setValue("0x00", typeIn);
          return "0";
        }
        break;
      case SIGNED_SHORT:
        if (Long.parseLong(valueIn) < -32768) {
          setValue(new BigInteger("32767").toString(), typeIn);
          return "32767";
        }
        if (Long.parseLong(valueIn) > 32767) {
          setValue(new BigInteger("-32768").toString(), typeIn);
          return "-32768";
        }
        break;
      case UNSIGNED_INT:
        if (Long.parseLong(valueIn) < 0) {
          setValue(new BigInteger("4294967295").toString(), typeIn);
          return "4294967295";
        }
        if (new BigInteger(valueIn).compareTo(new BigInteger("4294967295")) == 1) {
          setValue("0x00", typeIn);
          return "0";
        }
        break;
      case SIGNED_INT:
        if (Long.parseLong(valueIn) < -2147483648) {
          setValue(new BigInteger("2147483647").toString(), typeIn);
          return "2147483647";
        }
        if (Long.parseLong(valueIn) > 2147483647) {
          setValue(new BigInteger("-2147483648").toString(), typeIn);
          return "-2147483648";
        }
        break;
      case UNSIGNED_LONG:
        if (UIUtils.architecture == 64) {
          if (new BigInteger(valueIn).compareTo(BigInteger.valueOf(0)) == -1) {
            setValue(new BigInteger("18446744073709551615").toString(), typeIn);
            return "18446744073709551615";
          }
          else if (new BigInteger(valueIn).compareTo(new BigInteger("18446744073709551615")) == 1) {
            setValue("0x00", typeIn);
            return "0";
          }
        }
        else if (UIUtils.architecture == 32) {
          if (Long.parseLong(valueIn) < 0) {
            setValue(new BigInteger("4294967295").toString(), typeIn);
            return "4294967295";
          }
          if (new BigInteger(valueIn).compareTo(new BigInteger("4294967295")) == 1) {
            setValue("0x00", typeIn);
            return "0";
          }
        }
        break;
      case SIGNED_LONG:
        if (UIUtils.architecture == 64) {
          if (new BigInteger(valueIn).compareTo(new BigInteger("-9223372036854775808")) == -1) {
            setValue(new BigInteger("9223372036854775807").toString(), typeIn);
            return "9223372036854775807";
          }
          else if (new BigInteger(valueIn).compareTo(new BigInteger("9223372036854775807")) == 1) {
            setValue(new BigInteger("-9223372036854775808").toString(), typeIn);
            return "-9223372036854775808";
          }
        }
        else if (UIUtils.architecture == 32) {
          if (Long.parseLong(valueIn) < -2147483648) {
            setValue(new BigInteger("2147483647").toString(), typeIn);
            return "2147483647";
          }
          if (Long.parseLong(valueIn) > 2147483647) {
            setValue(new BigInteger("-2147483648").toString(), typeIn);
            return "-2147483648";
          }
        }
        break;
    }
    setValue(new BigInteger(valueIn).toString(), typeIn);
    return valueIn;
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
