import java.io.*;
import java.util.*;

public class CoordinatorMaster {

  public ProcessRun run = null;
  public ProcessRunFilter runFilter;

  private List<UIDetachedTab> detachedTabs = new ArrayList<>();
  private UIMainWindow mainWindow;
  private UIPlayControls playControls;
  

  public void exec() {
    runFilter = new ProcessRunFilter();
    mainWindow = new UIMainWindow(this);
    playControls = new UIPlayControls(this);
        
    mainWindow.display();
    playControls.display();
    
    loadConfigFile();
  }
  
  
  public void registerDetachedTab(UIDetachedTab tab) { detachedTabs.add(tab); }
  public void deregisterDetachedTab(UIDetachedTab tab) { detachedTabs.remove(tab); }
  
  
  public void closeProgram() {
    boolean answer = UIConfirmBox.display("Confirm Exit", "Are you sure you want to exit?");
    if (answer) {
      saveConfigFile();
      for (UIDetachedTab tab : detachedTabs) tab.window.close();
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
      loadConfig(lines);
      for (UIDetachedTab tab : detachedTabs) tab.loadConfig(lines);
      
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
      out += saveConfig();
      for (UIDetachedTab tab : detachedTabs) out += tab.saveConfig();
      
      writer.write(out);
      
    } catch (IOException ex) {
      System.err.println("Error saving config file!");
    }
  }
  
  
  private String saveConfig() {
    String config = "";
    for (UIDetachedTab tab : detachedTabs) config += "DetachedTab:" + tab.title + System.lineSeparator();
    
    return config;
  }
  
  
  private void loadConfig(List<String> config) {
    for (String line : config) {
      String[] parameters = line.trim().split(":");
      if (parameters.length > 1) {
        if (parameters[0].equals("DetachedTab")) mainWindow.detachTab(parameters[1]);
      }
    }
  }
  
  //detachTab(Tab tab, String title, HBox content

}
