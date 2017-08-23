import java.util.*;
import java.util.stream.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.util.*;

public class UIProgramAddressSpace {
  

  public static ScrollPane buildPAS(UIMainWindow mainWindow,
                                    Scene scene,
                                    DetailLevel detailLevel,
                                    List<ActivationRecord> stack,
                                    TreeMap<String, String> registers,
                                    TreeMap<String, VariableDelta> variables,
                                    List<ProgramSection> sections)
  {
    final ScrollPane scrollPane = new ScrollPane();
    
    final AnchorPane layout = new AnchorPane();
    layout.setPadding(new Insets(10, 10, 10, 10));
    layout.prefWidthProperty().bind(mainWindow.getTabWindow(
      SubProgram.toString(SubProgram.PAS)).widthProperty());
    layout.prefHeightProperty().bind(mainWindow.getTabWindow(
      SubProgram.toString(SubProgram.PAS)).heightProperty());

    if (detailLevel == DetailLevel.NOVICE) {
      
      GridPane variableTable = UIPASVariableTable.createTable(mainWindow,
        mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), 
        variables);
      
      AnchorPane.setTopAnchor(variableTable, 10.0);
      AnchorPane.setLeftAnchor(variableTable, 10.0);
      AnchorPane.setRightAnchor(variableTable, 10.0);
      AnchorPane.setBottomAnchor(variableTable, 10.0);
      layout.getChildren().add(variableTable);
 
      scrollPane.setContent(layout);
      ((Group) scene.getRoot()).getChildren().add(scrollPane);

      return scrollPane;
    }
    
    VBox pasLayout = new VBox();
    
    // REGISTERS
    Label registerHeader = new Label("CPU Registers");      // TODO: color-coding etc.?
    registerHeader.setStyle("-fx-border-color: black;");
    registerHeader.prefWidthProperty().bind(pasLayout.widthProperty());;
    pasLayout.getChildren().add(registerHeader);
    
    pasLayout.getChildren().add(UIPASRegisterTable.createTable(mainWindow,
      mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), registers));
    
    // FUNCTIONS+VARIABLES
    
    // Sections
    if (sections != null && sections.size() > 0) {
      for (ProgramSection ps : sections) {
        Label sectionHeader = new Label(ps.toString());      // TODO: color-coding etc.?
        sectionHeader.setStyle("-fx-border-color: black;");
        sectionHeader.prefWidthProperty().bind(pasLayout.widthProperty());;
        pasLayout.getChildren().add(sectionHeader);
        
        // Globals
        if (ps.name.endsWith(".data")) {
          TreeMap<String, VariableDelta> globalVariables = new TreeMap<>();
          for (VariableDelta v : variables.values()) {
            if (v.scope.equals(UIUtils.GLOBAL) && !v.type.contains("const")) 
              globalVariables.put(UIUtils.GLOBAL + "," + v.name, v);
          }     
          if (globalVariables.size() > 0) pasLayout.getChildren()
            .add(UIPASVariableTable.createTable(mainWindow,
            mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), 
            globalVariables));
        }
        
        // Read-only globals
        if (ps.name.endsWith(".rodata")) {
          TreeMap<String, VariableDelta> globalVariables = new TreeMap<>();
          for (VariableDelta v : variables.values()) {
            if (v.scope.equals(UIUtils.GLOBAL) && v.type.contains("const")) 
              globalVariables.put(UIUtils.GLOBAL + "," + v.name, v);
          }     
          if (globalVariables.size() > 0) pasLayout.getChildren()
            .add(UIPASVariableTable.createTable(mainWindow,
            mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), 
            globalVariables));
        }
      }
    }
    
    // Locals
    for (ActivationRecord ar : stack) {
      
      Label arHeader = new Label("0x" + Long.toHexString(ar.address) + ": " +ar.function);// TODO: color-coding etc.?
      arHeader.setStyle("-fx-border-color: black;");
      arHeader.prefWidthProperty().bind(pasLayout.widthProperty());;
      pasLayout.getChildren().add(arHeader);
      
      TreeMap<String, VariableDelta> localVariables = new TreeMap<>();
      for (VariableDelta v : variables.values()) {
        if (v.scope.equals(ar.function)) localVariables.put(ar.function + "," + v.name, v);
      }     
      if (localVariables.size() > 0) pasLayout.getChildren()
        .add(UIPASVariableTable.createTable(mainWindow,
        mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), 
        localVariables));
    }
    
    AnchorPane.setLeftAnchor(pasLayout, 10.0);
    AnchorPane.setRightAnchor(pasLayout, 10.0);
    layout.getChildren().add(pasLayout);
    
    scrollPane.setContent(layout);
     ((Group) scene.getRoot()).getChildren().add(scrollPane);

    return scrollPane;
  }

}
