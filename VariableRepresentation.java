public class VariableRepresentation {

  String value;
  public VariableType type;
  
  
  public String getValue() { return value; }
  
  
  public void setValue(String valueIn) {
    if (!valueIn.startsWith("0x") && type != VariableType.STRING) {
      switch (type) {
        case SIGNED_CHAR:
        case UNSIGNED_CHAR:
          value = valueIn.replaceAll("'", "");
          if (value.startsWith("\\x")) value = value.substring(2);
          else value = Integer.toHexString((int)(value.charAt(0)));
          value = "00".substring(value.length()) + value; // pad out with 0s
          break;
        case SIGNED_SHORT:
          value = Integer.toHexString(Short.parseShort(valueIn) & 0xffff);
          value = "0000".substring(value.length()) + value; // pad out with 0s
          break;
        case UNSIGNED_SHORT:
          value = Integer.toHexString(Integer.parseUnsignedInt(valueIn) & 0xffff);
          value = "0000".substring(value.length()) + value; // pad out with 0s
          break;
        case SIGNED_INT:
          value = Integer.toHexString(Integer.parseInt(valueIn));
          value = "00000000".substring(value.length()) + value; // pad out with 0s
          break;
        case UNSIGNED_INT:
          value = Integer.toHexString(Integer.parseUnsignedInt(valueIn));
          value = "00000000".substring(value.length()) + value; // pad out with 0s
          break;
        case SIGNED_LONG:
          value = Long.toHexString(Long.parseLong(valueIn));
          value = "0000000000000000".substring(value.length()) + value; // pad out with 0s
          break;
        case UNSIGNED_LONG:
          value = Long.toHexString(Long.parseUnsignedLong(valueIn));
          value = "0000000000000000".substring(value.length()) + value; // pad out with 0s
          break;
        default:
          value = valueIn;
          break;
      }
      value = "0x" + value.toUpperCase();
    }
    else value = valueIn;
  }
  
  
  public VariableRepresentation(VariableType typeIn, String valueIn) {
    type = typeIn;
    setValue(valueIn);
  }
  
  
  String asSignedChar() {
    switch (type) {
      case SIGNED_CHAR:
        return "TODO";
      default:
        return "TODO"; // TODO
    }
  }

}



/*
public enum VariableType {
  SIGNED_CHAR, UNSIGNED_CHAR,
  SIGNED_SHORT, UNSIGNED_SHORT,
  SIGNED_INT, UNSIGNED_INT,
  SIGNED_LONG, UNSIGNED_LONG,
  SIGNED_LONG_LONG, UNSIGNED_LONG_LONG,
  STRING; 
}
*/
