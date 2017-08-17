public enum DetailLevel {
  NOVICE, INTERMEDIATE, ADVANCED, EXPERT, CUSTOM;
  
  public static DetailLevel parse(String in) {
    if (in.equals("NOVICE")) return NOVICE;
    else if (in.equals("INTERMEDIATE")) return INTERMEDIATE;
    else if (in.equals("ADVANCED")) return ADVANCED;
    else if (in.equals("EXPERT")) return EXPERT;
    else if (in.equals("CUSTOM")) return CUSTOM;
    else {
      System.err.println("ERROR: Illegal DetailLevel type " + in);
      System.exit(1);
      return CUSTOM;
    }
  }
  
  public static String toString(DetailLevel in) {
    switch (in) {
      case NOVICE: return "NOVICE";
      case INTERMEDIATE: return "INTERMEDIATE";
      case ADVANCED: return "ADVANCED";
      case EXPERT: return "EXPERT";
      case CUSTOM: return "CUSTOM";
      default:
        System.err.println("ERROR: Illegal DetailLevel type " + in);
        System.exit(1);
        return "Unreachable";
    }
  }
}
