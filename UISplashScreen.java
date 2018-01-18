import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;

public class UISplashScreen extends Application {

  private CoordinatorMaster coordinator;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage window) {
    window.initStyle(StageStyle.UNDECORATED);
    window.setTitle("SecureCvisual");
    
    Button button = new Button("Start");
    button.setOnAction(e -> {
      window.close();
      coordinator = new CoordinatorMaster();
      coordinator.exec();
    });
    
    Label label = new Label("Author: James Walker\n\nThis work has been supported by the National Science Foundation under grants DUE-1245310,  DGE-1522883, DGE-1523017, IIS-1456763, and IIS-1455886.");
    label.setWrapText(true);
    label.setTextAlignment(TextAlignment.CENTER);
    
    Image image = new Image("assets/splash.png");
    ImageView imv = new ImageView();
    imv.setImage(image);

    VBox layout = new VBox();
    layout.setSpacing(50);
    layout.getChildren().addAll(imv, label, button);
    layout.setAlignment(Pos.CENTER);
    Scene scene = new Scene(layout, 300, 400);
    window.setScene(scene);
    window.show();
  }

}
