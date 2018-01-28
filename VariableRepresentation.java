/*
Procedure when changing the value in the text box:

-Make sure it's a number. If it isn't, abort.
-Clamp its value based on the variable type.                          // DONE
-Calculate the equivalent hex.                                        // DONE

-For bottom part, copy hex verbatim.                                  // DONE
-Extend or truncate the hex as necessary based on the variable type.  // DONE
-Then convert the hex back into a decimal number.

-Update everything.



Procedure when changing the type:

-Extend or truncate the hex as necessary based on the variable type.
-Then convert the hex back into a decimal number.
-Update everything.
*/

import java.math.*;
import java.util.*;

public class VariableRepresentation {

  private String value; // hex, does NOT have 0x prefix
  public boolean isBigEndian = true;
  
  
  public VariableRepresentation(String hex) {
    isBigEndian = true;
    setValueFromHex(hex);
  }
  
  
  public VariableRepresentation(String hex, boolean isBigEndianIn) {
    isBigEndian = isBigEndianIn;
    setValueFromHex(hex);
  }
  
  
  public VariableRepresentation(boolean isBigEndianIn) {
    isBigEndian = isBigEndianIn;
    value = "0";
  }
  
  
  public String getValue(boolean isBigEndianIn) {
    if (isBigEndianIn == isBigEndian) return value;
    else return reverseBytes(value);
  }
  
  
  public String getValue() {
    return value;
  }
  
  
  public void reverseEndianness() {
    setValueFromHex(reverseBytes(value));
    isBigEndian = !isBigEndian;
  }
  
  
  public void setValueFromHex(String hex) {
    if (hex.startsWith("0x")) value = hex.substring(2).toUpperCase();
    else value = hex;
  }


  public void setValueFromDecimal(String decimal, VariableType typeIn) {
    String intermediate = convertDecimalToHex(decimal);
    value = resizeHex(typeIn, intermediate, decimal.charAt(0) != '-').toUpperCase();
  }


  // EXPECTS DECIMAL
  public String clampValue(VariableType typeIn, String valueIn) {
    BigInteger valueInDec = new BigInteger(valueIn);
    switch (typeIn) {
      case UNSIGNED_CHAR:
        if (valueInDec.compareTo(BigInteger.ZERO) < 0) return "255";
        if (valueInDec.compareTo(new BigInteger("255")) > 0) return "0";
        break;
      case SIGNED_CHAR:
        if (valueInDec.compareTo(new BigInteger("-128")) < 0) return "127";
        if (valueInDec.compareTo(new BigInteger("127")) > 0) return "-128";
        break;
      case UNSIGNED_SHORT:
        if (valueInDec.compareTo(BigInteger.ZERO) < 0) return "65535";
        if (valueInDec.compareTo(new BigInteger("65535")) > 0) return "0";
        break;
      case SIGNED_SHORT:
        if (valueInDec.compareTo(new BigInteger("-32768")) < 0) return "32767";
        if (valueInDec.compareTo(new BigInteger("32767")) > 0) return "-32768";
        break;
      case UNSIGNED_INT:
        if (valueInDec.compareTo(BigInteger.ZERO) < 0) return "4294967295";
        if (valueInDec.compareTo(new BigInteger("4294967295")) > 0) return "0";
        break;
      case SIGNED_INT:
        if (valueInDec.compareTo(new BigInteger("-2147483648")) < 0) return "2147483647";
        if (valueInDec.compareTo(new BigInteger("2147483647")) > 0) return "-2147483648";
        break;
      case UNSIGNED_LONG:
        if (UIUtils.architecture == 64) {        
          if (valueInDec.compareTo(BigInteger.ZERO) < 0) return "18446744073709551615";
          if (valueInDec.compareTo(new BigInteger("18446744073709551615")) > 0) return "0";
        }
        else if (UIUtils.architecture == 32) {
          if (valueInDec.compareTo(BigInteger.ZERO) < 0) return "4294967295";
          if (valueInDec.compareTo(new BigInteger("4294967295")) > 0) return "0";
        }
        break;
      case SIGNED_LONG:
        if (UIUtils.architecture == 64) {
          if (valueInDec.compareTo(new BigInteger("-9223372036854775808")) < 0) return "9223372036854775807";
          if (valueInDec.compareTo(new BigInteger("9223372036854775807")) > 0) return "-9223372036854775808";
        }
        else if (UIUtils.architecture == 32) {
          if (valueInDec.compareTo(new BigInteger("-2147483648")) < 0) return "2147483647";
          if (valueInDec.compareTo(new BigInteger("2147483647")) > 0) return "-2147483648";
        }
        break;
    }
    return valueIn;
  } // clampValue()
  
  
  
  // Can't get Java's built-ins to do this consistently, so let's do it manually
  public static String convertHexToDecimal(String hex, VariableType typeIn) {
  
    boolean signed = (typeIn == VariableType.SIGNED_CHAR || typeIn == VariableType.SIGNED_INT ||
                      typeIn == VariableType.SIGNED_LONG || typeIn == VariableType.SIGNED_SHORT);
  
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
    return runningTally.toString();
  }
  
  
  
