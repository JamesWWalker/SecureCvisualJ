import java.io.*;
import java.util.*;
import javafx.application.Application;
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
import javafx.stage.*;

public class UIMainWindow {

  private DoubleProperty previousPasScrollbar = new SimpleDoubleProperty();
  public final double getPreviousPasScrollbar() { return previousPasScrollbar.get(); }
  public final void setPreviousPasScrollbar(double value) { previousPasScrollbar.set(value); }
  public DoubleProperty previousPasScrollbarProperty() { return previousPasScrollbar; }
  
  public CoordinatorMaster coordinator;
  public List<UIDetachedTab> detachedTabs = new ArrayList<>();
  public SensitiveDataVariable sdVariableToView = null;
  public Stage window;
  
  private Map<String, Node> contentPool;
  private FileChooser fileChooser;
  private double fontSize = 1.0;
  private BorderPane layout;
  private Scene scene;
  private TabPane tabPane;
  
  private RadioMenuItem menuNovice;
  private RadioMenuItem menuIntermediate;
  private RadioMenuItem menuAdvanced;
  private RadioMenuItem menuExpert;
  
  private ScrollPane scrollPanePAS;
  private ScrollPane scrollPaneSD;
  private Canvas canvasCG;
  // TODO: other scrollpanes
  
  
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
        coordinator.getRun().loadRun(absolutePath, 100, canvasCG);
        // Parse source
        Pane sourceCodeLayout = UISourceCode.parseSourceCode(coordinator.getRun().cSource);
        setTabContent(SubProgram.toString(SubProgram.SC), sourceCodeLayout);
        // Load source file together with program run -- DISABLED, boss didn't like it
//        String possibleSourceFile = absolutePath.substring(0, absolutePath.length() - 13) + ".c";
//        try {
//          Pane sourceCodeLayout = UISourceCode.loadSourceFile(possibleSourceFile);
//          setTabContent(SubProgram.toString(SubProgram.SC), sourceCodeLayout);
//        } catch (IOException ex) {
//          System.err.println("Could not find source file " + possibleSourceFile);
//        }
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
    menuShowProgramSections.selectedProperty().bindBidirectional
      (coordinator.runFilter.showAllSectionsByDefaultProperty());
    menuShowProgramSections.setOnAction(e -> setCustomDetailLevel());
    
    CheckMenuItem menuShowAssemblyCode = new CheckMenuItem("Assembly Code");
    menuShowAssemblyCode.selectedProperty().bindBidirectional(coordinator.runFilter.showAssemblyProperty());
    menuShowAssemblyCode.setOnAction(e -> setCustomDetailLevel());
    
    CheckMenuItem menuShowProgramOutput = new CheckMenuItem("Program Output");
    menuShowProgramOutput.selectedProperty().bindBidirectional(coordinator.runFilter.showOutputProperty());
    
    CheckMenuItem menuShowOffsets = new CheckMenuItem("Offsets");
    menuShowOffsets.selectedProperty().bindBidirectional(coordinator.runFilter.showOffsetsProperty());

    
//    CheckMenuItem menuShowMemoryLayout = new CheckMenuItem("Memory Layout"); // TODO
    
    viewElementsMenu.getItems().addAll(menuShowHiddenFunctions, menuShowRegisters, 
                                       menuShowProgramSections, menuShowAssemblyCode, 
                                       menuShowProgramOutput, menuShowOffsets /*menuShowMemoryLayout*/);
    viewMenu.getItems().add(viewElementsMenu);
    
    // Filters
    Menu filtersMenu = new Menu("Filters");
    MenuItem menuClearRegisterFilters = new MenuItem("Reset Register Filters");
    menuClearRegisterFilters.setOnAction(e -> {
      coordinator.runFilter.clearRegisterFilter();
      coordinator.queryProcessRunAndUpdateUI();
    });
    
    MenuItem menuClearSectionFilters = new MenuItem("Reset Section Filters");
    menuClearSectionFilters.setOnAction(e -> {
      coordinator.runFilter.clearSectionFilter();
      coordinator.queryProcessRunAndUpdateUI();
    });
    
    filtersMenu.getItems().addAll(menuClearRegisterFilters, menuClearSectionFilters);
    viewMenu.getItems().add(filtersMenu);

