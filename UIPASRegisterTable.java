import java.util.*;
import javafx.beans.binding.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;

public class UIPASRegisterTable {
  
  private static final String columnMapKeyName = "N";
  private static final String columnMapKeyValue = "V";
  
  
  public static TableView createTable(Stage window, TreeMap<String, String> registers) {
  
    TableColumn<Map, String> columnName = new TableColumn<>("Name");
    TableColumn<Map, String> columnValue = new TableColumn<>("Value");
    
    columnName.setCellValueFactory(new MapValueFactory(columnMapKeyName));
    columnValue.setCellValueFactory(new MapValueFactory(columnMapKeyValue));
    
    TableView table = new TableView<>(generateDataFromMap(registers));
    
    table.getSelectionModel().setCellSelectionEnabled(true);
    table.getColumns().setAll(columnName, columnValue);
    Callback<TableColumn<Map, String>, TableCell<Map, String>> cellFactoryForMap 
      = new Callback<TableColumn<Map, String>, TableCell<Map, String>>() {
        @Override
        public TableCell call(TableColumn p) {
          return new TextFieldTableCell(new StringConverter() {
            @Override
            public String toString(Object t) { return t.toString(); }
            @Override
            public Object fromString(String string) { return string; }                                    
          });
        }
      };
    
    columnName.setCellFactory(cellFactoryForMap);
    columnValue.setCellFactory(cellFactoryForMap);
    
    table.fixedCellSizeProperty().bind(window.widthProperty().add(window.heightProperty()).divide(28));
    table.prefHeightProperty().bind(table.fixedCellSizeProperty()
      .multiply(Bindings.size(table.getItems()).add(1.01)));
    table.minHeightProperty().bind(table.prefHeightProperty());
    table.maxHeightProperty().bind(table.prefHeightProperty());
    
    return table;
    
  } // createTable()
  
  
  private static ObservableList<Map> generateDataFromMap(TreeMap<String, String> registers) {
    ObservableList<Map> allData = FXCollections.observableArrayList();
    Set<String> keys = registers.keySet();
    for (String key : keys) {
      String value = registers.get(key);
      Map<String, String> dataRow = new HashMap<>();
      dataRow.put(columnMapKeyName, key);
      dataRow.put(columnMapKeyValue, value);
      allData.add(dataRow);
    }
    return allData;
  }

}
