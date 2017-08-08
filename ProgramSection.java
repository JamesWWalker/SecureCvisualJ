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
}
