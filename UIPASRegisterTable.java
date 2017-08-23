import java.util.*;
import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;

public class UIPASRegisterTable {
  
  
  public static GridPane createTable(UIMainWindow mainWindow,
                                     Stage window,
                                     TreeMap<String, String> registers)
  {
    GridPane table = new GridPane();
    
    Label headerName = new Label("Register");
    headerName.setStyle("-fx-font-weight: bold;");
    Label headerValue = new Label("Value");
    headerValue.setStyle("-fx-font-weight: bold;");
    
    table.add(headerName, 0, 0, 1, 1);
    table.add(headerValue, 1, 0, 1, 1);
    
    int row = 1;
    Set<String> keys = registers.keySet();
    for (String key : keys) {
      Label labelRegister = new Label(key);
      Pane registerContainer = new Pane();
      registerContainer.getChildren().add(labelRegister);
      registerContainer.setOnMouseClicked(e -> {
        mainWindow.coordinator.runFilter.addRegisterFilter(key);
        mainWindow.coordinator.queryProcessRunAndUpdateUI();
      });
      table.add(registerContainer, 0, row, 1, 1);
      
      Label labelValue = new Label(registers.get(key));
      Pane valueContainer = new Pane();
      valueContainer.setOnMouseClicked(e -> {
        mainWindow.coordinator.runFilter.addRegisterFilter(key);
        mainWindow.coordinator.queryProcessRunAndUpdateUI();
      });
      valueContainer.getChildren().add(labelValue);
      table.add(valueContainer, 1, row, 1, 1);
      
      if (row % 2 != 0) {
        registerContainer.setStyle("-fx-background-color: #bbbbbb;");
        valueContainer.setStyle("-fx-background-color: #bbbbbb;");
      }
      
      ++row;
    }
    
    table.setHgap(10);
    
    return table;
    
  } // createTable()
  

}
