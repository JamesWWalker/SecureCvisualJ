import java.util.*;

public class ActivationRecord implements Comparable<ActivationRecord> {

  public String file;
  public String function;
  public long address;
  String dynamicLink;
  String returnAddress;


  public ActivationRecord(String fileIn, String functionIn, long addressIn) {
    file = fileIn;
    function = functionIn;
    address = addressIn;
  }
  
  
  public ActivationRecord(String fileIn, 
                          String functionIn, 
                          long addressIn,
                          String dynamicLinkIn,
                          String returnAddressIn) 
  {
    file = fileIn;
    function = functionIn;
    address = addressIn;
    dynamicLink = dynamicLinkIn;
    returnAddress = returnAddressIn;
  }


  // copy constructor
  public ActivationRecord(ActivationRecord ar) {
    this(ar.file, ar.function, ar.address, ar.dynamicLink, ar.returnAddress);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ActivationRecord ar = (ActivationRecord) o;

    if (file != null ? !file.equals(ar.file) : ar.file != null) return false;
    if (function != null ? !function.equals(ar.function) : ar.function != null) return false;
    if (address != ar.address) return false;
    if (dynamicLink != null ? !dynamicLink.equals(ar.dynamicLink) 
      : ar.dynamicLink != null) return false;
    if (returnAddress != null ? !returnAddress.equals(ar.returnAddress) 
      : ar.returnAddress != null) return false;

    return true;
  }


  @Override
  public String toString() {
    return file + "," + function + "," + address + "," + dynamicLink + "," + returnAddress;
  }
  
  
  @Override
  public int hashCode() {
    return Objects.hash(file, function, address, dynamicLink, returnAddress);
  }
  
  
  public int compareTo(ActivationRecord other) {
    return Long.compare(other.address, address); // reverse order
	}

}
