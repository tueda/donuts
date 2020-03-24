package com.github.tueda.donuts.python;

import static com.google.common.truth.Truth.assertThat;

import com.github.tueda.donuts.Polynomial;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;

public class PythonUtilsTest {
  @Test
  public void createObjectInputStream() throws ClassNotFoundException, IOException {
    // Test by deserialization of a polynomial.

    Polynomial a = Polynomial.of("(1+x+y)^2");

    byte[] data;

    {
      ByteArrayOutputStream bstream = new ByteArrayOutputStream();
      ObjectOutputStream ostream = new ObjectOutputStream(bstream);
      ostream.writeObject(a);
      ostream.close();
      data = bstream.toByteArray();
    }

    Polynomial b;

    {
      ByteArrayInputStream bstream = new ByteArrayInputStream(data);
      ObjectInputStream ostream = PythonUtils.createObjectInputStream(bstream);
      b = (Polynomial) ostream.readObject();
      ostream.close();
    }

    assertThat(a).isEqualTo(b);
  }

  @Test
  public void sumOf() {
    // PythonUtils.sumOf() must be equivalent to Polynomial.sumOf().
    Polynomial a = Polynomial.of("1+x+y");
    Polynomial b = Polynomial.of("1+x-y");
    Polynomial c = Polynomial.of("1+z");
    Polynomial sum1 = PythonUtils.sumOf(new Polynomial[] {a, b, c});
    Polynomial sum2 = Polynomial.sumOf(a, b, c);
    Polynomial res = Polynomial.of("(1+x+y)+(1+x-y)+(1+z)");
    assertThat(sum1).isEqualTo(sum2);
    assertThat(sum1).isEqualTo(res);
  }

  @Test
  public void productOf() {
    // PythonUtils.productOf() must be equivalent to Polynomial.productOf().
    Polynomial a = Polynomial.of("1+x+y");
    Polynomial b = Polynomial.of("1+x-y");
    Polynomial c = Polynomial.of("1+z");
    Polynomial prod1 = PythonUtils.productOf(new Polynomial[] {a, b, c});
    Polynomial prod2 = Polynomial.productOf(a, b, c);
    Polynomial res = Polynomial.of("(1+x+y)*(1+x-y)*(1+z)");
    assertThat(prod1).isEqualTo(prod2);
    assertThat(prod1).isEqualTo(res);
  }

  @Test
  public void gcdOf() {
    // PythonUtils.gcdOf() must be equivalent to Polynomial.gcdOf().
    Polynomial a = Polynomial.of("(1+x)^3*(2+y)^5*(3+z)^2*(1+x+2*z)");
    Polynomial b = Polynomial.of("(1+x)^2*(2+y)^3*(3+z)^3*(7-5*x^2)");
    Polynomial c = Polynomial.of("(1+x)^2*(2+y)^2*(3+z)^5*(2-x-y)");
    Polynomial gcd1 = PythonUtils.gcdOf(new Polynomial[] {a, b, c});
    Polynomial gcd2 = Polynomial.gcdOf(a, b, c);
    Polynomial res = Polynomial.of("(1+x)^2*(2+y)^2*(3+z)^2");
    assertThat(gcd1).isEqualTo(gcd2);
    assertThat(gcd1).isEqualTo(res);
  }

  @Test
  public void lcmOf() {
    // PythonUtils.lcmOf() must be equivalent to Polynomial.lcmOf().
    Polynomial a = Polynomial.of("(1+x)^3*(2+y)^5*(3+z)^2*(1+x+2*z)");
    Polynomial b = Polynomial.of("(1+x)^2*(2+y)^3*(3+z)^3*(7-5*x^2)");
    Polynomial c = Polynomial.of("(1+x)^2*(2+y)^2*(3+z)^5*(2-x-y)");
    Polynomial lcm1 = PythonUtils.lcmOf(new Polynomial[] {a, b, c});
    Polynomial lcm2 = Polynomial.lcmOf(a, b, c);
    Polynomial res = Polynomial.of("(1+x)^3*(2+y)^5*(3+z)^5*(1+x+2*z)*(7-5*x^2)*(2-x-y)");
    assertThat(lcm1).isEqualTo(lcm2);
    assertThat(lcm1).isEqualTo(res);
  }
}
