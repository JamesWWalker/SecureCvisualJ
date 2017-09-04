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
                                     TreeMap<String, VariableDelta> variables,
                                     String color) 
  {
    GridPane table = new GridPane();
    
    ColumnConstraints column = new ColumnConstraints(10); // left column for color-coding
    table.getColumnConstraints().add(column);
    
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
    
    table.add(headerColor, 0, 0, 1, 1);
    table.add(headerAddress, 1, 0, 1, 1);
    table.add(headerName, 2, 0, 1, 1);
    table.add(headerType, 3, 0, 1, 1);
    table.add(headerSize, 4, 0, 1, 1);
    table.add(headerValue, 5, 0, 1, 1);
    
    int row = 1;
    Set<String> keys = variables.keySet();
    for (String key : keys) {
      VariableDelta variable = variables.get(key);
      
      Pane colorPane = new Pane();
      colorPane.setStyle("-fx-background-color: " + color + ";");
      table.add(colorPane, 0, row, 1, 1);
      
      Label labelAddress = new Label("0x" + Long.toHexString(variable.address));
      Pane addressContainer = new Pane();
      addressContainer.getChildren().add(labelAddress);
      addressContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(addressContainer, 1, row, 1, 1);
      
      Label labelName = new Label(variable.name);
      Pane nameContainer = new Pane();
      nameContainer.getChildren().add(labelName);
      nameContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(nameContainer, 2, row, 1, 1);
      
      Label labelType = new Label(variable.type);
      Pane typeContainer = new Pane();
      typeContainer.getChildren().add(labelType);
      typeContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(typeContainer, 3, row, 1, 1);
      
      Label labelSize = new Label(variable.size);
      Pane sizeContainer = new Pane();
      sizeContainer.getChildren().add(labelSize);
      sizeContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(sizeContainer, 4, row, 1, 1);
      
      Label labelValue = new Label(variable.value);
      Pane valueContainer = new Pane();
      valueContainer.getChildren().add(labelValue);
      valueContainer.setOnMouseClicked(e -> displayVariableRepresentation(variable.type, variable.value));
      table.add(valueContainer, 5, row, 1, 1);
      
      if (row % 2 != 0) {
        addressContainer.setStyle("-fx-background-color: #bbbbbb;");
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
