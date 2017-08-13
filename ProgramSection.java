public class ProgramSection {
  public String id;
  public String type;
  public long address;
  public long offset;
  public long size;
  public String flags;
  public String name;
  
  public ProgramSection(String idIn,
                        String typeIn,
                        long addressIn,
                        long offsetIn,
                        long sizeIn,
                        String flagsIn,
                        String nameIn)
  {
    id = idIn;
    type = typeIn;
    address = addressIn;
    offset = offsetIn;
    size = sizeIn;
    flags = flagsIn;
    name = nameIn;
  }
  
  
  // copy constructor
  public ProgramSection(ProgramSection other) {
    this(other.id, other.type, other.address, other.offset, other.size, other.flags, other.name);
  }
  
  
  @Override
  public String toString() {
    return name;
  }
}
