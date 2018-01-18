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
    representation = new VariableRepresentation(type, value, true);
    
    BorderPane container = new BorderPane();

    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setPadding(new Insets(10, 10, 10, 10));
    
    for (int n = 0; n < 3; ++n) {
      ColumnConstraints column = new ColumnConstraints();
      column.setPercentWidth(20.0);
      grid.getColumnConstraints().add(column);
    }
    ColumnConstraints lastColumn = new ColumnConstraints();
    lastColumn.setPercentWidth(40.0);
    grid.getColumnConstraints().add(lastColumn);
    
    for (int n = 0; n < 8; ++n) {
      RowConstraints row = new RowConstraints();
      row.setPercentHeight(12.5);
      grid.getRowConstraints().add(row);
    }

    cboTopType = new ComboBox<>();
    cboTopType.getItems().addAll("signed char", "unsigned char", "signed short", "unsigned short", 
      "signed int", "unsigned int", "signed long", "unsigned long");
    cboTopType.getSelectionModel().select(getIndexFromType(type));
    cboTopType.setOnAction(e -> {
      VariableType newType = getTypeFromSelection(cboTopType.getValue());
      BigInteger intermediateValue = new BigInteger(representation.typeConversion(txtBytesTop.getText(),
        getTypeFromSelection(cboTopType.getValue())).substring(2), 16);
      BigInteger newValue = representation.convertHexToDecimal(intermediateValue.toString(16),
        !VariableType.toString(getTypeFromSelection(cboTopType.getValue())).toLowerCase().contains("unsigned"));
      representation.setValue(newValue.toString(), getTypeFromSelection(cboTopType.getValue()));
      txtValueTop.setText(newValue.toString());
//      txtValueTop.setText(newValue.toString());
      updateUI();
    });
    grid.add(cboTopType, 0, 0, 1, 1);
    
    cboTopEndianness = new ComboBox<>();
    cboTopEndianness.getItems().addAll(BIG_ENDIAN, LITTLE_ENDIAN);
    cboTopEndianness.getSelectionModel().select(0);
    cboTopEndianness.setOnAction(e -> {
      representation.reverseEndianness(cboTopEndianness.getValue().equals(BIG_ENDIAN));
      updateUI();
    });
    grid.add(cboTopEndianness, 1, 0, 1, 1);
    
    txtValueTop = new TextField(value);
    txtValueTop.textProperty().addListener((obs, oldValue, newValue) -> {
      try {
        txtValueTop.setText(newValue);
        String interpretValue = "0";
        if (newValue.matches("-*\\d*")) interpretValue = newValue;
        representation.setValue(interpretValue, getTypeFromSelection(cboTopType.getValue()));
        if (!updateInProgress) updateUI();
      } catch (NumberFormatException ex) {
        newValue = newValue.replaceAll("[^\\d]", "");
        txtValueTop.setText(newValue);
        representation.setValue(newValue, getTypeFromSelection(cboTopType.getValue()));
        if (!updateInProgress) updateUI();
      }
    });
    grid.add(txtValueTop, 2, 0, 1, 1);
    
    txtBytesTop = new TextField(representation.getHex(
      getTypeFromSelection(cboTopType.getValue()),
      cboTopEndianness.getValue().equals(BIG_ENDIAN)));
    grid.add(txtBytesTop, 0, 1, 3, 1);
    
    Button btnDecrementValue = new Button("Decrement value (-)");
    btnDecrementValue.setOnAction(e -> {
      BigInteger biv = new BigInteger(txtValueTop.getText());
      biv = biv.subtract(BigInteger.ONE);
      txtValueTop.setText(
        representation.clampValue(getTypeFromSelection(cboTopType.getValue()), biv.toString()));
      updateUI();
    });
    grid.add(btnDecrementValue, 0, 2, 1, 1);
    
    Button btnIncrementValue = new Button("Increment value (+)");
    btnIncrementValue.setOnAction(e -> {
      BigInteger biv = new BigInteger(txtValueTop.getText());
      biv = biv.add(BigInteger.ONE);
      txtValueTop.setText(
        representation.clampValue(getTypeFromSelection(cboTopType.getValue()), biv.toString()));
      updateUI();
    });
    grid.add(btnIncrementValue, 1, 2, 1, 1);
    
    Label lblInterpret = new Label("Interpret As:");
    grid.add(lblInterpret, 0, 3, 3, 1);
    
    cboBottomType = new ComboBox<>();
    cboBottomType.getItems().addAll("signed char", "unsigned char", "signed short", 
      "unsigned short", "signed int", "unsigned int", "signed long", "unsigned long");
    cboBottomType.getSelectionModel().select(getIndexFromType(type));
    cboBottomType.setOnAction(e -> updateUI());
    grid.add(cboBottomType, 0, 4, 1, 1);
    
    cboBottomEndianness = new ComboBox<>();
    cboBottomEndianness.getItems().addAll(BIG_ENDIAN, LITTLE_ENDIAN);
    cboBottomEndianness.getSelectionModel().select(0);
    cboBottomEndianness.setOnAction(e -> updateUI());
    grid.add(cboBottomEndianness, 1, 4, 1, 1);
    
    txtValueBottom = new TextField(value);
    txtValueBottom.setEditable(false);
    grid.add(txtValueBottom, 2, 4, 1, 1);
    
    txtBytesBottom = new TextField(representation.getHex(
      getTypeFromSelection(cboTopType.getValue()),
      cboTopEndianness.getValue().equals(BIG_ENDIAN)));
    grid.add(txtBytesBottom, 0, 5, 3, 1);
    
    // Value "clocks"
    canvas = new Canvas(240, 240);
    canvas.widthProperty().bind(window.widthProperty().multiply(0.35));
    canvas.heightProperty().bind(window.heightProperty().divide(2));
    gc = canvas.getGraphicsContext2D();
    canvas.widthProperty().addListener(observable -> drawClock(
      canvas.getWidth()-10, canvas.getHeight()-80, gc, txtBytesTop.getText(),
      txtValueTop.getText(),
      VariableType.toString(getTypeFromSelection(cboTopType.getValue()))));
    canvas.heightProperty().addListener(observable -> drawClock(
      canvas.getWidth()-10, canvas.getHeight()-80,  gc, txtBytesTop.getText(),
      txtValueTop.getText(),
      VariableType.toString(getTypeFromSelection(cboTopType.getValue()))));
    grid.add(canvas, 3, 0, 1, 4);
    
    canvasBottom = new Canvas(240, 240);
    canvasBottom.widthProperty().bind(window.widthProperty().multiply(0.35));
    canvasBottom.heightProperty().bind(window.heightProperty().divide(2));
    gcBottom = canvasBottom.getGraphicsContext2D();
    canvasBottom.widthProperty().addListener(observable -> drawClock(
      canvasBottom.getWidth()-10, canvasBottom.getHeight()-80, gcBottom, txtBytesBottom.getText(),
      txtValueBottom.getText(),
      VariableType.toString(getTypeFromSelection(cboBottomType.getValue()))));
    canvasBottom.heightProperty().addListener(observable -> drawClock(
      canvasBottom.getWidth()-10, canvasBottom.getHeight()-80, gcBottom, txtBytesBottom.getText(),
      txtValueBottom.getText(),
      VariableType.toString(getTypeFromSelection(cboBottomType.getValue()))));
    grid.add(canvasBottom, 3, 3, 1, 4);
    
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
    txtBytesTop.setText(representation.getHex(
      getTypeFromSelection(cboTopType.getValue()),
      cboTopEndianness.getValue().equals(BIG_ENDIAN)));
