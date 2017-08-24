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
    }
    return STRING;
  }
}
