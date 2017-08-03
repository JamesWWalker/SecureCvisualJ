
public class ActivationRecord {

  public String file;
  public String function;


  public ActivationRecord(String fileIn, String functionIn) {
    file = fileIn;
    function = functionIn;
  }


  // copy constructor
  public ActivationRecord(ActivationRecord ar) {
    this(ar.file, ar.function);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ActivationRecord ar = (ActivationRecord) o;

    if (file != null ? !file.equals(ar.file) : ar.file != null) return false;
    if (function != null ? !function.equals(ar.function) : ar.function != null) return false;

    return true;
  }


  @Override
  public String toString() {
    return file + "," + function;
  }

}
