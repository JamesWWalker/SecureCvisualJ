import java.util.*;

public class Test {

  public static void main(String[] args) {
    ProcessRunFilter filter = new ProcessRunFilter();
    List<String> config = new ArrayList<>();
    config.add("DetailLevel:NOVICE");
    filter.loadConfig(config);
    System.out.println(filter.saveConfig());
    
    config.set(0, "DetailLevel:ADVANCED");
    filter.loadConfig(config);
    System.out.println(filter.saveConfig());
  }

}
