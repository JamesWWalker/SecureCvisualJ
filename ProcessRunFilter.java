import java.util.*;

public class ProcessRunFilter {

  private boolean showAllFunctions;
  private boolean showRegisters;
  private boolean showAllSectionsByDefault;
  private boolean showAssembly;
  
  private List<String> functionsFilter = new ArrayList<>();
  private List<String> registersFilter = new ArrayList<>();
  private List<String> sectionsFilter = new ArrayList<>();
  
  
  public void setDetailLevel(DetailLevel detailLevel) {
    functionsFilter.clear();
    registersFilter.clear();
    sectionsFilter.clear();
    
    switch (detailLevel) {
      case NOVICE:
        showAllFunctions = false;
        showRegisters = false;
        showAllSectionsByDefault = false;
        showAssembly = false;
        break;
      case INTERMEDIATE:
        showAllFunctions = false;
        showRegisters = true;
        showAllSectionsByDefault = false;
        showAssembly = true;
        sectionsFilter.add(".text");
        sectionsFilter.add(".data");
        sectionsFilter.add(".rodata");
        break;
      case ADVANCED:
        showAllFunctions = true;
        showRegisters = true;
        showAllSectionsByDefault = false;
        showAssembly = true;
        sectionsFilter.add(".text");
        sectionsFilter.add(".data");
        sectionsFilter.add(".rodata");
        break;
      case EXPERT:
        showAllFunctions = true;
        showRegisters = true;
        showAllSectionsByDefault = true;
        showAssembly = true;
        break;
    }
    
    if (!showAllFunctions) {
      functionsFilter.add("_start");
    }
  } // setDetailLevel()
  
  
  public int getSourceLine(ProcessRun run) {
    return run.getSourceLine();
  }
  
  
  public String getAssembly(ProcessRun run) {
    if (showAssembly) return run.getAssembly();
    else return "WARNING: Attempt to retrieve assembly while showing assembly is disabled.";
  }
  
  
  public List<ActivationRecord> getStack(ProcessRun run) {
    List<ActivationRecord> stack = run.getStack();
    if (!showAllFunctions) {
      for (int n = stack.size()-1; n >= 0; --n) {
        if (!stack.get(n).file.equals(run.programName)) stack.remove(n);
      }
    }
    for (int n = stack.size()-1; n >= 0; --n) {
      if (functionsFilter.contains(stack.get(n).function)) stack.remove(n);
    }
    return stack;
  }
  
  
  public TreeMap<String, String> getRegisters(ProcessRun run) {
    if (showRegisters) {
      TreeMap<String, String> registers = run.getRegisters();
      Set<String> keys = registers.keySet();
      for (String key : keys) {
        if (registersFilter.contains(key)) registers.remove(key);
      }
      return registers;
    }
    else {
      System.err.println("WARNING: Attempt to retrieve registers while showing registers is disabled.");
      return new TreeMap<String, String>();
    }
  }
  
  
  public TreeMap<String, VariableDelta> getVariables(ProcessRun run) {
    return run.getVariables();
  }
  
  
  public ArrayList<ProgramSection> getSections(ProcessRun run) {
    ArrayList<ProgramSection> sections = run.getSections();
    
    if (showAllSectionsByDefault) {
      for (int n = sections.size()-1; n >= 0; --n) {
        ProgramSection section = sections.get(n);
        if (sectionsFilter.stream().anyMatch(s -> section.name.endsWith(s))) sections.remove(n);
      }
      return sections;
    } 
    else {
      for (int n = sections.size()-1; n >= 0; --n) {
        ProgramSection section = sections.get(n);
        if (!sectionsFilter.stream().anyMatch(s -> section.name.endsWith(s))) sections.remove(n);
      }
      return sections;
    }
  }


}
