import java.io.*;
import java.util.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class UISourceCode {
  
  private static TextFlow assemblyDisplay;
  private static ScrollPane assemblyScrollPane;
  private static VBox layout;
  private static int previousSourceLine = -1;
  private static TextFlow sourceCodeDisplay;
  private static int sourceCodeLines = 0;
  private static ScrollPane sourceCodeScrollPane;
  
  
  public static Node loadSourceFile(String filename) throws IOException {
  
    assemblyDisplay = new TextFlow();
    assemblyScrollPane = new ScrollPane();
    layout = new VBox(10);
    sourceCodeDisplay = new TextFlow();
    sourceCodeLines = 0;
    sourceCodeScrollPane = new ScrollPane();
    
    BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
    String line = null;
    while ((line = bufferedReader.readLine()) != null) {
      Text sourceLine = new Text(Integer.toString(sourceCodeLines+1) + " " + line + "\n");
      sourceLine.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
      sourceLine.setUserData(new Integer(sourceCodeLines+1));
      sourceCodeDisplay.getChildren().add(sourceLine);
      ++sourceCodeLines;
    }
    bufferedReader.close();
    
    assemblyScrollPane.setMinHeight(70);
    assemblyScrollPane.setContent(assemblyDisplay);
    sourceCodeScrollPane.setContent(sourceCodeDisplay);
    layout.getChildren().addAll(assemblyScrollPane, sourceCodeScrollPane);
    
    return layout;
  }
  

  public static Node buildSC(Scene scene, int sourceLine, String assembly) {
  
    if (sourceCodeLines > 0 && sourceLine > 0 && sourceLine < sourceCodeLines)  {
      if (previousSourceLine >= 0) {
        ((Text)UIUtils.getByUserData(sourceCodeDisplay, previousSourceLine)).
          setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        ((Text)UIUtils.getByUserData(sourceCodeDisplay, previousSourceLine)).setFill(Color.BLACK);
      }
      ((Text)UIUtils.getByUserData(sourceCodeDisplay, sourceLine)).
        setFont(Font.font("Monospace", FontWeight.BOLD, 14));
      ((Text)UIUtils.getByUserData(sourceCodeDisplay, sourceLine)).setFill(Color.ORANGE);
      previousSourceLine = sourceLine;
    }
    
    if (assembly != null) {
      assemblyDisplay.getChildren().clear();
      Text assemblyText = new Text(assembly);
      assemblyText.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
      assemblyDisplay.getChildren().add(assemblyText);
    }
    
    return layout;
  }
  
}
