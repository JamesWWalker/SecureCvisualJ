import java.util.*;
import java.util.stream.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
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
        variables, "#ffffff");
      
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
    Label registerHeader = new Label("CPU Registers");
    registerHeader.setStyle("-fx-border-color: black;");
    registerHeader.setStyle("-fx-background-color: black;");
    registerHeader.setTextFill(Color.web("#cccccc"));
    registerHeader.prefWidthProperty().bind(pasLayout.widthProperty());
    registerHeader.setOnMouseClicked(e -> {
      mainWindow.coordinator.runFilter.setShowRegisters(
        !mainWindow.coordinator.runFilter.getShowRegisters());
      mainWindow.setCustomDetailLevel();
    });
    pasLayout.getChildren().add(registerHeader);
    
    if (registers.size() > 0) {
      pasLayout.getChildren().add(UIPASRegisterTable.createTable(mainWindow,
        mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), registers));
    }
    
    // FUNCTIONS+VARIABLES
    
    // Sections
    Label sectionHeader = new Label("Program Sections");
    sectionHeader.setStyle("-fx-border-color: black;");
    sectionHeader.setStyle("-fx-background-color: black;");
    sectionHeader.setTextFill(Color.web("#cccccc"));
    sectionHeader.prefWidthProperty().bind(pasLayout.widthProperty());
    sectionHeader.setOnMouseClicked(e -> {
      if (sections.size() == 0) mainWindow.coordinator.runFilter.clearSectionFilter();
      else {
        for (ProgramSection ps : sections)
          mainWindow.coordinator.runFilter.addSectionFilter(ps.name);
      }
      mainWindow.setCustomDetailLevel();
    });
    pasLayout.getChildren().add(sectionHeader);
    
    if (sections != null && sections.size() > 0) {
      for (ProgramSection ps : sections) {
        Label labelSection = new Label(ps.toString());      // TODO: color-coding etc.?
        labelSection.setStyle("-fx-border-color: black;");
        String color = UIUtils.getNextColor();
        labelSection.setStyle("-fx-background-color: " + color + ";");
        labelSection.prefWidthProperty().bind(pasLayout.widthProperty());
        labelSection.setOnMouseClicked(e -> {
          mainWindow.coordinator.runFilter.addSectionFilter(ps.name);
          mainWindow.coordinator.queryProcessRunAndUpdateUI();
        });
        pasLayout.getChildren().add(labelSection);
        
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
            globalVariables, color));
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
            globalVariables, color));
        }
      }
    }
    
    // Locals
    Label stackHeader = new Label("Stack");
    stackHeader.setStyle("-fx-border-color: black;");
    stackHeader.setStyle("-fx-background-color: black;");
    stackHeader.setTextFill(Color.web("#cccccc"));
    stackHeader.prefWidthProperty().bind(pasLayout.widthProperty());
    pasLayout.getChildren().add(stackHeader);
    
    for (ActivationRecord ar : stack) {
    
      String color = UIUtils.getNextColor();
      
      Label arHeader = new Label("0x" + Long.toHexString(ar.address) + ": " +ar.function);
      arHeader.setStyle("-fx-border-color: black;");
      arHeader.setStyle("-fx-background-color: " + color + ";");
      arHeader.prefWidthProperty().bind(pasLayout.widthProperty());;
      pasLayout.getChildren().add(arHeader);
      
      TreeMap<String, VariableDelta> localVariables = new TreeMap<>();
      for (VariableDelta v : variables.values()) {
        if (v.scope.equals(ar.function)) localVariables.put(ar.function + "," + v.name, v);
      }     
      if (localVariables.size() > 0) pasLayout.getChildren()
        .add(UIPASVariableTable.createTable(mainWindow,
        mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), 
        localVariables, color));
    }
    
    AnchorPane.setLeftAnchor(pasLayout, 10.0);
    AnchorPane.setRightAnchor(pasLayout, 10.0);
    layout.getChildren().add(pasLayout);
    
    scrollPane.setContent(layout);
     ((Group) scene.getRoot()).getChildren().add(scrollPane);

    return scrollPane;
  }

}
