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

public class UIPASVariableTable {
  
  private static final String columnMapKeyAddress = "A";
  private static final String columnMapKeyName = "N";
  private static final String columnMapKeyType = "T";
  private static final String columnMapKeySize = "S";
  private static final String columnMapKeyValue = "V";
  
  
  public static TableView createTable(Stage window, TreeMap<String, VariableDelta> variables) {
  
    TableColumn<Map, String> columnAddress = new TableColumn<>("Address");
    TableColumn<Map, String> columnName = new TableColumn<>("Name");
    TableColumn<Map, String> columnType = new TableColumn<>("Type");
    TableColumn<Map, String> columnSize = new TableColumn<>("Size");
    TableColumn<Map, String> columnValue = new TableColumn<>("Value");
    
    columnAddress.setCellValueFactory(new MapValueFactory(columnMapKeyAddress));
    columnName.setCellValueFactory(new MapValueFactory(columnMapKeyName));
    columnType.setCellValueFactory(new MapValueFactory(columnMapKeyType));
    columnSize.setCellValueFactory(new MapValueFactory(columnMapKeySize));
    columnValue.setCellValueFactory(new MapValueFactory(columnMapKeyValue));
    
    TableView table = new TableView<>(generateDataFromMap(variables));
    
    table.getSelectionModel().setCellSelectionEnabled(true);
    table.getColumns().setAll(columnAddress, 
                              columnName, 
                              columnType, 
                              columnSize, 
                              columnValue);
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
    
    columnAddress.setCellFactory(cellFactoryForMap);
    columnName.setCellFactory(cellFactoryForMap);
    columnType.setCellFactory(cellFactoryForMap);
    columnSize.setCellFactory(cellFactoryForMap);
    columnValue.setCellFactory(cellFactoryForMap);
    
    table.fixedCellSizeProperty().bind(window.widthProperty().add(window.heightProperty()).divide(28));
    table.prefHeightProperty().bind(table.fixedCellSizeProperty()
      .multiply(Bindings.size(table.getItems()).add(1.01)));
    table.minHeightProperty().bind(table.prefHeightProperty());
    table.maxHeightProperty().bind(table.prefHeightProperty());
    
    return table;
    
  } // createTable()
  
  
  private static ObservableList<Map> generateDataFromMap(TreeMap<String, VariableDelta> variables) {
    ObservableList<Map> allData = FXCollections.observableArrayList();
    Set<String> keys = variables.keySet();
    for (String key : keys) {
      VariableDelta variable = variables.get(key);
      Map<String, String> dataRow = new HashMap<>();
      dataRow.put(columnMapKeyAddress, "0x" + Long.toHexString(variable.address).toUpperCase());
      dataRow.put(columnMapKeyName, variable.name);
      dataRow.put(columnMapKeyType, variable.type);
      dataRow.put(columnMapKeySize, "TEMPORARY"); // TODO
      dataRow.put(columnMapKeyValue, variable.value);
      allData.add(dataRow);
    }
    return allData;
  }

}
