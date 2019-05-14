package com.github.tueda.donuts;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class VariableTest {
  @Test
  public void newInstance() {
    assertThrows(IllegalArgumentException.class, () -> new Variable(""));
    assertThrows(IllegalArgumentException.class, () -> new Variable("1"));
    assertThrows(IllegalArgumentException.class, () -> new Variable("a_"));
    assertThrows(IllegalArgumentException.class, () -> new Variable("$a"));
  }

  @Test
  public void compareTo() {
    String[] vars = {
      "a",
      "a0",
      "a0a",
      "a00",
      "a000",
      "a001",
      "a01",
      "a01b00",
      "a01b0001",
      "a1",
      "a1a",
      "a1b",
      "a2",
      "a2b8",
      "a2c7",
      "a2c08",
      "a03",
      "a3c8",
      "a009",
      "a0010",
      "a10",
      "a20",
      "a31",
      "a00100",
      "a290",
      "ab2",
      "ab03",
      "ab10",
      "b",
      "b1",
    };
    for (int i = 0; i < vars.length - 1; i++) {
      for (int j = i + 1; j < vars.length; j++) {
        Variable v1 = new Variable(vars[i]);
        Variable v2 = new Variable(vars[j]);
        assertWithMessage(v1 + " < " + v2).that(v1.compareTo(v2)).isLessThan(0);
        assertWithMessage(v2 + " > " + v1).that(v2.compareTo(v1)).isGreaterThan(0);
      }
    }
  }
}
