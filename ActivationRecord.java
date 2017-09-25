import java.util.*;

public class ActivationRecord implements Comparable<ActivationRecord> {

  public String file;
  public String function;
  public long address;


  public ActivationRecord(String fileIn, String functionIn, long addressIn) {
    file = fileIn;
    function = functionIn;
    address = addressIn;
  }


  // copy constructor
  public ActivationRecord(ActivationRecord ar) {
    this(ar.file, ar.function, ar.address);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ActivationRecord ar = (ActivationRecord) o;

    if (file != null ? !file.equals(ar.file) : ar.file != null) return false;
    if (function != null ? !function.equals(ar.function) : ar.function != null) return false;
    if (address != ar.address) return false;

    return true;
  }


  @Override
  public String toString() {
    return file + "," + function + "," + address;
  }
  
  
  @Override
  public int hashCode() {
    return Objects.hash(file, function, address);
  }
  
  
  public int compareTo(ActivationRecord other) {
    return Long.compare(address, other.address);
	}

}
