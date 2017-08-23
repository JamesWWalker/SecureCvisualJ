import java.util.*;
import javafx.scene.*;
import javafx.scene.layout.*;

public class UIUtils {

  public static final String GLOBAL = "*G*";
  
  private static List<String> colorWheel;
  private static int colorWheelIndex = 0;
  

  public static Node getByUserData(Pane parent, Object data) {
    for (Node n : parent.getChildren()) {
      if (data.equals(n.getUserData())) return n;
    }
    return null ;
  }
  
  
  public static void initializeColorWheel() {
    colorWheel = new ArrayList<>();
    colorWheel.add("#ff0000");      // red
    colorWheel.add("#ffa500");      // orange
    colorWheel.add("#ffff00");      // yellow
    colorWheel.add("#00ff00");      // green
    colorWheel.add("#00ffff");      // teal
    colorWheel.add("#ff69b4");      // pink
    colorWheel.add("#ff00ff");      // magenta
  }
  
  
  public static void resetColorIndex() { colorWheelIndex = 0; }
  
  
  public static String getNextColor() {
    String color = colorWheel.get(colorWheelIndex);
    ++colorWheelIndex;
    if (colorWheelIndex >= colorWheel.size()) colorWheelIndex = 0;
    return color;
  }

}
