import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

public class UIConfirmBox {

//Create variable
static boolean answer;

public static boolean display(String title, String message) {
    Stage window = new Stage();
    window.initModality(Modality.APPLICATION_MODAL);
    window.setTitle(title);
    window.setMinWidth(250);
    Label label = new Label();
    label.setText(message);

    //Create two buttons
    Button yesButton = new Button("Yes");
    Button noButton = new Button("No");

    //Clicking will set answer and close window
    yesButton.setOnAction(e -> {
        answer = true;
        window.close();
    });
    noButton.setOnAction(e -> {
        answer = false;
        window.close();
    });

    VBox layout = new VBox(10);
    HBox subLayout = new HBox(30);

    //Add buttons
    subLayout.getChildren().addAll(yesButton, noButton);
    subLayout.setAlignment(Pos.CENTER);
    layout.getChildren().addAll(label, subLayout);
    layout.setAlignment(Pos.CENTER);
    Scene scene = new Scene(layout);
    window.setScene(scene);
    window.showAndWait();

    //Make sure to return answer
    return answer;
}

}
