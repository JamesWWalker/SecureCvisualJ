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

public class UIPASOutputTable {
  
  
  public static GridPane createTable(UIMainWindow mainWindow,
                                     Stage window,
                                     List<String> outputs)
  {
    GridPane table = new GridPane();
    
    int row = 1;
    for (String output : outputs) {
      Label labelOutput = new Label(output);
      Pane containerOutput = new Pane();
      containerOutput.getChildren().add(labelOutput);
      table.add(containerOutput, 0, row, 1, 1);
      
      if (row % 2 != 0) containerOutput.setStyle("-fx-background-color: #bbbbbb;");
      
      ++row;
    }
    
    table.setHgap(10);
    
    return table;
    
  } // createTable()
  

}
