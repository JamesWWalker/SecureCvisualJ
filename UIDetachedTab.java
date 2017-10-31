import java.util.*;
import javafx.application.Application;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;

public class UIDetachedTab {

  public String id;
  public StackPane layout;
  public Scene scene = null;
  public String title;
  public Stage window;
  
  private CoordinatorMaster coordinator;
  private DoubleProperty fontSize = new SimpleDoubleProperty(10);
  private UIMainWindow mainWindow;
  
  
  public void setContent(Node content) {
    layout.getChildren().clear();
    layout.getChildren().add(content);
  }
  

  public UIDetachedTab(UIMainWindow mainWindowIn, 
                       CoordinatorMaster coordinatorIn, 
                       Node content, 
                       String titleIn)
  {
    mainWindow = mainWindowIn;
    window = new Stage();
    
    coordinator = coordinatorIn;
    
    title = titleIn;
    window.setTitle(title);
    
    layout = new StackPane();
    layout.getChildren().add(content);
  }
  
  
  public void display() {
    
    // disable close button
    window.setOnCloseRequest(e -> {
      e.consume();
      mainWindow.reattachTab(this);
      window.close(); 
    });
    
    scene = new Scene(layout, 300, 250);
    
    // scene size change listeners
    scene.widthProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
        UIUtils.calculateFontSize(layout, scene.getWidth(), scene.getHeight());
      }
    });
    scene.heightProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
        UIUtils.calculateFontSize(layout, scene.getWidth(), scene.getHeight());
      }
    });
    
    window.setScene(scene);
    window.show();
  }
  
  
  public String saveConfig() {
    String config = "";
    config += title + "X:" + Double.toString(window.getX()) + System.lineSeparator();
    config += title + "Y:" + Double.toString(window.getY()) + System.lineSeparator();
    config += title + "Width:" + Double.toString(window.getWidth()) + System.lineSeparator();
    config += title + "Height:" + Double.toString(window.getHeight()) + System.lineSeparator();
    
    return config;
  }
  
  
  public void loadConfig(List<String> config) {
    for (String line : config) {
      String[] parameters = line.trim().split(":");
      if (parameters.length > 1) {
        if (parameters[0].equals(title + "X")) window.setX(Double.parseDouble(parameters[1]));
        else if (parameters[0].equals(title + "Y")) window.setY(Double.parseDouble(parameters[1]));
        else if (parameters[0].equals(title + "Width")) window.setWidth(Double.parseDouble(parameters[1]));
        else if (parameters[0].equals(title + "Height")) window.setHeight(Double.parseDouble(parameters[1]));
      }
    }
  }

}