  // Can't get Java's built-ins to do this right for big numbers, so let's do it manually
  public String convertDecimalToHex(String decimal) {
  
    // Convert magnitude to hex
    BigInteger magnitude = new BigInteger(decimal);
    if (decimal.charAt(0) == '-') magnitude = new BigInteger(decimal.substring(1));
      
    int remainder;
    String hex = "";
    char symbols[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
 
    if (decimal.equals("0")) hex = "0";
    else {
      while (magnitude.compareTo(BigInteger.ZERO) > 0)      {
        remainder = magnitude.mod(new BigInteger("16")).intValue(); 
        hex = symbols[remainder] + hex; 
        magnitude = magnitude.divide(new BigInteger("16"));
      }
    }
    
    // Convert hex to binary
    String binary = "";
    for (int s = 0; s < hex.length(); ++s) binary += convertHexToBinary(hex.charAt(s));
    
    // If it's a negative number, take the two's complement
    String negativeBinary = "";
    if (decimal.charAt(0) == '-') {
      for (int s = 0; s < binary.length(); ++s) {
        if (binary.charAt(s) == '0') negativeBinary += "1";
        else negativeBinary += "0";
      }
      binary = addBinary(negativeBinary, "1");
    }
    
    // Convert the binary back to hex
    hex = "";
    while (true) {
      hex += convertBinaryToHex(binary.substring(0, 4));
      if (binary.length() <= 4) break;
      binary = binary.substring(4, binary.length());
    }
     
    return hex;
  }
  
  
  
  private String convertHexToBinary(char digit) {
    switch (digit) {
      case '0': return "0000";
      case '1': return "0001";
      case '2': return "0010";
      case '3': return "0011";
      case '4': return "0100";
      case '5': return "0101";
      case '6': return "0110";
      case '7': return "0111";
      case '8': return "1000";
      case '9': return "1001";
      case 'A': return "1010";
      case 'B': return "1011";
      case 'C': return "1100";
      case 'D': return "1101";
      case 'E': return "1110";
      case 'F': return "1111";
    }
    return "0000";
  }
  
  
  
  private char convertBinaryToHex(String binary) {
    if (binary.equals("0000")) return '0';
    else if (binary.equals("0001")) return '1';
    else if (binary.equals("0010")) return '2';
    else if (binary.equals("0011")) return '3';
    else if (binary.equals("0100")) return '4';
    else if (binary.equals("0101")) return '5';
    else if (binary.equals("0110")) return '6';
    else if (binary.equals("0111")) return '7';
    else if (binary.equals("1000")) return '8';
    else if (binary.equals("1001")) return '9';
    else if (binary.equals("1010")) return 'A';
    else if (binary.equals("1011")) return 'B';
    else if (binary.equals("1100")) return 'C';
    else if (binary.equals("1101")) return 'D';
    else if (binary.equals("1110")) return 'E';
    else if (binary.equals("1111")) return 'F';
    return '0';
  }
  
  
  
  // assumes any necessary resizings have already been done
  public String reverseBytes(String valueIn) {
    List<String> bytes = divideHexIntoBytes(valueIn);
    Collections.reverse(bytes);
    return String.join("", bytes);
  }
  
  
  
  private List<String> divideHexIntoBytes(String hex) {
    List<String> bytes = new ArrayList<>();
    while (hex.length() > 0) {
      bytes.add(hex.substring(0, 2));
      hex = hex.substring(2, hex.length());
    }
    return bytes;
  }
  
  
  
  public String resizeHex(VariableType typeIn, String hex, boolean positive) {

    int numDigits = 8;
  
    switch (typeIn) {
      case UNSIGNED_CHAR:
      case SIGNED_CHAR:
        numDigits = 2;
        break;
      case UNSIGNED_SHORT:
      case SIGNED_SHORT:
        numDigits = 4;
        break;
      case UNSIGNED_INT:
      case SIGNED_INT:
        numDigits = 8;
        break;
      case UNSIGNED_LONG:
      case SIGNED_LONG:
        numDigits = UIUtils.architecture / 4;
        break;
    }
  
    String resize = "";
    
    if (hex.length() < numDigits) {
      if (positive) {
        for (int n = 0; n < numDigits; ++n) resize += "0";
      }
      else {
        for (int n = 0; n < numDigits; ++n) resize += "F";
      }
      return resize.substring(hex.length()) + hex;
    }
    else if (hex.length() > numDigits) return hex.substring(hex.length() - numDigits);
    else return hex;
    
  } // resizeHex()
  
  
  private String addBinary(String a, String b) {
        int la = a.length();
        int lb = b.length();
        
        int max = Math.max(la, lb);
        
        StringBuilder sum = new StringBuilder("");
        int carry = 0;
        
        for(int i = 0; i < max; i++){
            int m = getBit(a, la - i - 1);
            int n = getBit(b, lb - i - 1);
            int add = m + n + carry;
            sum.append(add % 2);
            carry = add / 2;
        }
        
        if(carry == 1)
            sum.append("1");
        
        return sum.reverse().toString();
        
  }
    
  private int getBit(String s, int index){
        if(index < 0 || index >= s.length())
            return 0;
        
        if(s.charAt(index) == '0')
            return 0;
        else
            return 1;
        
  }
  
  
  
  
}
