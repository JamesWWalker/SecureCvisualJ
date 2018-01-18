import java.io.*;
import java.util.*;
import javafx.beans.value.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class UISourceCode {
  
  private static TextFlow assemblyDisplay;
  private static ScrollPane assemblyScrollPane;
  private static Text assemblyText;
  private static double fontSize = 1.0;
  private static VBox layout;
  private static int previousSourceLine = -1;
  private static List<Text> sourceCode = new ArrayList<>();
  private static TextFlow sourceCodeDisplay;
  private static int sourceCodeLines = 0;
  private static ScrollPane sourceCodeScrollPane;
  
  
  public static Pane parseSourceCode(List<String> code) {
    
    assemblyDisplay = new TextFlow();
    assemblyScrollPane = new ScrollPane();
    layout = new VBox(10);
    sourceCodeDisplay = new TextFlow();
    sourceCodeLines = 0;
    sourceCodeScrollPane = new ScrollPane();
    previousSourceLine = -1;
    
    for (String line : code) {
      Text sourceLine = new Text(Integer.toString(sourceCodeLines+1) + " " + line + "\n");
      sourceLine.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
      sourceLine.setUserData(new Integer(sourceCodeLines+1));
      sourceCodeDisplay.getChildren().add(sourceLine);
      sourceCode.add(sourceLine);
      ++sourceCodeLines;
    }
    
    assemblyScrollPane.setMinHeight(70);
    assemblyScrollPane.setContent(assemblyDisplay);
    sourceCodeScrollPane.setContent(sourceCodeDisplay);
    layout.getChildren().addAll(assemblyScrollPane, sourceCodeScrollPane);
    
    return layout;
    
  }
  

  public static Pane buildSC(Scene scene, int sourceLine, String assembly) {
  
    if (sourceCodeLines > 0 && sourceLine > 0 && sourceLine < sourceCodeLines)  {
      if (previousSourceLine >= 0) {
        ((Text)UIUtils.getByUserData(sourceCodeDisplay, previousSourceLine)).
          setFont(Font.font("Monospace", FontWeight.NORMAL,
          UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
        ((Text)UIUtils.getByUserData(sourceCodeDisplay, previousSourceLine)).setFill(Color.BLACK);
      }
      ((Text)UIUtils.getByUserData(sourceCodeDisplay, sourceLine)).
        setFont(Font.font("Monospace", FontWeight.BOLD,
        UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
      ((Text)UIUtils.getByUserData(sourceCodeDisplay, sourceLine)).setFill(Color.ORANGE);
      previousSourceLine = sourceLine;
    }
    
    // scene size change listeners
    scene.widthProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
        for (Text text : sourceCode) {
          text.setFont(new Font("Monospace",
            UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
        }
        assemblyText.setFont(new Font("Monospace",
          UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
      }
    });
    scene.heightProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
        for (Text text : sourceCode) {
          text.setFont(new Font("Monospace",
            UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
        }
        assemblyText.setFont(new Font("Monospace",
          UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
      }
    });
    
    if (assembly != null) {
      assemblyDisplay.getChildren().clear();
      assemblyText = new Text(assembly);
      assemblyText.setFont(Font.font("Monospace", FontWeight.NORMAL,
        UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
      assemblyDisplay.getChildren().add(assemblyText);
    }
    
    return layout;
  }
  
  
  public static void increaseFontSize(Scene scene) {
    if (fontSize < 5.0) fontSize += 0.1;
    for (Text text : sourceCode) {
      text.setFont(new Font("Monospace",
        UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
    }
    assemblyText.setFont(new Font("Monospace",
      UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
  }
  
  
  public static void decreaseFontSize(Scene scene) {
    if (fontSize > 0.1) fontSize -= 0.1;
    for (Text text : sourceCode) {
      text.setFont(new Font("Monospace",
        UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
    }
    assemblyText.setFont(new Font("Monospace",
      UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight())));
  }
  
  
  public static void toggleAssemblyWindow(boolean visible) {
    assemblyScrollPane.setVisible(visible);
    if (!visible) assemblyScrollPane.setMinHeight(0);
    else assemblyScrollPane.setMinHeight(70);
  }
  
}
