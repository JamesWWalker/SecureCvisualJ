import java.util.*;
import javafx.animation.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;

// TODO: Add speed speedSlider and maybe seek speedSlider
public class UIPlayControls {

  public Stage window;

  private CoordinatorMaster coordinator;
  private Image imageStart, imageBack, imagePlay, imageNext, imageEnd;
  private boolean isPlaying;
  private long playbackSpeed;
  private Label speedCaption;
  private Slider speedSlider;
  private Timeline timeline;
  private double xOffset = 0;
  private double yOffset = 0;
  
  
  public UIPlayControls(CoordinatorMaster coordinatorIn) {
    coordinator = coordinatorIn;
    imageStart = new Image("assets/playctl_start.png", 32, 32, false, false);
    imageBack = new Image("assets/playctl_back.png", 32, 32, false, false);
    imagePlay = new Image("assets/playctl_play.png", 32, 32, false, false);
    imageNext = new Image("assets/playctl_next.png", 32, 32, false, false);
    imageEnd = new Image("assets/playctl_end.png", 32, 32, false, false);
    
    speedCaption = new Label("Playback speed: Not set");
    speedSlider = new Slider(1, 10, 0);
    speedSlider.valueProperty().addListener((obs, oldv, newv) -> {
    System.err.println("LISTENRE CALLED");
      setPlaybackSpeed(newv.intValue());
      speedCaption.setText("Playback speed: " + Long.toString(playbackSpeed) + " ms");
    });
    speedSlider.setMajorTickUnit(1);
    speedSlider.setMinorTickCount(0);
    speedSlider.setShowTickMarks(true);
    speedSlider.setSnapToTicks(true);
    
    isPlaying = false;
  }
  

  public void display() {
    window = new Stage();

    window.initStyle(StageStyle.UNDECORATED);
    window.setTitle("Play Controls");
    window.setMinWidth(250);

    VBox root = new VBox(20);
    HBox buttonPanel = new HBox(20);

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

    Button buttonStart = new Button("", new ImageView(imageStart));
    buttonStart.setOnAction(e -> {
      if (coordinator.run != null) {
        coordinator.run.jumpToBeginning();
        // TODO: push update to PAS
      }
    });
    Button buttonBack = new Button("", new ImageView(imageBack));
    buttonBack.setOnAction(e -> {
      if (coordinator.run != null) {
        coordinator.run.previous();
        // TODO: push update to PAS
      }
    });
    Button buttonPlay = new Button("", new ImageView(imagePlay));
    buttonPlay.setOnAction(e -> {
      if (coordinator.run != null) {
        if (!isPlaying) {
          timeline = new Timeline(new KeyFrame(Duration.millis(playbackSpeed), x-> {
            if (!coordinator.run.next()) stopPlayer();
          }));
          timeline.setCycleCount(Animation.INDEFINITE);
          timeline.play();
          isPlaying = true;
        }
        else {
          timeline.stop();
          isPlaying = false;
        }
      }
    });
    Button buttonNext = new Button("", new ImageView(imageNext));
    buttonNext.setOnAction(e -> {
      if (coordinator.run != null) {
        coordinator.run.next();
        // TODO: push update to PAS
      }
    });
    Button buttonEnd = new Button("", new ImageView(imageEnd));
    buttonEnd.setOnAction(e -> {
      if (coordinator.run != null) {
        coordinator.run.jumpToEnd();
        // TODO: push update to PAS
      }
    });
    
    buttonPanel.getChildren().addAll(buttonStart, buttonBack, buttonPlay, buttonNext, buttonEnd);
    buttonPanel.setAlignment(Pos.CENTER);
    
    root.getChildren().addAll(buttonPanel, speedCaption, speedSlider);
    root.setAlignment(Pos.CENTER);
    root.setStyle("-fx-padding: 20 20 20 20;");
    
    Scene scene = new Scene(root, 360, 140);
    window.setScene(scene);
    window.show();
    speedSlider.setValue(8);
  }
  
  
  public String saveConfig() {
    String config = "";
    config += "PlayerX:" + Double.toString(window.getX()) + System.lineSeparator();
    config += "PlayerY:" + Double.toString(window.getY()) + System.lineSeparator();
    config += "PlayerSpeed:" + Long.toString((int)(speedSlider.getValue() + 0.01)) + System.lineSeparator();
    
    return config;
  }
  
  
  public void loadConfig(List<String> config) {
    for (String line : config) {
      String[] parameters = line.trim().split(":");
      if (parameters.length > 1) {
        if (parameters[0].equals("PlayerX")) window.setX(Double.parseDouble(parameters[1]));
        else if (parameters[0].equals("PlayerY")) window.setY(Double.parseDouble(parameters[1]));
        else if (parameters[0].equals("PlayerSpeed")) {
          // Set it to some arbitrary value that's different from its start value (0) to ensure that
          // the listener gets triggered even in the event that it was saved at value 0 (so it isn't changing)
          speedSlider.setValue(1);
          speedSlider.setValue(Integer.parseInt(parameters[1]));
        }
      }
    }
  }
  
  
  private void stopPlayer() {
  System.err.println("CALLING STOP");
    timeline.stop();
  }
  
  
  private void setPlaybackSpeed(int value) {
    switch (value) {
      case 1:
        playbackSpeed = 10;
        break;
      case 2:
        playbackSpeed = 100;
        break;
      case 3:
        playbackSpeed = 200;
        break;
      case 4:
        playbackSpeed = 300;
        break;
      case 5:
        playbackSpeed = 400;
        break;
      case 6:
        playbackSpeed = 500;
        break;
      case 7:
        playbackSpeed = 750;
        break;
      case 8:
        playbackSpeed = 1000;
        break;
      case 9:
        playbackSpeed = 2000;
        break;
      case 10:
        playbackSpeed = 5000;
        break;
      default:
        System.err.println("WARNING: Attempted to set invalid playback speed; defaulting to 1000 ms.");
        playbackSpeed = 1000;
    }
  }

}
