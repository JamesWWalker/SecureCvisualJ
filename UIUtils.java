import javafx.scene.*;
import javafx.scene.layout.*;

public class UIUtils {

  public static final String GLOBAL = "*G*";

  public static Node getByUserData(Pane parent, Object data) {
    for (Node n : parent.getChildren()) {
      if (data.equals(n.getUserData())) return n;
    }
    return null ;
  }

}
