import java.math.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;


public class UIVariableRepresentation {

  private static Canvas canvas, canvasBottom;
  private static GraphicsContext gc, gcBottom;

  public static double fontSize = 1.0;
  private static VariableRepresentation representation;
  public static Scene scene = null;
  private static boolean updateInProgress;
  private static Stage window;
  
  private static ComboBox<String> cboTopEndianness;
  private static ComboBox<String> cboTopType;
  private static TextField txtBytesTop;
  private static TextField txtValueTop;
  
  private static ComboBox<String> cboBottomEndianness;
  private static ComboBox<String> cboBottomType;
  private static TextField txtBytesBottom;
  private static TextField txtValueBottom;
  
  private static final String BIG_ENDIAN = "Big Endian";
  private static final String LITTLE_ENDIAN = "Little Endian";
  
  
  public static void display(String typeIn, String value) {
  
    updateInProgress = false;
  
    window = new Stage();
    window.setTitle("Variable Representations");
    window.initModality(Modality.APPLICATION_MODAL);
  
    VariableType type = VariableType.convertFromAnalysis(typeIn);
    representation = new VariableRepresentation(true);
    representation.setValueFromDecimal(value, type);
    
    BorderPane container = new BorderPane();

    GridPane grid = new GridPane();
    grid.setHgap(8);
    grid.setVgap(24);
    grid.setPadding(new Insets(10, 10, 10, 10));
    
    {
      ColumnConstraints column = new ColumnConstraints();
      column.setPercentWidth(25.0);
      grid.getColumnConstraints().add(column);
    }
    
    {
      ColumnConstraints column = new ColumnConstraints();
      column.setPercentWidth(25.0);
      grid.getColumnConstraints().add(column);
    }
    
    {
      ColumnConstraints column = new ColumnConstraints();
      column.setPercentWidth(50.0);
      grid.getColumnConstraints().add(column);
    }
    
//    for (int n = 0; n < 8; ++n) {
//      RowConstraints row = new RowConstraints();
//      row.setPercentHeight(12.5);
//      grid.getRowConstraints().add(row);
//    }

    cboTopType = new ComboBox<>();
    cboTopType.getItems().addAll("signed char", "unsigned char", "signed short", "unsigned short", 
      "signed int", "unsigned int", "signed long", "unsigned long");
    cboTopType.getSelectionModel().select(getIndexFromType(type));
    cboTopType.setOnAction(e -> {
      if (!updateInProgress) {
        updateInProgress = true;
        VariableType newType = getTypeFromSelection(cboTopType.getValue());
        representation.setValueFromHex(
          representation.resizeHex(newType,
                                   txtBytesTop.getText(),
                                   txtValueTop.getText().charAt(0) != '-')
        );
        txtValueTop.setText(representation.convertHexToDecimal(representation.getValue(), newType));
        updateInProgress = false;
        updateUI();
      }
    });
    grid.add(cboTopType, 0, 0, 1, 1);
    
    cboTopEndianness = new ComboBox<>();
    cboTopEndianness.getItems().addAll(BIG_ENDIAN, LITTLE_ENDIAN);
    cboTopEndianness.getSelectionModel().select(0);
    cboTopEndianness.setOnAction(e -> {
      if (!updateInProgress) {
        updateInProgress = true;
        if (representation.isBigEndian != (cboTopEndianness.getValue().equals(BIG_ENDIAN)))
          representation.reverseEndianness();
        txtValueTop.setText(representation.convertHexToDecimal(representation.getValue(), 
                                                               getTypeFromSelection(cboTopType.getValue())));
        updateInProgress = false;
        updateUI();
      }
    });
    grid.add(cboTopEndianness, 1, 0, 1, 1);
    
    txtValueTop = new TextField(value);
    txtValueTop.textProperty().addListener((obs, oldValue, newValue) -> {
      if (!updateInProgress) {
        updateInProgress = true;
        if (newValue.matches("-*\\d+")) {
          newValue = representation.clampValue(getTypeFromSelection(cboTopType.getValue()), newValue);
          String newHex = representation.convertDecimalToHex(newValue);
          representation.setValueFromHex(
            representation.resizeHex(getTypeFromSelection(cboTopType.getValue()), 
                                     newHex,
                                     newValue.charAt(0) != '-')
          );
          txtValueTop.setText(newValue);
          updateInProgress = false;
          updateUI();
        }
        else txtValueTop.setText(oldValue);
        updateInProgress = false;
      }

    });
    grid.add(txtValueTop, 2, 0, 1, 1);
    
    txtBytesTop = new TextField(representation.getValue(cboTopEndianness.getValue().equals(BIG_ENDIAN)));
    txtBytesTop.setEditable(false);
    grid.add(txtBytesTop, 0, 1, 3, 1);
    
    Button btnDecrementValue = new Button("Decrement value (-)");
    btnDecrementValue.setOnAction(e -> {
      BigInteger biv = new BigInteger(txtValueTop.getText());
      biv = biv.subtract(BigInteger.ONE);
      biv = new BigInteger(representation.clampValue(getTypeFromSelection(cboTopType.getValue()), biv.toString()));
      representation.setValueFromDecimal(biv.toString(), getTypeFromSelection(cboTopType.getValue()));
      txtValueTop.setText(biv.toString());
      updateUI();
    });
    grid.add(btnDecrementValue, 0, 2, 1, 1);
    
    Button btnIncrementValue = new Button("Increment value (+)");
    btnIncrementValue.setOnAction(e -> {
      BigInteger biv = new BigInteger(txtValueTop.getText());
      biv = biv.add(BigInteger.ONE);
      biv = new BigInteger(representation.clampValue(getTypeFromSelection(cboTopType.getValue()), biv.toString()));
      representation.setValueFromDecimal(biv.toString(), getTypeFromSelection(cboTopType.getValue()));
      txtValueTop.setText(biv.toString());
      updateUI();
    });
    grid.add(btnIncrementValue, 1, 2, 1, 1);
    
    // Value lines
    canvas = new Canvas(500, 80);
    canvas.widthProperty().bind(window.widthProperty().multiply(0.9));
    canvas.heightProperty().bind(window.heightProperty().divide(8));
    gc = canvas.getGraphicsContext2D();
    canvas.widthProperty().addListener(observable -> drawVisualization(
      canvas.getWidth(), canvas.getHeight(), gc, txtBytesTop.getText(),
      VariableType.toString(getTypeFromSelection(cboTopType.getValue()))));
    canvas.heightProperty().addListener(observable -> drawVisualization(
      canvas.getWidth(), canvas.getHeight(),  gc, txtBytesTop.getText(),
      VariableType.toString(getTypeFromSelection(cboTopType.getValue()))));
    grid.add(canvas, 0, 3, 3, 1);
    
    canvasBottom = new Canvas(500, 80);
    canvasBottom.widthProperty().bind(window.widthProperty().multiply(0.9));
    canvasBottom.heightProperty().bind(window.heightProperty().divide(8));
    gcBottom = canvasBottom.getGraphicsContext2D();
    canvasBottom.widthProperty().addListener(observable -> drawVisualization(
      canvasBottom.getWidth(), canvasBottom.getHeight(), gcBottom, txtBytesBottom.getText(),
      VariableType.toString(getTypeFromSelection(cboBottomType.getValue()))));
    canvasBottom.heightProperty().addListener(observable -> drawVisualization(
      canvasBottom.getWidth()-10, canvasBottom.getHeight()-10, gcBottom, txtBytesBottom.getText(),
      VariableType.toString(getTypeFromSelection(cboBottomType.getValue()))));
    grid.add(canvasBottom, 0, 4, 3, 1);

    // Bottom part
    Label lblInterpret = new Label("Interpret As:");
    grid.add(lblInterpret, 0, 5, 3, 1);
    
    cboBottomType = new ComboBox<>();
    cboBottomType.getItems().addAll("signed char", "unsigned char", "signed short", 
      "unsigned short", "signed int", "unsigned int", "signed long", "unsigned long");
    cboBottomType.getSelectionModel().select(getIndexFromType(type));
    cboBottomType.setOnAction(e -> updateUI());
    grid.add(cboBottomType, 0, 6, 1, 1);
    
    cboBottomEndianness = new ComboBox<>();
    cboBottomEndianness.getItems().addAll(BIG_ENDIAN, LITTLE_ENDIAN);
    cboBottomEndianness.getSelectionModel().select(0);
    cboBottomEndianness.setOnAction(e -> updateUI());
    grid.add(cboBottomEndianness, 1, 6, 1, 1);
    
    txtValueBottom = new TextField(value);
    txtValueBottom.setEditable(false);
    grid.add(txtValueBottom, 2, 6, 1, 1);
    
    txtBytesBottom = new TextField(representation.getValue(cboTopEndianness.getValue().equals(BIG_ENDIAN)));
    txtBytesBottom.setEditable(false);
    grid.add(txtBytesBottom, 0, 7, 3, 1);
    
    // View menu
    Menu viewMenu = new Menu("View");
    
    MenuItem menuIncreaseFontSize = new MenuItem("Increase Font Size");
    menuIncreaseFontSize.setOnAction(e -> {
      if (fontSize < 5.0) fontSize += 0.1;
      grid.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
    });
    menuIncreaseFontSize.setAccelerator(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN));
    viewMenu.getItems().add(menuIncreaseFontSize);
    
