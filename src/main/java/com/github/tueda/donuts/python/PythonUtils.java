package com.github.tueda.donuts.python;

import com.github.tueda.donuts.Polynomial;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import lombok.experimental.UtilityClass;

/** This class consists of static utility methods for Python binding. */
@UtilityClass
public class PythonUtils {
  // The following code is a workaround for kivy/pyjnius#453.

  /** Our own ObjectInputStream. See https://stackoverflow.com/a/12180547. */
  private static class ObjectInputStream2 extends ObjectInputStream {
    /** Construct an object input stream. */
    public ObjectInputStream2(final InputStream in) throws IOException {
      super(in);
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass desc) throws ClassNotFoundException {
      return Class.forName(desc.getName(), false, Thread.currentThread().getContextClassLoader());
    }
  }

  /**
   * Construct an object input stream. This function avoids JDK-4340158.
   *
   * @param in the input stream to read from
   * @return the resultant input stream
   * @throws IOException when an I/O error occurs
   */
  public static ObjectInputStream createObjectInputStream(final InputStream in) throws IOException {
    return new ObjectInputStream2(in);
  }

  // The following methods are defined in order to avoid the issues of overloading static methods
  // with variable arguments in Pyjnius.

  /**
   * Returns the sum of the given polynomials.
   *
   * @param polynomials the polynomials for which the sum is to be computed
   * @return {@code polynomial1 + ... + polynomialN}
   */
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.UseVarargs"})
  public static Polynomial sumOf(final Polynomial[] polynomials) {
    return Polynomial.sumOf(polynomials);
  }

  /**
   * Returns the product of the given polynomials.
   *
   * @param polynomials the polynomials for which the product is to be computed
   * @return {@code polynomial1 * ... * polynomialN}
   */
  @SuppressWarnings("PMD.UseVarargs")
  public static Polynomial productOf(final Polynomial[] polynomials) {
    return Polynomial.productOf(polynomials);
  }

  /**
   * Returns the greatest common divisor of the given polynomials.
   *
   * @param polynomials the polynomials for which the GCD is to be computed
   * @return {@code GCD(polynomial1, ..., polynomialN)}
   */
  @SuppressWarnings("PMD.UseVarargs")
  public static Polynomial gcdOf(final Polynomial[] polynomials) {
    return Polynomial.gcdOf(polynomials);
  }

  /**
   * Returns the least common multiple of the given polynomials.
   *
   * @param polynomials the polynomials for which the LCM is to be computed
   * @return {@code LCM(polynomial1, ..., polynomialN)}
   * @throws IllegalArgumentException when no polynomial is given
   */
  @SuppressWarnings("PMD.UseVarargs")
  public static Polynomial lcmOf(final Polynomial[] polynomials) {
    return Polynomial.lcmOf(polynomials);
  }
}
