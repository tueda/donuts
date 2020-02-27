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

  /** Construct an object input stream, which avoids JDK-4340158. */
  public static ObjectInputStream createObjectInputStream(final InputStream in) throws IOException {
    return new ObjectInputStream2(in);
  }

  // The following methods are defined in order to avoid the issues of overloading static methods
  // with variable arguments in Pyjnius.

  /** Returns the greatest common divisor of the given polynomials. */
  @SuppressWarnings("PMD.UseVarargs")
  public static Polynomial gcdOf(final Polynomial[] polynomials) {
    return Polynomial.gcdOf(polynomials);
  }

  /**
   * Returns the least common multiple of the given polynomials.
   *
   * @throws IllegalArgumentException when no polynomial is given
   */
  @SuppressWarnings("PMD.UseVarargs")
  public static Polynomial lcmOf(final Polynomial[] polynomials) {
    return Polynomial.lcmOf(polynomials);
  }
}