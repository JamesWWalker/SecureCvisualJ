import java.io.*;
import java.util.*;
import javafx.application.Application;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;

public class UIMainWindow {

  public Stage window;
  
  private CoordinatorMaster coordinator;
  private FileChooser fileChooser;
  private DoubleProperty fontSize = new SimpleDoubleProperty(10);
  private BorderPane layout;
  private Scene scene;
  private TabPane tabPane;
  
  
  public UIMainWindow(CoordinatorMaster coordinatorIn) {
    coordinator = coordinatorIn;
    fileChooser = new FileChooser();
    FileChooser.ExtensionFilter extFilter =
      new FileChooser.ExtensionFilter("Program run files (*.vaccs)", "*.vaccs");
    fileChooser.getExtensionFilters().add(extFilter);
    fileChooser.setTitle("Load Program Run");
  }
  
  
  public void display() {
    window = new Stage();

    window.setTitle("SecureCvisual");
    window.setMinWidth(250);
    
    // Exit callback
    window.setOnCloseRequest( e -> {
      e.consume();
      coordinator.closeProgram();
    });

    // File menu
    Menu fileMenu = new Menu("File");
    MenuItem menuOpen = new MenuItem("Open Program Run...");
    menuOpen.setOnAction(e -> {
      File file = fileChooser.showOpenDialog(window);
      if (file != null) {
        coordinator.getRun().loadRun(file.getAbsolutePath(), 100);
        // TODO: load source file together with program run
      }
    });
    MenuItem menuExit = new MenuItem("Exit...");
    menuExit.setOnAction(e -> coordinator.closeProgram());
    fileMenu.getItems().addAll(menuOpen, menuExit);
    
    // View menu
    Menu viewMenu = new Menu("View");
    
    // Detail mode
    Menu detailMenu = new Menu("Detail Mode");
    ToggleGroup detailToggle = new ToggleGroup();

    RadioMenuItem menuNovice = new RadioMenuItem("Novice");
    menuNovice.setOnAction(e -> coordinator.runFilter.setDetailLevel(DetailLevel.NOVICE));
    RadioMenuItem menuIntermediate = new RadioMenuItem("Intermediate");
    menuIntermediate.setOnAction(e -> coordinator.runFilter.setDetailLevel(DetailLevel.INTERMEDIATE));
    RadioMenuItem menuAdvanced = new RadioMenuItem("Advanced");
    menuAdvanced.setOnAction(e -> coordinator.runFilter.setDetailLevel(DetailLevel.ADVANCED));
    RadioMenuItem menuExpert = new RadioMenuItem("Expert");
    menuExpert.setOnAction(e -> coordinator.runFilter.setDetailLevel(DetailLevel.EXPERT));
    RadioMenuItem menuCustom = new RadioMenuItem("Custom"); // TODO

    menuNovice.setToggleGroup(detailToggle);
    menuIntermediate.setToggleGroup(detailToggle);
    menuAdvanced.setToggleGroup(detailToggle);
    menuExpert.setToggleGroup(detailToggle);
    menuCustom.setToggleGroup(detailToggle);
    
    coordinator.runFilter.detailLevelProperty().addListener((obs, oldv, newv) -> {
      switch (newv) {
        case NOVICE: menuNovice.setSelected(true); break;
        case INTERMEDIATE: menuIntermediate.setSelected(true); break;
        case ADVANCED: menuAdvanced.setSelected(true); break;
        case EXPERT: menuExpert.setSelected(true); break;
        case CUSTOM: menuCustom.setSelected(true); break;
      }
    });

    detailMenu.getItems().addAll(menuNovice, menuIntermediate, menuAdvanced, menuExpert, menuCustom);
    viewMenu.getItems().add(detailMenu);
    
    // View elements
    Menu viewElementsMenu = new Menu("View Elements");
    CheckMenuItem menuShowHiddenFunctions = new CheckMenuItem("Hidden Functions");
    menuShowHiddenFunctions.selectedProperty().bindBidirectional(coordinator.runFilter.showAllFunctionsProperty());
    
    CheckMenuItem menuShowRegisters = new CheckMenuItem("CPU Registers");
    menuShowRegisters.selectedProperty().bindBidirectional(coordinator.runFilter.showRegistersProperty());
    
    CheckMenuItem menuShowProgramSections = new CheckMenuItem("Program Sections");
    menuShowProgramSections.selectedProperty().bindBidirectional(coordinator.runFilter.showAllSectionsByDefaultProperty());
    
    CheckMenuItem menuShowAssemblyCode = new CheckMenuItem("Assembly Code");
    menuShowAssemblyCode.selectedProperty().bindBidirectional(coordinator.runFilter.showAssemblyProperty());
    
    CheckMenuItem menuShowMemoryLayout = new CheckMenuItem("Memory Layout"); // TODO
    
    viewElementsMenu.getItems().addAll(menuShowHiddenFunctions,
                                       menuShowRegisters, menuShowProgramSections,
                                       menuShowAssemblyCode, menuShowMemoryLayout);
    viewMenu.getItems().add(viewElementsMenu);

    // Help menu TODO
    Menu helpMenu = new Menu("Help");
    MenuItem menuDocumentation = new MenuItem("Documentation...");
    MenuItem menuAbout = new MenuItem("About...");
    helpMenu.getItems().addAll(menuDocumentation, menuAbout);

    //Main menu bar
    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);

