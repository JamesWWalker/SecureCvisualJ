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
  
  public CoordinatorMaster coordinator;
  public List<UIDetachedTab> detachedTabs = new ArrayList<>();
  public Stage window;
  
  private Map<String, Node> contentPool;
  private FileChooser fileChooser;
  private DoubleProperty fontSize = new SimpleDoubleProperty(10);
  private BorderPane layout;
  private Scene scene;
  private TabPane tabPane;
  
  RadioMenuItem menuNovice;
  RadioMenuItem menuIntermediate;
  RadioMenuItem menuAdvanced;
  RadioMenuItem menuExpert;
  
  
  public UIMainWindow(CoordinatorMaster coordinatorIn) {
    contentPool = new HashMap<>();
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
        String absolutePath = file.getAbsolutePath();
        coordinator.getRun().loadRun(absolutePath, 100);
        // Load source file together with program run
        String possibleSourceFile = absolutePath.substring(0, absolutePath.length() - 13) + ".c";
        try {
          Pane sourceCodeLayout = UISourceCode.loadSourceFile(possibleSourceFile);
          setTabContent(SubProgram.toString(SubProgram.SC), sourceCodeLayout);
        } catch (IOException ex) {
          System.err.println("Could not find source file " + possibleSourceFile);
        }
        coordinator.queryProcessRunAndUpdateUI();
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

    menuNovice = new RadioMenuItem(DetailLevel.toString(DetailLevel.NOVICE));
    menuNovice.setOnAction(e -> {
      coordinator.runFilter.setDetailLevel(DetailLevel.NOVICE);
      coordinator.queryProcessRunAndUpdateUI();
    });
    menuIntermediate = new RadioMenuItem(DetailLevel.toString(DetailLevel.INTERMEDIATE));
    menuIntermediate.setOnAction(e -> {
      coordinator.runFilter.setDetailLevel(DetailLevel.INTERMEDIATE);
      coordinator.queryProcessRunAndUpdateUI();
    });
    menuAdvanced = new RadioMenuItem(DetailLevel.toString(DetailLevel.ADVANCED));
    menuAdvanced.setOnAction(e -> {
      coordinator.runFilter.setDetailLevel(DetailLevel.ADVANCED);
      coordinator.queryProcessRunAndUpdateUI();
    });
    menuExpert = new RadioMenuItem(DetailLevel.toString(DetailLevel.EXPERT));
    menuExpert.setOnAction(e -> {
      coordinator.runFilter.setDetailLevel(DetailLevel.EXPERT);
      coordinator.queryProcessRunAndUpdateUI();
    });

    menuNovice.setToggleGroup(detailToggle);
    menuIntermediate.setToggleGroup(detailToggle);
    menuAdvanced.setToggleGroup(detailToggle);
    menuExpert.setToggleGroup(detailToggle);
    
    coordinator.runFilter.detailLevelProperty().addListener((obs, oldv, newv) -> {
      switch (newv) {
        case NOVICE: menuNovice.setSelected(true); break;
        case INTERMEDIATE: menuIntermediate.setSelected(true); break;
        case ADVANCED: menuAdvanced.setSelected(true); break;
        case EXPERT: menuExpert.setSelected(true); break;
      }
    });

    detailMenu.getItems().addAll(menuNovice, menuIntermediate, menuAdvanced, menuExpert);
    viewMenu.getItems().add(detailMenu);
    
    // View elements
    Menu viewElementsMenu = new Menu("View Elements");
    CheckMenuItem menuShowHiddenFunctions = new CheckMenuItem("Hidden Functions");
    menuShowHiddenFunctions.selectedProperty().bindBidirectional(coordinator.runFilter.showAllFunctionsProperty());
    menuShowHiddenFunctions.setOnAction(e -> setCustomDetailLevel());
    
    CheckMenuItem menuShowRegisters = new CheckMenuItem("CPU Registers");
    menuShowRegisters.selectedProperty().bindBidirectional(coordinator.runFilter.showRegistersProperty());
    menuShowRegisters.setOnAction(e -> setCustomDetailLevel());
    
    CheckMenuItem menuShowProgramSections = new CheckMenuItem("All Program Sections");
    menuShowProgramSections.selectedProperty().bindBidirectional(coordinator.runFilter.showAllSectionsByDefaultProperty());
    menuShowProgramSections.setOnAction(e -> setCustomDetailLevel());
    
    CheckMenuItem menuShowAssemblyCode = new CheckMenuItem("Assembly Code");
    menuShowAssemblyCode.selectedProperty().bindBidirectional(coordinator.runFilter.showAssemblyProperty());
    menuShowAssemblyCode.setOnAction(e -> setCustomDetailLevel());
    
    CheckMenuItem menuShowMemoryLayout = new CheckMenuItem("Memory Layout"); // TODO
    
    viewElementsMenu.getItems().addAll(menuShowHiddenFunctions,
                                       menuShowRegisters, menuShowProgramSections,
                                       menuShowAssemblyCode, menuShowMemoryLayout);
    viewMenu.getItems().add(viewElementsMenu);
    
    // Filters
    Menu filtersMenu =new Menu("Filters");
    MenuItem menuClearRegisterFilters = new MenuItem("Clear Register Filters");
    menuClearRegisterFilters.setOnAction(e -> {
      coordinator.runFilter.clearRegisterFilter();
      coordinator.queryProcessRunAndUpdateUI();
    });
    
    filtersMenu.getItems().add(menuClearRegisterFilters);
    viewMenu.getItems().add(filtersMenu);

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
        case 0: tabName = SubProgram.toString(SubProgram.PAS); break;
        case 1: tabName = SubProgram.toString(SubProgram.CG); break;
        case 2: tabName = SubProgram.toString(SubProgram.FO); break;
        case 3: tabName = SubProgram.toString(SubProgram.SD); break;
        case 4: tabName = SubProgram.toString(SubProgram.SC); break;
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
      contentPool.put(tabName, content);
      tab.setContent(contentPool.get(tabName));
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
  
  
  private void setCustomDetailLevel() {
    menuNovice.setSelected(false);
    menuIntermediate.setSelected(false);
    menuAdvanced.setSelected(false);
    menuExpert.setSelected(false);
    coordinator.runFilter.setDetailLevel(DetailLevel.CUSTOM);
    coordinator.queryProcessRunAndUpdateUI();
  }
  
  
  public void detachTab(String title) {
    Tab tab = tabPane.getTabs().stream().filter(t -> t.getId().equals(title)).findAny().orElse(null);
    assert tab != null;
    UIDetachedTab detachedTab = new UIDetachedTab(this, coordinator, contentPool.get(title), title);
    detachedTabs.add(detachedTab);
    detachedTab.display();
    tabPane.getTabs().remove(tab);
    coordinator.queryProcessRunAndUpdateUI();
  }
  
  
  public void reattachTab(UIDetachedTab dtab) {
    detachedTabs.remove(dtab);
    
    Tab tab = new Tab();
    tab.setClosable(false);
    tab.setText(dtab.title);
    tab.setId(dtab.title);
//    tab.setContent(contentPool.get(title));
    
    // Must be made detachable again
    ContextMenu contextMenu = new ContextMenu();
    MenuItem detach = new MenuItem("Detach " + dtab.title);
    detach.setOnAction(e -> detachTab(dtab.title));
    contextMenu.getItems().add(detach);
    tab.setContextMenu(contextMenu);
    contextMenu.show(tabPane.lookup("#" + dtab.title), Side.RIGHT, 0, 0);
    
    tabPane.getTabs().add(tab);
    coordinator.queryProcessRunAndUpdateUI();
  }
  
  
  private void setTabContent(String title, Node content) {
//    contentPool.put(title, content);
    Tab tab = tabPane.getTabs().stream().filter(t -> t.getId().equals(title)).findAny().orElse(null);
    if (tab != null) {
      tab.setContent(content);
      return;
    }
    UIDetachedTab dtab = detachedTabs.stream().filter(t -> t.title.equals(title)).findAny().orElse(null);
    if (dtab != null) {
      dtab.setContent(content);
      return;
    }
    assert false; /* */
  }
  
  
  public Stage getTabWindow(String title) {
    Tab tab = tabPane.getTabs().stream().filter(t -> t.getId().equals(title)).findAny().orElse(null);
    if (tab != null) return window;
    UIDetachedTab dtab = detachedTabs.stream().filter(t -> t.title.equals(title)).findAny().orElse(null);
    if (dtab != null) return dtab.window;
    assert false; /* */
    return null;
  }
  
  
  public void updateUI(int sourceLine,
                       String assembly,
                       List<ActivationRecord> stack,
                       TreeMap<String, String> registers,
                       TreeMap<String, VariableDelta> variables,
                       ArrayList<ProgramSection> sections)
  {
    // Update PAS
    setTabContent(SubProgram.toString(SubProgram.PAS),
                  UIProgramAddressSpace.buildPAS(this,
                                                 scene,
                                                 coordinator.runFilter.getDetailLevel(),
                                                 stack,
                                                 registers,
                                                 variables,
                                                 sections)
                 );
    // Update source code
    setTabContent(SubProgram.toString(SubProgram.SC),
                  UISourceCode.buildSC(scene, sourceLine, assembly));
    // TODO: other tabs
  }
  
  
  public String saveConfig() {
    String config = "";
    config += "MainWindowX:" + Double.toString(window.getX()) + System.lineSeparator();
    config += "MainWindowY:" + Double.toString(window.getY()) + System.lineSeparator();
    config += "MainWindowWidth:" + Double.toString(window.getWidth()) + System.lineSeparator();
    config += "MainWindowHeight:" + Double.toString(window.getHeight()) + System.lineSeparator();
    for (UIDetachedTab tab : detachedTabs) config += "DetachedTab:" + tab.title + System.lineSeparator();
    for (UIDetachedTab tab : detachedTabs) config += tab.saveConfig();
    
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
        else if (parameters[0].equals("DetachedTab")) detachTab(parameters[1]);
      }
    }
    for (UIDetachedTab tab : detachedTabs) tab.loadConfig(config);
  }

  

}
