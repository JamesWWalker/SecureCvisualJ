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
  

  public static Node buildPAS(UIMainWindow mainWindow,
                              Scene scene,
                              DetailLevel detailLevel,
                              List<ActivationRecord> stack,
                              TreeMap<String, String> registers,
                              TreeMap<String, VariableDelta> variables,
                              ArrayList<ProgramSection> sections)
  {
    final ScrollPane scrollPane = new ScrollPane();
    final AnchorPane layout = new AnchorPane();
    layout.setPadding(new Insets(10, 10, 10, 10));
    layout.prefWidthProperty().bind(mainWindow.getTabWindow(
      SubProgram.toString(SubProgram.PAS)).widthProperty());
    layout.prefHeightProperty().bind(mainWindow.getTabWindow(
      SubProgram.toString(SubProgram.PAS)).heightProperty());

    if (detailLevel == DetailLevel.NOVICE) {
      
      TableView variableTable = UIPASVariableTable.createTable(mainWindow.getTabWindow(
        SubProgram.toString(SubProgram.PAS)), variables);
      
      AnchorPane.setTopAnchor(variableTable, 10.0);
      AnchorPane.setLeftAnchor(variableTable, 10.0);
      AnchorPane.setRightAnchor(variableTable, 10.0);
      AnchorPane.setBottomAnchor(variableTable, 10.0);
      layout.getChildren().add(variableTable);
 
      scrollPane.setContent(layout);
      ((Group) scene.getRoot()).getChildren().add(scrollPane);
      
      return scrollPane;
    }
    
    // FUNCTIONS+VARIABLES
    VBox variableLayout = new VBox();
    
    // TODO: globals
    
    for (ActivationRecord ar : stack) {
      
      Label arHeader = new Label("ADDRESS: " + ar.function);      // TODO: color-coding etc.?
      arHeader.setStyle("-fx-border-color: black;");
      arHeader.prefWidthProperty().bind(variableLayout.widthProperty());;
      variableLayout.getChildren().add(arHeader);
      
      TreeMap<String, VariableDelta> localVariables = new TreeMap<>();
      for (VariableDelta v : variables.values()) {
        if (v.scope.equals(ar.function)) localVariables.put(ar.function + "," + v.name, v);
      }     
      if (localVariables.size() > 0) variableLayout.getChildren()
        .add(UIPASVariableTable.createTable(mainWindow.getTabWindow(
        SubProgram.toString(SubProgram.PAS)), localVariables));
    }
    
    AnchorPane.setLeftAnchor(variableLayout, 10.0);
    AnchorPane.setRightAnchor(variableLayout, 10.0);
    layout.getChildren().add(variableLayout);
    
    // TODO; heap variables
    
    // TODO
    scrollPane.setContent(layout);
     ((Group) scene.getRoot()).getChildren().add(scrollPane);
    
    return scrollPane;
  }

}
