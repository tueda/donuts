package com.github.tueda.donuts.util;

import java.io.Serializable;
import java.util.Comparator;
import lombok.EqualsAndHashCode;

/** A comparator for arrays of integers. */
@EqualsAndHashCode
public class IntArrayComparator implements Comparator<int[]>, Serializable {
  private static final long serialVersionUID = 1L;

  /** Construct a comparator. */
  public IntArrayComparator() {
    // Do nothing.
  }

  @Override
  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  public int compare(final int[] a1, final int[] a2) {
    if (a1 == a2) {
      return 0;
    }
    if (a1 == null) {
      return -1;
    }
    if (a2 == null) {
      return 1;
    }

    final int n1 = a1.length;
    final int n2 = a2.length;

    if (n1 < n2) {
      final int n = compareImpl(a1, a2, n1);
      return n == 0 ? -1 : n;
    } else if (n1 > n2) {
      final int n = compareImpl(a1, a2, n2);
      return n == 0 ? 1 : n;
    } else {
      return compareImpl(a1, a2, n1);
    }
  }

  private static int compareImpl(final int[] a1, final int[] a2, final int n) {
    for (int i = 0; i < n; i++) {
      final int e1 = a1[i];
      final int e2 = a2[i];
      if (e1 < e2) {
        return -1;
      } else if (e1 > e2) {
        return 1;
      }
    }
    return 0;
  }
}
