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
                                   SensitiveDataState lastState,
                                   SensitiveDataVariable sdVariableToView)
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
        headerPane.setOnMouseClicked(e -> {
          mainWindow.sdVariableToView = lastState.variables.get(variableName);
          mainWindow.coordinator.queryProcessRunAndUpdateUI();
        });
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

          table.add(coreZeroedPane, 0, row, numColumns, 1);
          ++row;
        }
        int col = 0;
        for (String variableName : lastState.variables.keySet()) {
          if (state.variables.containsKey(variableName)) {
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
            varPane.setOnMouseClicked(e -> {
              mainWindow.sdVariableToView = state.variables.get(variableName);
             mainWindow.coordinator.queryProcessRunAndUpdateUI();
            });
            table.add(varPane, col, row, 1, 1);
          }
          ++col;
        }
        ++row;
      }
    }


    // Flowchart
    Label labelFlowchartHeader = new Label("Flow Chart");
    Pane paneFlowchartHeader = new Pane();
    paneFlowchartHeader.getChildren().add(labelFlowchartHeader);
    paneFlowchartHeader.setStyle("-fx-background-color: black;");
    labelFlowchartHeader.setTextFill(Color.web("#ffffff"));
    
    Label labelFlowchartCoreZeroed = new Label("Core Zeroed");
    Pane paneFlowchartCoreZeroed = new Pane();
    paneFlowchartCoreZeroed.getChildren().add(labelFlowchartCoreZeroed);
    paneFlowchartCoreZeroed.setStyle("-fx-background-color: #8c8c8c;");
    labelFlowchartCoreZeroed.setTextFill(Color.web("#ffffff"));
    
    Label labelFlowchartMemLocked = new Label("Mem Locked");
    Pane paneFlowchartMemLocked = new Pane();
    paneFlowchartMemLocked.getChildren().add(labelFlowchartMemLocked);
    paneFlowchartMemLocked.setStyle("-fx-background-color: #8c8c8c;");
    labelFlowchartMemLocked.setTextFill(Color.web("#ffffff"));
    
    Label labelFlowchartValueSet = new Label("Value Set");
    Pane paneFlowchartValueSet = new Pane();
    paneFlowchartValueSet.getChildren().add(labelFlowchartValueSet);
    paneFlowchartValueSet.setStyle("-fx-background-color: #8c8c8c;");
    labelFlowchartValueSet.setTextFill(Color.web("#ffffff"));
    
    Label labelFlowchartValueCleared = new Label("Value Cleared");
    Pane paneFlowchartValueCleared = new Pane();
    paneFlowchartValueCleared.getChildren().add(labelFlowchartValueCleared);
    paneFlowchartValueCleared.setStyle("-fx-background-color: #8c8c8c;");
    labelFlowchartValueCleared.setTextFill(Color.web("#ffffff"));
    
    Label labelFlowchartMemUnlocked = new Label("Mem Unlocked");
    Pane paneFlowchartMemUnlocked = new Pane();
    paneFlowchartMemUnlocked.getChildren().add(labelFlowchartMemUnlocked);
    paneFlowchartMemUnlocked.setStyle("-fx-background-color: #8c8c8c;");
    labelFlowchartMemUnlocked.setTextFill(Color.web("#ffffff"));
    
    if (sdVariableToView != null && states.size() > 0) {
      String color = "#ff0000;";
      String textColor = "000000";
      if (sdVariableToView.isSecure) {
        color = "#00aa00;";
        textColor = "ffffff";
      }
      if (states.get(states.size()-1).coreSizeZeroed) {
        paneFlowchartCoreZeroed.setStyle("-fx-background-color: " + color);
        labelFlowchartCoreZeroed.setTextFill(Color.web(textColor));
      }
      if (sdVariableToView.stepsApplied[UIUtils.SD_EV_MEMORYLOCKED]) {
        paneFlowchartMemLocked.setStyle("-fx-background-color: " + color);
        labelFlowchartMemLocked.setTextFill(Color.web(textColor));
      }
      if (sdVariableToView.stepsApplied[UIUtils.SD_EV_VALUESET]) {
        paneFlowchartValueSet.setStyle("-fx-background-color: " + color);
        labelFlowchartValueSet.setTextFill(Color.web(textColor));
      }
      if (sdVariableToView.stepsApplied[UIUtils.SD_EV_VALUECLEARED]) {
        paneFlowchartValueCleared.setStyle("-fx-background-color: " + color);
        labelFlowchartValueCleared.setTextFill(Color.web(textColor));
      }
      if (sdVariableToView.stepsApplied[UIUtils.SD_EV_MEMORYUNLOCKED]) {
        paneFlowchartMemUnlocked.setStyle("-fx-background-color: " + color);
        labelFlowchartMemUnlocked.setTextFill(Color.web(textColor));
      }
    }
    
    flowchart.add(paneFlowchartHeader,       0, 0, 1, 1);
    flowchart.add(paneFlowchartCoreZeroed,   0, 1, 1, 1);
    flowchart.add(paneFlowchartMemLocked,    0, 2, 1, 1);
    flowchart.add(paneFlowchartValueSet,     0, 3, 1, 1);
    flowchart.add(paneFlowchartValueCleared, 0, 4, 1, 1);
    flowchart.add(paneFlowchartMemUnlocked,  0, 5, 1, 1);
    
    table.setHgap(10);
    
    AnchorPane.setLeftAnchor(table, 10.0);
    AnchorPane.setRightAnchor(flowchart, 10.0);
    layout.getChildren().add(table);
    layout.getChildren().add(flowchart);

    scrollPane.setContent(layout);
    ((Group) scene.getRoot()).getChildren().add(scrollPane);

    return scrollPane;  
  }

}