    MenuItem menuIncreaseFontSize = new MenuItem("Increase Font Size");
    menuIncreaseFontSize.setOnAction(e -> {
      if (fontSize < 5.0) fontSize += 0.1;
      tabPane.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
    });
    menuIncreaseFontSize.setAccelerator(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN));
    viewMenu.getItems().add(menuIncreaseFontSize);
    
    MenuItem menuDecreaseFontSize = new MenuItem("Decrease Font Size");
    menuDecreaseFontSize.setOnAction(e -> {
      if (fontSize > 0.1) fontSize -= 0.1;
      tabPane.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
    });
    menuDecreaseFontSize.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));
    viewMenu.getItems().add(menuDecreaseFontSize);

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
    
    // scene size change listeners
    scene.widthProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
        tabPane.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
      }
    });
    scene.heightProperty().addListener(new ChangeListener<Number>() {
      @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
        tabPane.setStyle("-fx-font-size: " + UIUtils.calculateFontSize(fontSize, scene.getWidth(), scene.getHeight()));
      }
    });
    
    // Set up canvas for call graph
    canvasCG = new Canvas(getTabWindow(SubProgram.toString(SubProgram.CG)).getWidth(),
                          getTabWindow(SubProgram.toString(SubProgram.CG)).getHeight());
    canvasCG.heightProperty().bind(getTabWindow(SubProgram.toString(SubProgram.CG)).heightProperty());
    canvasCG.widthProperty().bind(getTabWindow(SubProgram.toString(SubProgram.CG)).widthProperty());
    
    borderPane.setCenter(tabPane);
    borderPane.setTop(menuBar);
    root.getChildren().add(borderPane);
    window.setScene(scene);
    window.show();
  }
  
  
  public void setCustomDetailLevel() {
    menuNovice.setSelected(false);
    menuIntermediate.setSelected(false);
    menuAdvanced.setSelected(false);
    menuExpert.setSelected(false);
    coordinator.runFilter.setDetailLevel(DetailLevel.CUSTOM);
    coordinator.queryProcessRunAndUpdateUI();
    UISourceCode.toggleAssemblyWindow(coordinator.runFilter.getShowAssembly());
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
    scrollPanePAS = UIProgramAddressSpace.buildPAS(this,
                                                   scene,
                                                   coordinator.runFilter.getDetailLevel(),
                                                   stack,
                                                   registers,
                                                   variables,
                                                   sections);
    scrollPanePAS.setVvalue(getPreviousPasScrollbar());
    previousPasScrollbarProperty().bind(scrollPanePAS.vvalueProperty());
    setTabContent(SubProgram.toString(SubProgram.PAS), scrollPanePAS);
    
    // Update source code
    setTabContent(SubProgram.toString(SubProgram.SC),
                  UISourceCode.buildSC(scene, sourceLine, assembly));
                  
    // Update SD
    scrollPaneSD = UISensitiveData.buildSD(this, 
                                           scene, 
                                           coordinator.runFilter.getAllSensitiveDataStates(coordinator.getRun()),
                                           coordinator.runFilter.getLastSensitiveDataState(coordinator.getRun()),
                                           sdVariableToView);
    setTabContent(SubProgram.toString(SubProgram.SD), scrollPaneSD);
                  
    // TODO: other tabs
    
  }
  
  
  public String saveConfig() {
    String config = "";
    config += "MainWindowX:" + Double.toString(window.getX()) + System.lineSeparator();
    config += "MainWindowY:" + Double.toString(window.getY()) + System.lineSeparator();
    config += "MainWindowWidth:" + Double.toString(window.getWidth()) + System.lineSeparator();
    config += "MainWindowHeight:" + Double.toString(window.getHeight()) + System.lineSeparator();
    config += "FontSize:" + Double.toString(fontSize) + System.lineSeparator();
    config += "VarRepFontSize:" + Double.toString(UIVariableRepresentation.fontSize) + System.lineSeparator();
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
        else if (parameters[0].equals("FontSize")) fontSize = Double.parseDouble(parameters[1]);
        else if (parameters[0].equals("VarRepFontSize")) UIVariableRepresentation.fontSize =
          Double.parseDouble(parameters[1]);
      }
    }
    for (UIDetachedTab tab : detachedTabs) tab.loadConfig(config);
  }

  

}
