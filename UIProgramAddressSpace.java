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
        variables, "#ffffff", -1, null, null);
      
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
    
    // INVOCATION
    Label invocation = new Label("Invocation: " + 
      mainWindow.coordinator.runFilter.getInvocation(mainWindow.coordinator.getRun()));
    invocation.setStyle("-fx-border-color: #444444;");
    invocation.setStyle("-fx-background-color: #444444;");
    invocation.setTextFill(Color.web("#ffffff"));
    invocation.prefWidthProperty().bind(pasLayout.widthProperty());
    pasLayout.getChildren().add(invocation);
    
    // OUTPUT
    Label outputHeader = new Label("Program Output");
    outputHeader.setStyle("-fx-border-color: black;");
    outputHeader.setStyle("-fx-background-color: black;");
    outputHeader.setTextFill(Color.web("#cccccc"));
    outputHeader.prefWidthProperty().bind(pasLayout.widthProperty());
    outputHeader.setOnMouseClicked(e -> {
      mainWindow.coordinator.runFilter.setShowOutput(
        !mainWindow.coordinator.runFilter.getShowOutput());
      mainWindow.setCustomDetailLevel();
    });
    pasLayout.getChildren().add(outputHeader);
    List<String> outputs = mainWindow.coordinator.runFilter.getOutput(mainWindow.coordinator.getRun());
    if (outputs != null && outputs.size() > 0) {
      pasLayout.getChildren().add(UIPASOutputTable.createTable(mainWindow,
        mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), outputs));
    }
    
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
          mainWindow.coordinator.runFilter.addSectionFilter(ps.values.get(ps.values.size()-1));
      }
      mainWindow.setCustomDetailLevel();
    });
    pasLayout.getChildren().add(sectionHeader);
    
    if (sections != null && sections.size() > 0) {
      for (ProgramSection ps : sections) {
        Label labelSection = new Label(ps.toString());
        labelSection.setStyle("-fx-border-color: black;");
        String color = UIUtils.getNextColor();
        labelSection.setStyle("-fx-background-color: " + color + ";");
        labelSection.prefWidthProperty().bind(pasLayout.widthProperty());
        labelSection.setOnMouseClicked(e -> {
          mainWindow.coordinator.runFilter.addSectionFilter(ps.values.get(ps.values.size()-1));
          mainWindow.coordinator.queryProcessRunAndUpdateUI();
        });
        pasLayout.getChildren().add(labelSection);
        
        // Globals
        if (ps.values.get(ps.values.size()-1).endsWith(".data")) {
          TreeMap<String, VariableDelta> globalVariables = new TreeMap<>();
          for (VariableDelta v : variables.values()) {
            if (v.scope.equals(UIUtils.GLOBAL) && !v.type.contains("const")) 
              globalVariables.put(UIUtils.GLOBAL + "," + v.name, v);
          }
          if (globalVariables.size() > 0) pasLayout.getChildren()
            .add(UIPASVariableTable.createTable(mainWindow,
            mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), 
            globalVariables, color, -1, null, null));
        }
        
        // Read-only globals
        if (ps.values.get(ps.values.size()-1).endsWith(".rodata")) {
          TreeMap<String, VariableDelta> globalVariables = new TreeMap<>();
          for (VariableDelta v : variables.values()) {
            if (v.scope.equals(UIUtils.GLOBAL) && v.type.contains("const")) 
              globalVariables.put(UIUtils.GLOBAL + "," + v.name, v);
          }     
          if (globalVariables.size() > 0) pasLayout.getChildren()
            .add(UIPASVariableTable.createTable(mainWindow,
            mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), 
            globalVariables, color, -1, null, null));
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
    
    Collections.sort(stack);
    
    for (ActivationRecord ar : stack) {
    
      String color = UIUtils.getNextColor();
      
      Label arHeader = new Label("0x" + Long.toHexString(ar.address) + ": " + ar.function);
      arHeader.setStyle("-fx-border-color: black;");
      arHeader.setStyle("-fx-background-color: " + color + ";");
      arHeader.prefWidthProperty().bind(pasLayout.widthProperty());
      pasLayout.getChildren().add(arHeader);
      
//      String returnAddressText = "Return Address: ";
//      returnAddressText += ar.returnAddress;
//      Label returnAddressLabel = new Label(returnAddressText);
//      returnAddressLabel.setStyle("-fx-border-color: black;");
//      returnAddressLabel.setStyle("-fx-background-color: " + color + ";");
//      returnAddressLabel.prefWidthProperty().bind(pasLayout.widthProperty());
//      pasLayout.getChildren().add(returnAddressLabel);
      
      TreeMap<String, VariableDelta> localVariables = new TreeMap<>();
      for (VariableDelta v : variables.values()) {
        if (v.scope.equals(ar.function)) localVariables.put(ar.function + "," + v.name, v);
      }     
      if (localVariables.size() > 0) pasLayout.getChildren()
        .add(UIPASVariableTable.createTable(mainWindow,
        mainWindow.getTabWindow(SubProgram.toString(SubProgram.PAS)), 
        localVariables, color, ar.address, ar.dynamicLink, ar.returnAddress));
    }
    
    // HEAP
    Label heapHeader = new Label("Heap");
    heapHeader.setStyle("-fx-border-color: black;");
    heapHeader.setStyle("-fx-background-color: black;");
    heapHeader.setTextFill(Color.web("#cccccc"));
    heapHeader.prefWidthProperty().bind(pasLayout.widthProperty());
    pasLayout.getChildren().add(heapHeader);
    
    List<VariableDelta> varList = new ArrayList<>(variables.values());
    Collections.sort(varList);
    
    GridPane heapTable = new GridPane();
    
    int row = 0;
    for (VariableDelta variable : varList) {
    
      long functionAddress = 0;
      for (ActivationRecord ar : stack) {
        if (ar.function.equals(variable.scope)) {
          functionAddress = ar.address;
          break;
        }
      }
    
      if (variable.pointsTo >= 0 && variable.pointsTo < 1000000000) { // TODO: this is nonsense pretty sure.
        Label labelAddress = new Label("0x" + Long.toHexString(variable.pointsTo));
        Pane addressContainer = new Pane();
        addressContainer.getChildren().add(labelAddress);
//        addressContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, valueStandin));
        heapTable.add(addressContainer, 0, row);
      
        Label labelValue = new Label(variable.value);
        Pane valueContainer = new Pane();
        valueContainer.getChildren().add(labelValue);
//      valueContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, valueStandin));
        heapTable.add(valueContainer, 1, row);
        
        if (row % 2 != 0) {
          addressContainer.setStyle("-fx-background-color: #bbbbbb;");
          valueContainer.setStyle("-fx-background-color: #bbbbbb;");
        }
        
        ++row;
      }
    }

    heapTable.setHgap(10);
    
    pasLayout.getChildren().add(heapTable);
    
    AnchorPane.setLeftAnchor(pasLayout, 10.0);
    AnchorPane.setRightAnchor(pasLayout, 10.0);
    layout.getChildren().add(pasLayout);
    
    scrollPane.setContent(layout);
     ((Group) scene.getRoot()).getChildren().add(scrollPane);

    return scrollPane;
  }

}