    //Tabs
    Group root = new Group();
    scene = new Scene(root, 400, 250, Color.WHITE);

    tabPane = new TabPane();

    BorderPane borderPane = new BorderPane();
    
    // Tabs
    String tabName = "";
    for (int i = 0; i < 5; ++i) {
    
      switch (i) {
        case 0: tabName = "Program Address Space"; break;
        case 1: tabName = "Call Graph"; break;
        case 2: tabName = "Sensitive Data"; break;
        case 3: tabName = "File Operations"; break;
        case 4: tabName = "Source Code"; break;
        default:
          System.err.println("Error populating tabs");
          System.exit(1);
      }
    
      // initialize
      Tab tab = new Tab();
      tab.setClosable(false);
      tab.setText(tabName);
      tab.setId(tabName);
      
      // content
      HBox content = new HBox();
      content.getChildren().add(new Label(tabName));
      content.setAlignment(Pos.CENTER);
      
      // make detachable
      ContextMenu contextMenu = new ContextMenu();
      MenuItem detach = new MenuItem("Detach " + tabName);
      detach.setOnAction(e -> detachTab(tab.getText()));
      contextMenu.getItems().add(detach);
      tab.setContextMenu(contextMenu);
      contextMenu.show(tabPane.lookup("#" + tabName), Side.RIGHT, 0, 0);
      
      // finalize
      tab.setContent(content);
      tabPane.getTabs().add(tab);
    }
    
    // bind to take available space
    borderPane.prefHeightProperty().bind(scene.heightProperty());
    borderPane.prefWidthProperty().bind(scene.widthProperty());
    
    // font size binding
    fontSize.bind(scene.widthProperty().add(scene.heightProperty()).divide(70));
    tabPane.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
    
    borderPane.setCenter(tabPane);
    borderPane.setTop(menuBar);
    root.getChildren().add(borderPane);
    window.setScene(scene);
    window.show();
  }
  
  
  public void detachTab(String title) {
    Tab tab = tabPane.getTabs().stream().filter(t -> t.getId().equals(title)).findAny().orElse(null);
    assert tab != null;
    UIDetachedTab detachedTab = new UIDetachedTab(this, coordinator, tab.getContent(), title);
    coordinator.registerDetachedTab(detachedTab);
    detachedTab.display();
    tabPane.getTabs().remove(tab);
  }
  
  
  public void reattachTab(String title, Node content) {
    Tab tab = new Tab();
    tab.setClosable(false);
    tab.setText(title);
    tab.setId(title);
    tab.setContent(content);
    
    // Must be made detachable again
    ContextMenu contextMenu = new ContextMenu();
    MenuItem detach = new MenuItem("Detach " + title);
    detach.setOnAction(e -> detachTab(title));
    contextMenu.getItems().add(detach);
    tab.setContextMenu(contextMenu);
    contextMenu.show(tabPane.lookup("#" + title), Side.RIGHT, 0, 0);
    
    tabPane.getTabs().add(tab);
  }
  
  
  public String saveConfig() {
    String config = "";
    config += "MainWindowX:" + Double.toString(window.getX()) + System.lineSeparator();
    config += "MainWindowY:" + Double.toString(window.getY()) + System.lineSeparator();
    config += "MainWindowWidth:" + Double.toString(window.getWidth()) + System.lineSeparator();
    config += "MainWindowHeight:" + Double.toString(window.getHeight()) + System.lineSeparator();
    
    return config;
  }
  
  
  public void loadConfig(List<String> config) {
    for (String line : config) {
      String[] parameters = line.trim().split(":");
      if (parameters.length > 1) {
        if (parameters[0].equals("MainWindowX")) window.setX(Double.parseDouble(parameters[1]));
        else if (parameters[0].equals("MainWindowY")) window.setY(Double.parseDouble(parameters[1]));
        else if (parameters[0].equals("MainWindowWidth")) window.setWidth(Double.parseDouble(parameters[1]));
        else if (parameters[0].equals("MainWindowHeight")) window.setHeight(Double.parseDouble(parameters[1]));
      }
    }
  }

  

}
