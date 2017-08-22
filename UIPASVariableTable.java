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
                                     TreeMap<String, VariableDelta> variables) 
  {
    GridPane table = new GridPane();
    
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
    
    table.add(headerAddress, 0, 0, 1, 1);
    table.add(headerName, 1, 0, 1, 1);
    table.add(headerType, 2, 0, 1, 1);
    table.add(headerSize, 3, 0, 1, 1);
    table.add(headerValue, 4, 0, 1, 1);
    
    int row = 1;
    Set<String> keys = variables.keySet();
    for (String key : keys) {
      VariableDelta variable = variables.get(key);
      
      Label labelAddress = new Label("0x" + Long.toHexString(variable.address));
      Pane addressContainer = new Pane();
      addressContainer.getChildren().add(labelAddress);
      table.add(addressContainer, 0, row, 1, 1);
      
      Label labelName = new Label(variable.name);
      Pane nameContainer = new Pane();
      nameContainer.getChildren().add(labelName);
      table.add(nameContainer, 1, row, 1, 1);
      
      Label labelType = new Label(variable.type);
      Pane typeContainer = new Pane();
      typeContainer.getChildren().add(labelType);
      table.add(typeContainer, 2, row, 1, 1);
      
      Label labelSize = new Label("TODO");
      Pane sizeContainer = new Pane();
      sizeContainer.getChildren().add(labelSize);
      table.add(sizeContainer, 3, row, 1, 1);
      
      Label labelValue = new Label(variable.value);
      Pane valueContainer = new Pane();
      valueContainer.getChildren().add(labelValue);
      table.add(valueContainer, 4, row, 1, 1);
      
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
  

}
