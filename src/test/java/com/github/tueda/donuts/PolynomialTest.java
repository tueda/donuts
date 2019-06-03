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

  @Test
  public void add() {
    Polynomial p1 = new Polynomial("x + y");
    Polynomial p2 = new Polynomial("y + z");
    Polynomial p3 = new Polynomial("z + x");
    Polynomial q = p1.add(p2).add(p3);
    Polynomial r = new Polynomial("2 * x + 2 * y + 2 * z + w - 1 * w");
    assertThat(q).isEqualTo(r);
  }

  @Test
  public void subtract() {
    Polynomial p1 = new Polynomial("x + y + z");
    Polynomial p2 = new Polynomial("x");
    Polynomial p3 = new Polynomial("y + z");
    Polynomial q = p1.subtract(p2).subtract(p3);
    Polynomial r = new Polynomial();
    assertThat(q).isEqualTo(r);
  }

  @Test
  public void multiply() {
    Polynomial p1 = new Polynomial("x + y + z");
    Polynomial p2 = new Polynomial("x - y - z");
    Polynomial q = p1.multiply(p2);
    Polynomial r = new Polynomial("x^2 - (y + z)^2");
    assertThat(q).isEqualTo(r);
  }

  @Test
  public void pow() {
    Polynomial p1 = new Polynomial("1-x+y");
    Polynomial p2 = p1.pow(2);
    Polynomial p3 = p1.pow(3);
    Polynomial p4 = new Polynomial("(1-x+y)^2");
    Polynomial p5 = new Polynomial("(1-x+y)^3");
    assertThat(p2).isEqualTo(p4);
    assertThat(p3).isEqualTo(p5);
  }
}
