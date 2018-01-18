import java.math.*;
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
                                     String dynamicLink,
                                     String returnAddress)
  {
    VariableDelta returnPlaceholder = null;
    VariableDelta linkPlaceholder = null;
    if (returnAddress != null) returnPlaceholder = 
      new VariableDelta("int", "", "Return Addr", returnAddress, functionAddress+4);
    if (dynamicLink != null) linkPlaceholder = 
      new VariableDelta("int", "", "Dynamic Link", dynamicLink, functionAddress);
      
    long offsetStart = functionAddress;
  
    List<VariableDelta> variables = new ArrayList<>(varTree.values());
    if (returnPlaceholder != null && linkPlaceholder != null) {
      variables.add(returnPlaceholder);
      variables.add(linkPlaceholder);
    }
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
    Label headerPointsTo = new Label("Points To");
    headerPointsTo.setStyle("-fx-font-weight: bold;");
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
      table.add(headerPointsTo, 5, 0, 1, 1);
      table.add(headerValue, 6, 0, 1, 1);
    }
    else {
      table.add(headerColor, 0, 0, 1, 1);
      table.add(headerAddress, 1, 0, 1, 1);
      table.add(headerOffset, 2, 0, 1, 1);
      table.add(headerName, 3, 0, 1, 1);
      table.add(headerType, 4, 0, 1, 1);
      table.add(headerSize, 5, 0, 1, 1);
      table.add(headerPointsTo, 6, 0, 1, 1);
      table.add(headerValue, 7, 0, 1, 1);
    }
    
    int row = 1;
    for (VariableDelta variable : variables) {
    
      if (functionAddress < 0 && row == 1) offsetStart = variable.address;

      Pane colorPane = new Pane();
      colorPane.setStyle("-fx-background-color: " + color + ";");
      table.add(colorPane, 0, row, 1, 1);
      
      Label labelAddress;
//      if (!variable.name.equals("Return Addr"))
      labelAddress = new Label("0x" + Long.toHexString(variable.address));
//      else labelAddress = new Label("");
      Pane addressContainer = new Pane();
      addressContainer.getChildren().add(labelAddress);
      addressContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(addressContainer, 1, row, 1, 1);
      
      int column = 2;
      
      long offset = variable.address - offsetStart;
      String offsetStr = Long.toString(offset);
      if (offset > 0) offsetStr = "+" + offsetStr;
      Label labelOffset = new Label(offsetStr);
      Pane offsetContainer = new Pane();
      if (showOffsets) {
        offsetContainer.getChildren().add(labelOffset);
        offsetContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
        table.add(offsetContainer, column, row, 1, 1);
        ++column;
      }
      
      Label labelName = new Label(variable.name);
      Pane nameContainer = new Pane();
      nameContainer.getChildren().add(labelName);
      nameContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(nameContainer, column, row, 1, 1);
      
      ++column;
      
      Label labelType;
      if (!variable.name.equals("Return Addr") && !variable.name.equals("Dynamic Link"))
        labelType = new Label(variable.type);
      else labelType = new Label("");
      Pane typeContainer = new Pane();
      typeContainer.getChildren().add(labelType);
      typeContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(typeContainer, column, row, 1, 1);
      
      ++column;
      
      Label labelSize;
      if (!variable.name.equals("Return Addr") && !variable.name.equals("Dynamic Link"))
        labelSize = new Label(variable.size);
      else labelSize = new Label("");
      Pane sizeContainer = new Pane();
      sizeContainer.getChildren().add(labelSize);
      sizeContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(sizeContainer, column, row, 1, 1);
      
      ++column;
      
      Label labelPointsTo;
      if (!variable.name.equals("Return Addr") && !variable.name.equals("Dynamic Link") && variable.pointsTo > 0) 
        labelPointsTo = new Label("0x" + Long.toHexString(variable.pointsTo));
      else labelPointsTo = new Label("-----");
      Pane pointsToContainer = new Pane();
      pointsToContainer.getChildren().add(labelPointsTo);
      pointsToContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(pointsToContainer, column, row, 1, 1);
      
      ++column;
      
      Label labelValue;
      if (variable.pointsTo > 0 && variable.pointsTo < 1000000000)  // TODO: Fix this
        labelValue = new Label("(In heap)");
      else labelValue = new Label(variable.value);
      Pane valueContainer = new Pane();
      valueContainer.getChildren().add(labelValue);
      valueContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
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
    else if (value.startsWith("'") && value.endsWith("'") && value.length() == 3) {
      UIVariableRepresentation.display(type, new Integer((int)value.charAt(1)).toString());
    }
    else if (value.startsWith("'\\x") && value.endsWith("'") && value.length() == 6) {
      UIVariableRepresentation.display(type, new BigInteger(value.substring(3, 5), 16).toString());
    }
    else if (value.equals("'\\0'")) UIVariableRepresentation.display(type, "0");
  }
  

}
