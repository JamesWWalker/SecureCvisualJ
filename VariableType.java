public enum VariableType {
  SIGNED_CHAR, UNSIGNED_CHAR,
  SIGNED_SHORT, UNSIGNED_SHORT,
  SIGNED_INT, UNSIGNED_INT,
  SIGNED_LONG, UNSIGNED_LONG,
  STRING; 


  public static VariableType convertFromAnalysis(String in) {
    if (!in.contains("unsigned")) {
      if (in.contains("char")) return SIGNED_CHAR;
      if (in.contains("int")) {
        if (!in.contains("long")) {
          if (!in.contains("short")) return SIGNED_INT;
          return SIGNED_SHORT;
        }
        return SIGNED_LONG;
      }
      if (in.contains("long")) return SIGNED_LONG;
      if (in.contains("short")) return SIGNED_SHORT;
    } 
    else {
      if (in.contains("char")) return UNSIGNED_CHAR;
      if (in.contains("int")) {
        if (!in.contains("long")) {
          if (!in.contains("short")) return UNSIGNED_INT;
          return UNSIGNED_SHORT;
        }
        return UNSIGNED_LONG;
      }
      if (in.contains("long")) return UNSIGNED_LONG;
      if (in.contains("short")) return UNSIGNED_SHORT;
    }
    return STRING;
  }
  
  
  public static String toString(VariableType type) {
    switch (type) {
      case SIGNED_CHAR: return "signed char";
      case UNSIGNED_CHAR: return "unsigned char";
      case SIGNED_SHORT: return "signed short";
      case UNSIGNED_SHORT: return "unsigned short";
      case SIGNED_INT: return "signed int"; 
      case UNSIGNED_INT: return "unsigned int";
      case SIGNED_LONG: return "signed long";
      case UNSIGNED_LONG: return "unsigned long";
      case STRING: return "string";
      default: return "invalid";
    }
  }
}
