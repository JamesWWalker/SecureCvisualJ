import javafx.application.Application;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;

public class UIDetachedTab {

  public HBox content;
  public String id;
  public String title;
  public Stage window;
  
  private CoordinatorMaster coordinator;
  private DoubleProperty fontSize = new SimpleDoubleProperty(10);
  private UIMainWindow mainWindow;
  private StackPane layout;
  

  public UIDetachedTab(UIMainWindow mainWindowIn, 
                       CoordinatorMaster coordinatorIn, 
                       HBox contentIn, 
                       String titleIn)
  {
    mainWindow = mainWindowIn;
    window = new Stage();
    
    coordinator = coordinatorIn;
    
    title = titleIn;
    window.setTitle(title);
    
    layout = new StackPane();
    content = contentIn;
    layout.getChildren().add(content);
  }
  
  
  public void display() {
    
    // disable close button
    window.setOnCloseRequest(e -> {
      e.consume();
      coordinator.deregisterDetachedTab(this);
      mainWindow.reattachTab(title, content);
      window.close(); 
    });
    
    Scene scene = new Scene(layout, 300, 250);
    
    // font size binding
    fontSize.bind(scene.widthProperty().add(scene.heightProperty()).divide(50));
    layout.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
    
    window.setScene(scene);
    window.show();
  }

}
