package com.github.tueda.donuts;

import cc.redberry.rings.bigint.BigInteger;

public class TestUtils {
  /**
   * Returns an array of {@code int}.
   *
   * @param values the values
   * @return an array of {@code int}
   */
  public static int[] ints(int... values) {
    return values;
  }

  /**
   * Returns an array of {@code BigInteger}.
   *
   * @param values the values
   * @return an array of {@code BigInteger}
   */
  public static BigInteger[] bigInts(int... values) {
    BigInteger[] result = new BigInteger[values.length];
    for (int i = 0; i < values.length; i++) {
      result[i] = BigInteger.valueOf(values[i]);
    }
    return result;
  }
}
