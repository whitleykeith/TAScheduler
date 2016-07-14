package comp110;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Shift extends HashSet<Employee> {

  /* Even though we don't serialize, this keeps Java from complaining... */
  private static final long serialVersionUID = 5723878473617645106L;

  private byte _hour;
  private int _capacity;

  public Shift(byte hour, int capacity) {
    _hour = hour;
    _capacity = capacity;
  }

  public String toString() {
    List<String> names = this.stream().map(e -> e.getName()).collect(Collectors.toList());
    return String.format("%02d", _hour) + ": " + String.join(", ", names);
  }

  public byte getHour() {
    return _hour;
  }

  public void setHour(byte hour) {
    _hour = hour;
  }

  public int getCapacity() {
    return _capacity;
  }

  public void setCapacity(int capacity) {
    _capacity = capacity;
  }

}
