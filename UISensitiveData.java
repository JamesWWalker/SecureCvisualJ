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
                                   Scene scene)
  {
    final ScrollPane scrollPane = new ScrollPane();
    
    final AnchorPane layout = new AnchorPane();
    layout.setPadding(new Insets(10, 10, 10, 10));
    layout.prefWidthProperty().bind(mainWindow.getTabWindow(
      SubProgram.toString(SubProgram.SD)).widthProperty());
    layout.prefHeightProperty().bind(mainWindow.getTabWindow(
      SubProgram.toString(SubProgram.SD)).heightProperty());

    HBox pasLayout = new HBox();
    GridPane table = new GridPane();
    
    Label labelTest = new Label("TEST");
//    Pane testContainer = new Pane();
//    testContainer.getChildren().add(testAddress);
    table.add(labelTest, 0, 0, 1, 1);
    // add here
    
    pasLayout.getChildren().add(table);
    
    AnchorPane.setLeftAnchor(pasLayout, 10.0);
    AnchorPane.setRightAnchor(pasLayout, 10.0);
    layout.getChildren().add(pasLayout);

    scrollPane.setContent(layout);
    ((Group) scene.getRoot()).getChildren().add(scrollPane);

    return scrollPane;  
  }

}
