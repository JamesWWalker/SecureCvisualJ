import javafx.event.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

// TODO: Add speed slider and maybe seek slider
public class UIPlayControls {

  public Stage window;

  private CoordinatorMaster coordinator;
  private Image imageStart, imageBack, imagePlay, imageNext, imageEnd;
  private double xOffset = 0;
  private double yOffset = 0;
  
  
  public UIPlayControls(CoordinatorMaster coordinatorIn) {
    coordinator = coordinatorIn;
    imageStart = new Image("assets/playctl_start.png", 32, 32, false, false);
    imageBack = new Image("assets/playctl_back.png", 32, 32, false, false);
    imagePlay = new Image("assets/playctl_play.png", 32, 32, false, false);
    imageNext = new Image("assets/playctl_next.png", 32, 32, false, false);
    imageEnd = new Image("assets/playctl_end.png", 32, 32, false, false);
  }
  

  public void display() {
    window = new Stage();

    window.initStyle(StageStyle.UNDECORATED);
    window.setTitle("Play Controls");
    window.setMinWidth(250);

    HBox root = new HBox(20);

    root.setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
      xOffset = event.getSceneX();
      yOffset = event.getSceneY();
      }
    });
    root.setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
      window.setX(event.getScreenX() - xOffset);
      window.setY(event.getScreenY() - yOffset);
      }
    });

    // TODO: actions
    Button buttonStart = new Button("", new ImageView(imageStart));
    Button buttonBack = new Button("", new ImageView(imageBack));
    Button buttonPlay = new Button("", new ImageView(imagePlay));
    Button buttonNext = new Button("", new ImageView(imageNext));
    Button buttonEnd = new Button("", new ImageView(imageEnd));
    
    root.getChildren().addAll(buttonStart, buttonBack, buttonPlay, buttonNext, buttonEnd);
    root.setAlignment(Pos.CENTER);
    
    Scene scene = new Scene(root, 360, 60);
    window.setScene(scene);
    window.show();
  }

}
