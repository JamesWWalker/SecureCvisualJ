import java.util.*;

public class VariableDelta {

  public String type;
  public String scope;
  public String name;
  public String value;
  public long address;
  public long pointsTo;


  public VariableDelta(
    String typeIn,
    String scopeIn,
    String nameIn,
    String valueIn,
    long addressIn)
  {
    type = typeIn;
    scope = scopeIn;
    name = nameIn;
    value = valueIn;
    address = addressIn;
    pointsTo = -1;
  }


  public VariableDelta(
    String typeIn,
    String scopeIn,
    String nameIn,
    String valueIn,
    long addressIn,
    long pointsToIn)
  {
    type = typeIn;
    scope = scopeIn;
    name = nameIn;
    value = valueIn;
    address = addressIn;
    pointsTo = pointsToIn;
  }


  // Copy constructor
  public VariableDelta(VariableDelta delta) {
    this(delta.type, delta.scope, delta.name, delta.value, delta.address, delta.pointsTo);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VariableDelta delta = (VariableDelta) o;

    if (scope != null ? !scope.equals(delta.scope) : delta.scope != null) return false;
    if (name != null ? !name.equals(delta.name) : delta.name != null) return false;
    if (type != null ? !type.equals(delta.type) : delta.type != null) return false;
    if (value != null ? !value.equals(delta.value) : delta.value != null) return false;
    if (address != delta.address) return false;

    return true;
  }
  
  
  @Override
  public int hashCode() {
    return Objects.hash(type, scope, name, value, address, pointsTo);
  }


  @Override
  public String toString() {
    return "[" + scope + "," + name + "]=" + value + System.lineSeparator();
  }


  public boolean isSameVariable(VariableDelta o) {
    return type.equals(o.type) &&
           scope.equals(o.scope) &&
           name.equals(o.name);
  }


  public String getKey() {
    return scope + "," + name;
  }


  public static void applyDelta(VariableDelta target, VariableDelta delta) {
    assert target.isSameVariable(delta);
    target.type = delta.type;
    target.scope = delta.scope;
    target.name = delta.name;
    target.value = delta.value;
    target.address = delta.address;
  }

} // VariableDelta class
