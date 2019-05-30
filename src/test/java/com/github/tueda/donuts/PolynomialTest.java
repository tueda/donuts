package com.github.tueda.donuts;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

public class PolynomialTest {
  @Test
  public void newInstance() {
    Polynomial p1 = new Polynomial("x + y + z");
    Polynomial p2 = new Polynomial("z + y + x");
    assertThat(p1).isEqualTo(p2);
  }
}
