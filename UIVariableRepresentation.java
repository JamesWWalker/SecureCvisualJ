import java.math.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class UIVariableRepresentation {

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
      column.setPercentWidth(33.3);
      grid.getColumnConstraints().add(column);
    }
    
    for (int n = 0; n < 7; ++n) {
      RowConstraints row = new RowConstraints();
      row.setPercentHeight(14.2);
      grid.getRowConstraints().add(row);
    }

    cboTopType = new ComboBox<>();
    cboTopType.getItems().addAll("char", "signed short", "unsigned short", "signed int",
      "unsigned int", "signed long", "unsigned long");
    cboTopType.getSelectionModel().select(getIndexFromType(type));
    cboTopType.setOnAction(e -> {
      VariableType newType = getTypeFromSelection(cboTopType.getValue());
      if (txtValueTop.getText().startsWith("-") &&
          (newType == VariableType.UNSIGNED_CHAR ||
           newType == VariableType.UNSIGNED_INT ||
           newType == VariableType.UNSIGNED_SHORT ||
           newType == VariableType.UNSIGNED_LONG))
      {
        representation.setValue(txtValueTop.getText().substring(1), 
                                getTypeFromSelection(cboTopType.getValue()));
      }
      else representation.setValue(txtValueTop.getText(), getTypeFromSelection(cboTopType.getValue()));
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
        if (!newValue.matches("-*\\d*")) {
          if (!newValue.startsWith("-")) txtValueTop.setText(newValue.replaceAll("[^\\d]", ""));
          else txtValueTop.setText("-" + newValue.replaceAll("[^\\d]", ""));
        }
        representation.setValue(newValue, getTypeFromSelection(cboTopType.getValue()));
        if (!updateInProgress) updateUI();
      } catch (NumberFormatException ex) {
        txtValueTop.setText(oldValue);
        representation.setValue(oldValue, getTypeFromSelection(cboTopType.getValue()));
        if (!updateInProgress) updateUI();
      }
    });
    grid.add(txtValueTop, 2, 0, 1, 1);
    
    txtBytesTop = new TextField(representation.getHex(
      getTypeFromSelection(cboTopType.getValue()),
      cboTopEndianness.getValue().equals(BIG_ENDIAN)));
    grid.add(txtBytesTop, 0, 1, 3, 1);
    
    Label lblInterpret = new Label("Interpret As:");
    grid.add(lblInterpret, 0, 2, 3, 1);
    
    cboBottomType = new ComboBox<>();
    cboBottomType.getItems().addAll("char", "signed short", "unsigned short", "signed int",
      "unsigned int", "signed long", "unsigned long");
    cboBottomType.getSelectionModel().select(getIndexFromType(type));
    cboBottomType.setOnAction(e -> updateUI());
    grid.add(cboBottomType, 0, 3, 1, 1);
    
    cboBottomEndianness = new ComboBox<>();
    cboBottomEndianness.getItems().addAll(BIG_ENDIAN, LITTLE_ENDIAN);
    cboBottomEndianness.getSelectionModel().select(0);
    cboBottomEndianness.setOnAction(e -> updateUI());
    grid.add(cboBottomEndianness, 1, 3, 1, 1);
    
    txtValueBottom = new TextField(value);
    txtValueBottom.setEditable(false);
    grid.add(txtValueBottom, 2, 3, 1, 1);
    
    txtBytesBottom = new TextField(representation.getHex(
      getTypeFromSelection(cboTopType.getValue()),
      cboTopEndianness.getValue().equals(BIG_ENDIAN)));
    grid.add(txtBytesBottom, 0, 4, 3, 1);
    
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
    
    scene = new Scene(container, 600, 300);
    
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
    txtValueTop.setText(representation.getDecimal(
      getTypeFromSelection(cboTopType.getValue()),
      cboTopEndianness.getValue().equals(BIG_ENDIAN)));
    txtBytesBottom.setText(representation.getHex(
      getTypeFromSelection(cboBottomType.getValue()),
      cboBottomEndianness.getValue().equals(BIG_ENDIAN)));
    txtValueBottom.setText(representation.getDecimal(
      getTypeFromSelection(cboBottomType.getValue()),
      cboBottomEndianness.getValue().equals(BIG_ENDIAN)));
    updateInProgress = false;
  }
  
  
  private static int getIndexFromType(VariableType type) {
    switch (type) {
      case SIGNED_CHAR:
      case UNSIGNED_CHAR:
        return 0;
      case SIGNED_SHORT:
        return 1;
      case UNSIGNED_SHORT:
        return 2;
      case SIGNED_INT:
        return 3;
      case UNSIGNED_INT:
        return 4;
      case SIGNED_LONG:
        return 5;
      case UNSIGNED_LONG:
        return 6;
      default:
        assert false;
        return 0;
    }
  }
  
  
  private static VariableType getTypeFromSelection(String selection) {
    if (selection.equals("char")) return VariableType.SIGNED_CHAR;
    else if (selection.equals("signed short")) return VariableType.SIGNED_SHORT;
    else if (selection.equals("unsigned short")) return VariableType.UNSIGNED_SHORT;
    else if (selection.equals("signed int")) return VariableType.SIGNED_INT;
    else if (selection.equals("unsigned int")) return VariableType.UNSIGNED_INT;
    else if (selection.equals("signed long")) return VariableType.SIGNED_LONG;
    else if (selection.equals("unsigned long")) return VariableType.UNSIGNED_LONG;
    assert false;
    return VariableType.STRING;
  }

}
