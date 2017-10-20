import java.util.*;
import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;

public class UIPASVariableTable {

  
  public static GridPane createTable(UIMainWindow mainWindow,
                                     Stage window,
                                     TreeMap<String, VariableDelta> varTree,
                                     String color,
                                     long functionAddress,
                                     String returnAddress,
                                     String funcName) 
  {
    VariableDelta returnPlaceholder = null;
    VariableDelta functionPlaceholder = null;
    if (returnAddress != null) returnPlaceholder = 
      new VariableDelta("int", "", "Return Addr", returnAddress, functionAddress+4);
    if (functionAddress >= 0 && funcName != null) functionPlaceholder = 
      new VariableDelta("int", "", funcName, "", functionAddress);
      
    long offsetStart = functionAddress;
  
    List<VariableDelta> variables = new ArrayList<>(varTree.values());
    variables.add(returnPlaceholder);
    variables.add(functionPlaceholder);
    Collections.sort(variables);
  
    GridPane table = new GridPane();
    
    ColumnConstraints columnCon = new ColumnConstraints(10); // left column for color-coding
    table.getColumnConstraints().add(columnCon);
    
    boolean showOffsets = mainWindow.coordinator.runFilter.getShowOffsets();
    
    Pane headerColor = new Pane();
    headerColor.setStyle("-fx-background-color: " + color + ";");
//    headerColor.setPrefWidth(10);
//    headerColor.setPrefWidth(50);
    Label headerAddress = new Label("Address");
    headerAddress.setStyle("-fx-font-weight: bold;");
    Label headerName = new Label("Name");
    headerName.setStyle("-fx-font-weight: bold;");
    Label headerType = new Label("Type");
    headerType.setStyle("-fx-font-weight: bold;");
    Label headerSize = new Label("Size");
    headerSize.setStyle("-fx-font-weight: bold;");
    Label headerValue = new Label("Value");
    headerValue.setStyle("-fx-font-weight: bold;");
    Label headerOffset = new Label("Offset");
    headerOffset.setStyle("-fx-font-weight: bold;");

    if (!showOffsets) {    
      table.add(headerColor, 0, 0, 1, 1);
      table.add(headerAddress, 1, 0, 1, 1);
      table.add(headerName, 2, 0, 1, 1);
      table.add(headerType, 3, 0, 1, 1);
      table.add(headerSize, 4, 0, 1, 1);
      table.add(headerValue, 5, 0, 1, 1);
    }
    else {
      table.add(headerColor, 0, 0, 1, 1);
      table.add(headerAddress, 1, 0, 1, 1);
      table.add(headerOffset, 2, 0, 1, 1);
      table.add(headerName, 3, 0, 1, 1);
      table.add(headerType, 4, 0, 1, 1);
      table.add(headerSize, 5, 0, 1, 1);
      table.add(headerValue, 6, 0, 1, 1);
    }
    
    int row = 1;
    for (VariableDelta variable : variables) {
    
      if (functionAddress < 0 && row == 1) offsetStart = variable.address;
    
      String pointsToString = "0x" + Long.toHexString(variable.pointsTo);
      String valueStandin = variable.value;
      if (mainWindow.coordinator.runFilter.getDetailLevel() != DetailLevel.NOVICE &&
          variable.pointsTo >= 0 && variable.pointsTo < 1000000000) // TODO: Fix this.
      {
        valueStandin = "0x" + Long.toHexString(variable.pointsTo);
      }

      Pane colorPane = new Pane();
      colorPane.setStyle("-fx-background-color: " + color + ";");
      table.add(colorPane, 0, row, 1, 1);
      
      Label labelAddress;
//      if (!variable.name.equals("Return Addr"))
        labelAddress = new Label("0x" + Long.toHexString(variable.address));
//      else labelAddress = new Label("");
      Pane addressContainer = new Pane();
      addressContainer.getChildren().add(labelAddress);
      if (variable.pointsTo < 0)
        addressContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      else
        addressContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, pointsToString));
      table.add(addressContainer, 1, row, 1, 1);
      
      int column = 2;
      
      long offset = offsetStart - variable.address;
      String offsetStr = Long.toString(offset);
      if (offset > 0) offsetStr = "+" + offsetStr;
      Label labelOffset = new Label(offsetStr);
      Pane offsetContainer = new Pane();
      if (showOffsets) {
        offsetContainer.getChildren().add(labelOffset);
        if (variable.pointsTo < 0)
          offsetContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
        else
          offsetContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, pointsToString));
        table.add(offsetContainer, column, row, 1, 1);
        ++column;
      }
      
      Label labelName = new Label(variable.name);
      Pane nameContainer = new Pane();
      nameContainer.getChildren().add(labelName);
      if (variable.pointsTo < 0)
        nameContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      else
        nameContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, pointsToString));
      table.add(nameContainer, column, row, 1, 1);
      
      ++column;
      
      Label labelType;
      if (!variable.name.equals("Return Addr") && !variable.name.equals(funcName))
        labelType = new Label(variable.type);
      else labelType = new Label("");
      Pane typeContainer = new Pane();
      typeContainer.getChildren().add(labelType);
      if (variable.pointsTo < 0)
        typeContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      else
        typeContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, pointsToString));
      table.add(typeContainer, column, row, 1, 1);
      
      ++column;
      
      Label labelSize;
      if (!variable.name.equals("Return Addr") && !variable.name.equals(funcName))
        labelSize = new Label(variable.type);
      else labelSize = new Label("");
      Pane sizeContainer = new Pane();
      sizeContainer.getChildren().add(labelSize);
      if (variable.pointsTo < 0)
        sizeContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      else
        sizeContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, pointsToString));
      table.add(sizeContainer, column, row, 1, 1);
      
      ++column;
      
      Label labelValue = new Label(valueStandin);
      Pane valueContainer = new Pane();
      valueContainer.getChildren().add(labelValue);
      if (variable.pointsTo < 0)
        valueContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      else
        valueContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, pointsToString));
      table.add(valueContainer, column, row, 1, 1);
      
      if (row % 2 != 0) {
        addressContainer.setStyle("-fx-background-color: #bbbbbb;");
        offsetContainer.setStyle("-fx-background-color: #bbbbbb;");
        nameContainer.setStyle("-fx-background-color: #bbbbbb;");
        typeContainer.setStyle("-fx-background-color: #bbbbbb;");
        sizeContainer.setStyle("-fx-background-color: #bbbbbb;");
        valueContainer.setStyle("-fx-background-color: #bbbbbb;");
      }
      
      ++row;
    }

    table.setHgap(10);
    
    return table;
    
  } // createTable()
  
  
  static void displayVariableRepresentation(String type, String value) {
    if (value.matches("-*\\d*")) UIVariableRepresentation.display(type, value);
  }
  

}