//    txtValueTop.setText(representation.getDecimal(
//      getTypeFromSelection(cboTopType.getValue()),
//      cboTopEndianness.getValue().equals(BIG_ENDIAN)));
    txtBytesBottom.setText(representation.getHex(
      getTypeFromSelection(cboBottomType.getValue()),
      cboBottomEndianness.getValue().equals(BIG_ENDIAN)));
    txtValueBottom.setText(representation.getDecimal(
      getTypeFromSelection(cboBottomType.getValue()),
      cboBottomEndianness.getValue().equals(BIG_ENDIAN)));
    drawClock(canvas.getWidth()-10, 
              canvas.getHeight()-80, 
              gc, 
              txtBytesTop.getText(),
              txtValueTop.getText(),
              VariableType.toString(getTypeFromSelection(cboTopType.getValue())));
    drawClock(canvasBottom.getWidth()-10,
              canvasBottom.getHeight()-80, 
              gcBottom, 
              txtBytesBottom.getText(),
              txtValueBottom.getText(),
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
  
  
  private static String getMaxSizeByType(String type) {
    if (type.equals("signed char")) return "0x7F=2^8";
    else if (type.equals("unsigned char")) return "0xFF=2^8";
    else if (type.equals("signed short")) return "0x7FFF=2^(16-1)-1";
    else if (type.equals("unsigned short")) return "0xFFFF=2^16";
    else if (type.equals("signed int")) return "0x7FFFFFFF=2^(32-1)-1";
    else if (type.equals("unsigned int")) return "0xFFFFFFFF=2^32";
    else if (type.equals("signed long")) return "0x7FFFFFFFFFFFFFFF=2^(64-1)-1";
    else if (type.equals("unsigned long")) return "0xFFFFFFFFFFFFFFFF=2^64";
    assert false;
    return "Unknown";
  }
  
  
  private static String getMinSizeByType(String type) {
    if (type.equals("signed char")) return "0x00";
    else if (type.equals("unsigned char")) return "0x80=2^(8-1)";
    else if (type.equals("signed short")) return "0x8000=-2^(16-1)";
    else if (type.equals("unsigned short")) return "0x0000=0";
    else if (type.equals("signed int")) return "0x80000000=-2^(32-1)";
    else if (type.equals("unsigned int")) return "0x00000000=0";
    else if (type.equals("signed long")) return "0x8000000000000000=-2^(64-1)";
    else if (type.equals("unsigned long")) return "0x0000000000000000=0";
    assert false;
    return "Unknown";
  }
  
  
  private static void drawClock(double width, 
                                double height, 
                                GraphicsContext gc, 
                                String value,
                                String decValue,
                                String type) 
  {
    BigDecimal min = BigDecimal.ZERO;
    BigDecimal max = BigDecimal.ZERO;
    // figure out where we are between min and max values
    if (type.contains("unsigned")) {
      min = new BigDecimal("0");
      if (type.contains("char")) max = new BigDecimal("255");
      else if (type.contains("short")) max = new BigDecimal("65535");
      else if (type.contains("int")) max = new BigDecimal("4294967295");
      else if (type.contains("long")) max = new BigDecimal("18446744073709551615");
    }
    else {
      if (type.contains("char")) {
        min = new BigDecimal("-128");
        max = new BigDecimal("127");
      }
      else if (type.contains("short")) {
        min = new BigDecimal("-32768");
        max = new BigDecimal("32767");
      }
      else if (type.contains("int")) {
        min = new BigDecimal("-2147483648");
        max = new BigDecimal("2147483647");
      }
      else if (type.contains("long")) {
        min = new BigDecimal("-9223372036854775808");
        max = new BigDecimal("9223372036854775807");
      }
    }
    BigDecimal val = new BigDecimal(decValue);
    min = min.abs();
    max = max.add(min);
    val = val.add(min);
    BigDecimal proportion = val.divide(max, 5, RoundingMode.HALF_UP).multiply(new BigDecimal(360));
    double angle = 360.0 - proportion.doubleValue();
    
    // draw
    gc.clearRect(0, 0, width+10, height+90);
    
    gc.setLineWidth(5);
    
    gc.setFont(Font.font("Sans", FontWeight.BOLD, 12));
    gc.setTextAlign(TextAlignment.LEFT);
    
    if (!type.contains("unsigned")) {
      gc.setFill(Color.rgb(255, 85, 0));
      gc.fillText("Negative", 5, 15);
      
      gc.setFill(Color.rgb(50, 220, 50));
      gc.fillText("Positive", 5, 35);
    }
    
    gc.setFill(Color.BLUE);
    gc.fillText("Current value", 5, 55);
    
    if (!type.contains("unsigned")) gc.setStroke(Color.rgb(235, 85, 0));
    else gc.setStroke(Color.rgb(50, 220, 50));
    gc.strokeArc(width*(1.0/4.0), height*(1.0/4.0), width/2.0, height/2.0, 90, 180, ArcType.OPEN);
    gc.setStroke(Color.rgb(50, 220, 50));
    gc.strokeArc(width*(1.0/4.0), height*(1.0/4.0), width/2.0, height/2.0, 270, 180, ArcType.OPEN);
    
    gc.setStroke(Color.BLUE);
    gc.setFill(Color.BLUE);
    gc.fillText(value, width*(1.0/4.0)+20, height/2.0);
    
    gc.setStroke(Color.rgb(255, 85, 0));
    gc.setFill(Color.rgb(255, 85, 0));
    gc.setTextAlign(TextAlignment.RIGHT);
    gc.fillText(getMinSizeByType(type), width/2.0-10, height*(7.0/8.0));
    if (!type.contains("unsigned")) gc.fillText("-1", width/2.0-10, height*(3.0/16.0));
    
    gc.setStroke(Color.rgb(50, 220, 50));
    gc.setFill(Color.rgb(50, 220, 50));
    gc.setTextAlign(TextAlignment.LEFT);
    gc.fillText(getMaxSizeByType(type), width/2.0+10, height*(7.0/8.0));
    if (!type.contains("unsigned")) gc.fillText("0", width/2.0+10, height*(3.0/16.0));
    
    gc.setFill(Color.BLUE);
    gc.setStroke(Color.BLUE);
    gc.strokeArc(width*(1.0/4.0), height*(1.0/4.0), width/2.0, height/2.0, angle-5-90, 10, ArcType.OPEN);
  }

}
