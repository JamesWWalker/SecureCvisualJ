import java.util.*;

public enum SubProgram {
  PAS, CG, FO, SD, SC;
  
  public static SubProgram parse(String in) {
    if (in.equals("Program Address Space")) return PAS;
    else if (in.equals("Call Graph")) return CG;
    else if (in.equals("File Operations")) return FO;
    else if (in.equals("Sensitive Data")) return SD;
    else if (in.equals("Source Code")) return SC;
    else {
      System.err.println("ERROR: Illegal SubProgram type " + in);
      System.exit(1);
      return PAS;
    }
  }
  
  public static String toString(SubProgram in) {
    switch (in) {
      case PAS: return "Program Address Space";
      case CG: return "Call Graph";
      case FO: return "File Operations";
      case SD: return "Sensitive Data";
      case SC: return "Source Code";
      default:
        System.err.println("ERROR: Illegal SubProgram type " + in);
        System.exit(1);
        return "Unreachable";
    }
  }
  
  public static List<String> allSubPrograms() {
    List<String> all = new ArrayList<>();
    all.add("Program Address Space");
    all.add("Call Graph");
    all.add("File Operations");
    all.add("Sensitive Data");
    all.add("Source Code");
    return all;
  }

}
