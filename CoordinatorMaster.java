import java.io.*;
import java.util.*;
import javafx.beans.property.*;

public class CoordinatorMaster {

  private ObjectProperty<ProcessRun> run = new SimpleObjectProperty<>();
  public final ProcessRun getRun() { return run.get(); }
  public final void setRun(ProcessRun value) { run.set(value); }
  public ObjectProperty<ProcessRun> runProperty() { return run; }

  public ProcessRunFilter runFilter;

  private UIMainWindow mainWindow;
  private UIPlayControls playControls;
  

  public void exec() {
    UIUtils.initializeColorWheel();
    setRun(new ProcessRun());
    runFilter = new ProcessRunFilter();
    mainWindow = new UIMainWindow(this);
    playControls = new UIPlayControls(this);
        
    mainWindow.display();
    playControls.display();
    
    loadConfigFile();
  }
  
  
  public void queryProcessRunAndUpdateUI() {
    // TODO: add data for other tabs
    if (!getRun().isNull()) {
      UIUtils.resetColorIndex();
      mainWindow.updateUI(runFilter.getSourceLine(getRun()),
                          runFilter.getAssembly(getRun()),
                          runFilter.getStack(getRun()),
                          runFilter.getRegisters(getRun()),
                          runFilter.getVariables(getRun()),
                          runFilter.getSections(getRun()));
    }
  }
  
  
  public void closeProgram() {
    boolean answer = UIConfirmBox.display("Confirm Exit", "Are you sure you want to exit?");
    if (answer) {
      saveConfigFile();
      for (UIDetachedTab tab : mainWindow.detachedTabs) tab.window.close();
      playControls.window.close();
      mainWindow.window.close();
    }
  }
  
  
  public void loadConfigFile() {
    try (BufferedReader reader = new BufferedReader(new FileReader("./config.txt"))) {
      List<String> lines = new ArrayList<String>();
      String line = null;
           
      while ((line = reader.readLine()) != null) lines.add(line + System.lineSeparator());

      mainWindow.loadConfig(lines);
      playControls.loadConfig(lines);
      runFilter.loadConfig(lines);
      
    } catch (IOException ex) {
      System.err.println("Error loading config file, proceeding with default values.");
    }
  }
  
  
  public void saveConfigFile() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("./config.txt"))) {
      String out = "";
      out += mainWindow.saveConfig();
      out += playControls.saveConfig();
      out += runFilter.saveConfig();
      
      writer.write(out);
      
    } catch (IOException ex) {
      System.err.println("Error saving config file!");
    }
  }
  

}
