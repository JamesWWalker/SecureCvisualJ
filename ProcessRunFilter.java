import java.util.*;
import javafx.beans.property.*;

public class ProcessRunFilter {

  private BooleanProperty showAllFunctions = new SimpleBooleanProperty();
  public final boolean getShowAllFunctions() { return showAllFunctions.get(); }
  public final void setShowAllFunctions(boolean value) { showAllFunctions.set(value); }
  public BooleanProperty showAllFunctionsProperty() { return showAllFunctions; }
  
  private BooleanProperty showRegisters = new SimpleBooleanProperty();
  public final boolean getShowRegisters() { return showRegisters.get(); }
  public final void setShowRegisters(boolean value) { showRegisters.set(value); }
  public BooleanProperty showRegistersProperty() { return showRegisters; }
  
  private BooleanProperty showAllSectionsByDefault = new SimpleBooleanProperty();
  public final boolean getShowAllSectionsByDefault() { return showAllSectionsByDefault.get(); }
  public final void setShowAllSectionsByDefault(boolean value) { showAllSectionsByDefault.set(value); }
  public BooleanProperty showAllSectionsByDefaultProperty() { return showAllSectionsByDefault; }
  
  private BooleanProperty showAssembly = new SimpleBooleanProperty();
  public final boolean getShowAssembly() { return showAssembly.get(); }
  public final void setShowAssembly(boolean value) { showAssembly.set(value); }
  public BooleanProperty showAssemblyProperty() { return showAssembly; }
  
  private ObjectProperty<DetailLevel> detailLevel = new SimpleObjectProperty<>();
  public final DetailLevel getDetailLevel() { return detailLevel.get(); }
  public final void setDetailLevel(DetailLevel value) { detailLevel.set(value); }
  public ObjectProperty<DetailLevel> detailLevelProperty() { return detailLevel; }
  
  private List<String> functionsFilter = new ArrayList<>();
  private List<String> registersFilter = new ArrayList<>();
  private List<String> sectionsFilter = new ArrayList<>();
  
  
  public ProcessRunFilter() {
    detailLevelProperty().addListener((obs, oldv, newv) -> detailLevelChanged());
  }
  
  
  private void detailLevelChanged() {
  
    functionsFilter.clear();
    registersFilter.clear();
    sectionsFilter.clear(); // TODO: custom
    
    switch (getDetailLevel()) {
      case NOVICE:
        showAllFunctions.set(false);
        showRegisters.set(false);
        showAllSectionsByDefault.set(false);
        showAssembly.set(false);
        break;
      case INTERMEDIATE:
        showAllFunctions.set(false);
        showRegisters.set(true);
        showAllSectionsByDefault.set(false);
        showAssembly.set(true);
        sectionsFilter.add(".text");
        sectionsFilter.add(".data");
        sectionsFilter.add(".rodata");
        break;
      case ADVANCED:
        showAllFunctions.set(true);
        showRegisters.set(true);
        showAllSectionsByDefault.set(false);
        showAssembly.set(true);
        sectionsFilter.add(".text");
        sectionsFilter.add(".data");
        sectionsFilter.add(".rodata");
        break;
      case EXPERT:
        showAllFunctions.set(true);
        showRegisters.set(true);
        showAllSectionsByDefault.set(true);
        showAssembly.set(true);
        break;
    }
    
    if (!showAllFunctions.get()) {
      functionsFilter.add("_start");
    }
  } // detailLevelChanged()
  
  
  public int getSourceLine(ProcessRun run) {
    return run.getSourceLine();
  }
  
  
  public String getAssembly(ProcessRun run) {
    if (showAssembly.get()) return run.getAssembly();
    else return "WARNING: Attempt to retrieve assembly while showing assembly is disabled.";
  }
  
  
  public List<ActivationRecord> getStack(ProcessRun run) {
    List<ActivationRecord> stack = run.getStack();
    if (!showAllFunctions.get()) {
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
    if (showRegisters.get()) {
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
    
    if (showAllSectionsByDefault.get()) {
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
  
  
  public String saveConfig() {
    String config = "";
    config += "DetailLevel:" + getDetailLevel().toString() + System.lineSeparator();
    config += "ShowAllFunctions:" + showAllFunctions.get() + System.lineSeparator();
    config += "ShowRegisters:" + showRegisters.get() + System.lineSeparator();
    config += "ShowAllSectionsByDefault:" + showAllSectionsByDefault.get() + System.lineSeparator();
    config += "ShowAssembly:" + showAssembly.get() + System.lineSeparator();

    config += outputFilter("FunctionsFilter", functionsFilter);
    config += outputFilter("RegistersFilter", registersFilter);
    config += outputFilter("SectionsFilter", sectionsFilter);
    
    return config;
  }
  
  
  private String outputFilter(String name, List<String> filter) {
    String output = "";
    if (filter.size() > 0) {
      output += name + ":";
      for (int n = 0; n < filter.size(); ++n) {
        output += filter.get(n);
        if (n < filter.size()-1) output += ",";
      }
      output += System.lineSeparator();
    }
    return output;
  }
  
  
  public void loadConfig(List<String> config) {
    for (String line : config) {
      String[] parameters = line.trim().split(":");
      if (parameters.length > 1) {
        if (parameters[0].equals("DetailLevel")) setDetailLevel(DetailLevel.parse(parameters[1].trim()));
        else if (parameters[0].equals("ShowAllFunctions")) showAllFunctions.set(Boolean.parseBoolean(parameters[1]));
        else if (parameters[0].equals("ShowRegisters")) showRegisters.set(Boolean.parseBoolean(parameters[1]));
        else if (parameters[0].equals("ShowAllSectionsByDefault")) showAllSectionsByDefault.set(Boolean.parseBoolean(parameters[1]));
        else if (parameters[0].equals("ShowAssembly")) showAssembly.set(Boolean.parseBoolean(parameters[1]));
        
        else if (parameters[0].equals("FunctionsFilter")) readFilter(functionsFilter, parameters[1].split(","));
        else if (parameters[0].equals("RegistersFilter")) readFilter(registersFilter, parameters[1].split(","));
        else if (parameters[0].equals("SectionsFilter")) readFilter(sectionsFilter, parameters[1].split(","));
      }
    }
  }
  
  
  private void readFilter(List<String> filter, String[] elements) {
    filter.clear();
    for (int n = 0; n < elements.length; ++n) filter.add(elements[n]);
  }


}
