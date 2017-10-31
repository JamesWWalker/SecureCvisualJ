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

public class UISensitiveData {
  

  public static ScrollPane buildSD(UIMainWindow mainWindow,
                                   Scene scene,
                                   List<SensitiveDataState> states,
                                   SensitiveDataState lastState)
  {
    final ScrollPane scrollPane = new ScrollPane();
    
    final AnchorPane layout = new AnchorPane();
    layout.setPadding(new Insets(10, 10, 10, 10));
    layout.prefWidthProperty().bind(mainWindow.getTabWindow(
      SubProgram.toString(SubProgram.SD)).widthProperty().subtract(30));
    layout.prefHeightProperty().bind(mainWindow.getTabWindow(
      SubProgram.toString(SubProgram.SD)).heightProperty().subtract(40));

    GridPane table = new GridPane();
    GridPane flowchart = new GridPane();
    
    // Progression table
    
    // Headers
    int numColumns = 0;
    if (lastState != null) {
      List<String> variableNames = new ArrayList<>(lastState.variables.keySet());
      numColumns = variableNames.size();
      for (int col = 0; col < numColumns; ++col) {
        String variableName = variableNames.get(col).replaceAll(",", ":");
        Label headerLabel = new Label(variableName);
        headerLabel.setTextFill(Color.web("#cccccc"));
        Pane headerPane = new Pane();
        headerPane.setStyle("-fx-border-color: black;");
        headerPane.setStyle("-fx-background-color: black;");
        headerPane.getChildren().add(headerLabel);
        // TODO: mouse click
        table.add(headerPane, col, 0, 1, 1);
      }
    }

    if (states.size() > 0) {      
      int row = 1;
      for (SensitiveDataState state : states) {
        if (state.coreSizeZeroedHere) {
          Label coreZeroedLabel = new Label("Core zeroed");
          Tooltip t = new Tooltip("Core size zeroed");
          Tooltip.install(coreZeroedLabel, t);
          boolean stateSecure = true;
          for (String variableName : state.variables.keySet()) {
            if (!state.variables.get(variableName).isSecure) {
              stateSecure = false;
              break;
            }
          }
          Pane coreZeroedPane;
          if (stateSecure) {
            coreZeroedPane = new Pane();
            coreZeroedPane.getChildren().add(coreZeroedLabel);
            coreZeroedPane.setStyle("-fx-border-color: black;");
            coreZeroedPane.setStyle("-fx-background-color: green;");
            coreZeroedLabel.setTextFill(Color.web("#ffffff"));
          }
          else {
            coreZeroedPane = new Pane();
            coreZeroedPane.getChildren().add(coreZeroedLabel);
            coreZeroedPane.setStyle("-fx-border-color: black;");
            coreZeroedPane.setStyle("-fx-background-color: red;");
          }          
          if (numColumns == 0) numColumns = 1;
          
          table.add(coreZeroedPane, 0, row, 1, numColumns);
        }
        else {
          int col = 0;
          for (String variableName : state.variables.keySet()) {
            SensitiveDataVariable variable = state.variables.get(variableName);
            Label varLabel = new Label(variable.shortMessage);
            Tooltip t = new Tooltip(variable.message);
            Tooltip.install(varLabel, t);
            Pane varPane;
            if (variable.isSecure) {
              varPane = new Pane();
              varPane.getChildren().add(varLabel);
              varPane.setStyle("-fx-border-color: black;");
              varPane.setStyle("-fx-background-color: green;");
              varLabel.setTextFill(Color.web("#ffffff"));
            }
            else {
              varPane = new Pane();
              varPane.getChildren().add(varLabel);
              varPane.setStyle("-fx-border-color: black;");
              varPane.setStyle("-fx-background-color: red;");
            }
            table.add(varPane, col, row, 1, 1);
            ++col;
          }
        }
        ++row;
      }
    }

    
    // TODO
    
    Label labelTest2 = new Label("TEST2");
    flowchart.add(labelTest2, 0, 0, 1, 1);
    // add here
    
    table.setHgap(10);
    
    AnchorPane.setLeftAnchor(table, 10.0);
    AnchorPane.setRightAnchor(flowchart, 10.0);
    layout.getChildren().add(table);
    layout.getChildren().add(flowchart);

    scrollPane.setContent(layout);
    ((Group) scene.getRoot()).getChildren().add(scrollPane);

/*
    QColor color;
    if (var.isSecure) color = Qt::green;
    else color = Qt::red;

    for (int n = 1; n < 6; ++n) svFlowchart->item(n, 0)->setBackgroundColor(Qt::gray);

    if (state.coreSizeZeroed)
        svFlowchart->item(1, 0)->setBackgroundColor(color);
    if (var.stepsApplied[SECVAR_EVTYPE_VARIABLE_MEMORY_LOCKED])
        svFlowchart->item(2, 0)->setBackgroundColor(color);
    if (var.stepsApplied[SECVAR_EVTYPE_VARIABLE_VALUE_SET])
        svFlowchart->item(3, 0)->setBackgroundColor(color);
    if (var.stepsApplied[SECVAR_EVTYPE_VARIABLE_VALUE_CLEARED])
        svFlowchart->item(4, 0)->setBackgroundColor(color);
    if (var.stepsApplied[SECVAR_EVTYPE_VARIABLE_MEMORY_UNLOCKED])
        svFlowchart->item(5, 0)->setBackgroundColor(color);
*/

    return scrollPane;  
  }

}
