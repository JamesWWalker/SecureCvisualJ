import java.util.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.util.*;

public class UIProgramAddressSpace {
  

  public static Node buildPAS(Scene scene,
                              DetailLevel detailLevel,
                              List<ActivationRecord> stack,
                              TreeMap<String, String> registers,
                              TreeMap<String, VariableDelta> variables,
                              ArrayList<ProgramSection> sections)
  {
    final AnchorPane layout = new AnchorPane();

    if (detailLevel == DetailLevel.NOVICE) {
      
      TableView variableTable = UIPASVariableTable.createTable(variables);
 
      layout.setPadding(new Insets(10, 10, 10, 10));
      layout.prefWidthProperty().bind(scene.widthProperty());
      layout.prefHeightProperty().bind(scene.heightProperty());
      AnchorPane.setTopAnchor(variableTable, 10.0);
      AnchorPane.setLeftAnchor(variableTable, 10.0);
      AnchorPane.setRightAnchor(variableTable, 10.0);
      AnchorPane.setBottomAnchor(variableTable, 10.0);
      layout.getChildren().add(variableTable);
 
      ((Group) scene.getRoot()).getChildren().add(layout);
    }
    
    return layout;
  }

}
