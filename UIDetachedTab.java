import java.util.*;
import javafx.application.Application;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;

public class UIDetachedTab {

  public String id;
  public BorderPane layout;
  public Scene scene = null;
  public String title;
  public Stage window;
  
  private CoordinatorMaster coordinator;
  private double fontSize = 1.0;
  private UIMainWindow mainWindow;
  
  
  public void setContent(Node content) {
    layout.setCenter(content);
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
    
    // View menu
    Menu viewMenu = new Menu("View");
    
    MenuItem menuIncreaseFontSize = new MenuItem("Increase Font Size");
    menuIncreaseFontSize.setOnAction(e -> {
      if (fontSize < 5.0) fontSize += 0.1;
      layout.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
    });
    menuIncreaseFontSize.setAccelerator(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN));
    viewMenu.getItems().add(menuIncreaseFontSize);
    
    MenuItem menuDecreaseFontSize = new MenuItem("Decrease Font Size");
    menuDecreaseFontSize.setOnAction(e -> {
      if (fontSize > 0.1) fontSize -= 0.1;
      layout.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
    });
    menuDecreaseFontSize.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));
    viewMenu.getItems().add(menuDecreaseFontSize);
    
    //Main menu bar
    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(viewMenu);
    
    layout = new BorderPane();
    layout.setCenter(content);
    layout.setTop(menuBar);
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
        layout.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
      }
    });
    scene.heightProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
        layout.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
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
    config += title + "FontSize:" + Double.toString(fontSize) + System.lineSeparator();
    
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
        else if (parameters[0].equals(title + "FontSize")) fontSize = Double.parseDouble(parameters[1]);
      }
    }
  }

}
