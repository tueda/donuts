package com.github.tueda.donuts;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ReadMeTest {
  @Test
  public void example() {
    Polynomial a = new Polynomial("1 + x + y");
    assertThat(a.toString()).isEqualTo("1+y+x");

    Polynomial b = new Polynomial("1 + y + z");
    assertThat(b.toString()).isEqualTo("1+z+y");

    Polynomial g = a.add(b);
    assertThat(g.toString()).isEqualTo("2+z+2*y+x");

    Polynomial ag = a.multiply(g);
    assertThat(ag.toString()).isEqualTo("2+z+4*y+3*x+y*z+x*z+2*y^2+3*x*y+x^2");

    Polynomial bg = b.multiply(g);
    assertThat(bg.toString()).isEqualTo("2+3*z+4*y+x+z^2+3*y*z+x*z+2*y^2+x*y");

    Polynomial gcd = ag.gcd(bg);
    assertThat(gcd.toString()).isEqualTo("2+z+2*y+x");

    RationalFunction div = ag.divide(bg);
    assertThat(div.toString()).isEqualTo("(1+y+x)/(1+z+y)");

    Polynomial[] fac = new Polynomial("-2*x^4*y^3 + 2*x^3*y^4 + 2*x^2*y^5 - 2*x*y^6").factors();
    assertThat(Arrays.toString(fac)).isEqualTo("[-2, y, y, y, x, -y+x, -y+x, y+x]");
  }
}