    MenuItem menuDecreaseFontSize = new MenuItem("Decrease Font Size");
    menuDecreaseFontSize.setOnAction(e -> {
      if (fontSize > 0.1) fontSize -= 0.1;
      grid.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
    });
    menuDecreaseFontSize.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));
    viewMenu.getItems().add(menuDecreaseFontSize);
    
    //Main menu bar
    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(viewMenu);
    
    container.setTop(menuBar);
    container.setCenter(grid);
    
    scene = new Scene(container, 800, 600);
    
    // scene size change listeners
    scene.widthProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
        grid.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
      }
    });
    scene.heightProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
        grid.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
      }
    });
    
    window.setScene(scene);
    window.showAndWait();
  }
  
  
  private static void updateUI() {
    updateInProgress = true;
    txtBytesTop.setText(representation.getValue());
      
    // Figure out what bottom hex should be
    String bottomHex = txtBytesTop.getText();
    VariableType bottomType = getTypeFromSelection(cboBottomType.getValue());
    if (!cboBottomEndianness.getValue().equals(cboTopEndianness.getValue())) 
      bottomHex = representation.reverseBytes(bottomHex);
    bottomHex = 
      representation.resizeHex(bottomType, 
                               bottomHex,
                               (txtValueTop.getText().charAt(0) != '-') || 
                               bottomType == VariableType.UNSIGNED_CHAR ||
                               bottomType == VariableType.UNSIGNED_INT ||
                               bottomType == VariableType.UNSIGNED_LONG ||
                               bottomType == VariableType.UNSIGNED_SHORT);
    txtBytesBottom.setText(bottomHex);
    txtValueBottom.setText(representation.convertHexToDecimal(
                            bottomHex, getTypeFromSelection(cboBottomType.getValue())));
    drawVisualization(canvas.getWidth(), 
              canvas.getHeight(),
              gc, 
              txtBytesTop.getText(),
              VariableType.toString(getTypeFromSelection(cboTopType.getValue())));
    drawVisualization(canvasBottom.getWidth(),
              canvasBottom.getHeight(),
              gcBottom, 
              txtBytesBottom.getText(),
              VariableType.toString(getTypeFromSelection(cboBottomType.getValue())));
    updateInProgress = false;
  }
  
  
  private static int getIndexFromType(VariableType type) {
    switch (type) {
      case SIGNED_CHAR:
        return 0;
      case UNSIGNED_CHAR:
        return 1;
      case SIGNED_SHORT:
        return 2;
      case UNSIGNED_SHORT:
        return 3;
      case SIGNED_INT:
        return 4;
      case UNSIGNED_INT:
        return 5;
      case SIGNED_LONG:
        return 6;
      case UNSIGNED_LONG:
        return 7;
      default:
        assert false;
        return 8;
    }
  }
  
  
  private static VariableType getTypeFromSelection(String selection) {
    if (selection.equals("signed char")) return VariableType.SIGNED_CHAR;
    else if (selection.equals("unsigned char")) return VariableType.UNSIGNED_CHAR;
    else if (selection.equals("signed short")) return VariableType.SIGNED_SHORT;
    else if (selection.equals("unsigned short")) return VariableType.UNSIGNED_SHORT;
    else if (selection.equals("signed int")) return VariableType.SIGNED_INT;
    else if (selection.equals("unsigned int")) return VariableType.UNSIGNED_INT;
    else if (selection.equals("signed long")) return VariableType.SIGNED_LONG;
    else if (selection.equals("unsigned long")) return VariableType.UNSIGNED_LONG;
    assert false;
    return VariableType.STRING;
  }
  
  
  // Returns decimal interpretation for hex of all Fs
  private static String getMaxSizeByType(String type) {
    if (type.equals("signed char")) return "0xFF = -1";
    else if (type.equals("unsigned char")) return "0xFF = 2^8 -1";
    else if (type.equals("signed short")) return "0xFFFF = -1";
    else if (type.equals("unsigned short")) return "0xFFFF = 2^16 -1";
    else if (type.equals("signed int")) return "0xFFFFFFFF = -1";
    else if (type.equals("unsigned int")) return "0xFFFFFFFF = 2^32 -1";
    else if (type.equals("signed long")) {
      if (UIUtils.architecture == 64) return "0xFFFFFFFFFFFFFFFF = -1";
      else if (UIUtils.architecture == 32) return "0xFFFFFFFF = -1";
    }
    else if (type.equals("unsigned long")) {
      if (UIUtils.architecture == 64) return "0xFFFFFFFFFFFFFFFF = 2^64 -1";
      else if (UIUtils.architecture == 32) return "0xFFFFFFFF = 2^32 -1";
    }
    assert false;
    return "Unknown";
  }
  
  
  // Returns hex of all zeroes
  private static String getMinSizeByType(String type) {
    if (type.contains("char")) return "0x00 = 0";
    else if (type.contains("signed short")) return "0x0000 = 0";
    else if (type.contains("signed int")) return "0x00000000 = 0";
    else if (type.contains("signed long")) {
      if (UIUtils.architecture == 64) return "0x0000000000000000 = 0";
      else if (UIUtils.architecture == 32) return "0x00000000 = 0";
    }
    assert false;
    return "Unknown";
  }
  
  
  private static void drawVisualization(double width, 
                                        double height, 
                                        GraphicsContext gc, 
                                        String value,
                                        String type) 
  {
    BigDecimal min = BigDecimal.ZERO;
    BigDecimal max = BigDecimal.ZERO;
    BigDecimal val = BigDecimal.ZERO;
    
    // figure out where we are between min and max values
    // (per PO, go by hex from 0x000 instead of by the decimal interpretations; i.e. treat
    // as unsigned even if they are signed)
    if (type.contains("char")) {
      max = new BigDecimal("255");
      val = new BigDecimal(representation.convertHexToDecimal(value, VariableType.UNSIGNED_CHAR));
    }
    else if (type.contains("short")) {
      max = new BigDecimal("65535");
      val = new BigDecimal(representation.convertHexToDecimal(value, VariableType.UNSIGNED_SHORT));
    }
    else if (type.contains("int")) {
      max = new BigDecimal("4294967295");
      val = new BigDecimal(representation.convertHexToDecimal(value, VariableType.UNSIGNED_INT));
    }
    else if (type.contains("long")) {
      if (UIUtils.architecture == 64) max = new BigDecimal("18446744073709551615");
      else if (UIUtils.architecture == 32) max = new BigDecimal("4294967295");
      val = new BigDecimal(representation.convertHexToDecimal(value, VariableType.UNSIGNED_INT));
    }
//    min = min.abs();
//    max = max.add(min);
//    val = val.add(min);
    
    BigDecimal propIntermediate = val.divide(max, 5, RoundingMode.HALF_UP);
    double proportion = propIntermediate.doubleValue();
    
    // draw
    gc.clearRect(0, 0, width, height);
    
    gc.setLineWidth(5);
    
    gc.setFont(Font.font("Sans", FontWeight.BOLD, 12));
    gc.setTextAlign(TextAlignment.LEFT);
    
/*    if (!type.contains("unsigned")) {
      gc.setFill(Color.rgb(255, 85, 0));
      gc.fillText("Negative", 5, 15);
      
      gc.setFill(Color.rgb(30, 180, 30));
      gc.fillText("Positive", 5, 35);
    }
    
    gc.setFill(Color.BLUE);
    gc.fillText("Current value", 5, 55); /* */
    
    if (!type.contains("unsigned")) gc.setStroke(Color.rgb(235, 85, 0));
    else gc.setStroke(Color.rgb(30, 180, 30));
    gc.strokeLine(0, height/2.0, width/2.0, height/2.0);
    gc.setStroke(Color.rgb(30, 180, 30));
    gc.strokeLine(width/2.0, height/2.0, width, height/2.0);
    
    gc.setStroke(Color.BLUE);
    gc.setFill(Color.BLUE);
    gc.setTextAlign(TextAlignment.CENTER);
    gc.fillText("0x" + value, width/2.0, height/2.0-15);
    
    gc.setStroke(Color.rgb(255, 85, 0));
    gc.setFill(Color.rgb(255, 85, 0));
    gc.setTextAlign(TextAlignment.LEFT);
    gc.fillText(getMinSizeByType(type), 0, height/2.0+15);
    gc.setTextAlign(TextAlignment.RIGHT);
//    if (!type.contains("unsigned")) gc.fillText("-1", width/2.0-10, height/2.0+15);
    
    gc.setStroke(Color.rgb(30, 180, 30));
    gc.setFill(Color.rgb(30, 180, 30));
    gc.setTextAlign(TextAlignment.RIGHT);
    gc.fillText(getMaxSizeByType(type), width, height/2.0+15);
    gc.setTextAlign(TextAlignment.LEFT);
//    if (!type.contains("unsigned")) gc.fillText("0", width/2.0, height/2.0+15);
    
    gc.setFill(Color.BLUE);
    gc.setStroke(Color.BLUE);
    gc.strokeLine(width*proportion-10, height/2.0, width*proportion+10, height/2.0);
  }

}
