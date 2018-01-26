import java.util.*;
import javafx.scene.*;
import javafx.scene.layout.*;

public class UIUtils {

  public static final String GLOBAL = "*G*";
  
  public static int architecture = 64; // number of bits
  
  private static List<String> colorWheel;
  private static int colorWheelIndex = 0;
  
  public static final int SD_EV_MEMORYLOCKED = 0;
  public static final int SD_EV_VALUESET = 1;
  public static final int SD_EV_VALUECLEARED = 2;
  public static final int SD_EV_MEMORYUNLOCKED = 3;
  

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
  
  
  public static double calculateFontSize(double fontSize, double windowWidth, double windowHeight) {
      return ((windowWidth + windowHeight) / 70) * fontSize;
  }

}
