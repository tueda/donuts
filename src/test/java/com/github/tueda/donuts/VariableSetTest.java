package com.github.tueda.donuts;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

public class VariableSetTest {
  @Test
  public void union() {
    VariableSet setA = VariableSet.of("a", "b");
    VariableSet setB = VariableSet.of("b", "c");
    VariableSet setC = VariableSet.of("a", "c");
    assertThat(setA.union(setB).union(setC)).isEqualTo(VariableSet.of("a", "b", "c"));
  }
}
