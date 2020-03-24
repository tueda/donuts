package com.github.tueda.donuts.util;

import static com.google.common.truth.Truth.assertThat;

import java.util.Comparator;
import org.junit.jupiter.api.Test;

public class IntArrayComparatorTest {
  @Test
  public void test() {
    Comparator<int[]> cmp = new IntArrayComparator();

    {
      int[] a = {1};

      assertThat(cmp.compare(null, null)).isEqualTo(0);
      assertThat(cmp.compare(null, a)).isEqualTo(-1);
      assertThat(cmp.compare(a, null)).isEqualTo(1);
    }

    {
      java.util.List<int[]> list = new java.util.ArrayList<>();

      list.add(new int[] {1});
      list.add(new int[] {1});
      list.add(new int[] {1, 1});
      list.add(new int[] {2});
      list.add(new int[] {3, 1});

      assertThat(list).isInOrder(cmp);
    }

    {
      int[] a = {2};
      int[] b = {1};

      assertThat(cmp.compare(a, b)).isGreaterThan(0);
    }

    {
      int[] a = {1, 1};
      int[] b = {1};

      assertThat(cmp.compare(a, b)).isGreaterThan(0);
    }
  }
}
