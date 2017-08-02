
public class ActivationRecord {

  public String function;


  public ActivationRecord(String functionIn) {
    function = functionIn;
  }


  // copy constructor
  public ActivationRecord(ActivationRecord ar) {
    this(ar.function);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ActivationRecord ar = (ActivationRecord) o;

    if (function != null ? !function.equals(ar.function) : ar.function != null) return false;

    return true;
  }

}
