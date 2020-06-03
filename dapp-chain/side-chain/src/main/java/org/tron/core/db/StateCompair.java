package org.tron.core.db;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class StateCompair {

  String resA;
  String resB;

  public void fill(String s) {
    if (resA == null) {
      resA = s;
    } else {
      resB = s;
    }
  }

  public boolean compare() {
    return resA != null && resB != null && resA.equals(resB);
  }

  public void clear() {
    resA = null;
    resB = null;
  }

}
