import java.util.*;

public class ProgramSection {
  public List<String> values;
  
  public ProgramSection(List<String> valuesIn)
  {
    values = new ArrayList<>();
    for (String s : valuesIn) values.add(s);
  }
  
  
  // copy constructor
  public ProgramSection(ProgramSection other) {
    this(other.values);
  }
  
  
  @Override
  public String toString() {
    return values.toString();
  }
}
