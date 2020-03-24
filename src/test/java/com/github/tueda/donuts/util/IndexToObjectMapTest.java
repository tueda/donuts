package com.github.tueda.donuts.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class IndexToObjectMapTest {
  @Test
  public void test() {
    IndexToObjectMap<String> map = new IndexToObjectMap<>();

    map.put(2, "abc");
    map.put(5, "def");

    assertThat(map.get(0)).isNull();
    assertThat(map.get(1)).isNull();
    assertThat(map.get(2)).isEqualTo("abc");
    assertThat(map.get(3)).isNull();
    assertThat(map.get(4)).isNull();
    assertThat(map.get(5)).isEqualTo("def");
    assertThat(map.get(6)).isNull();

    assertThrows(IndexOutOfBoundsException.class, () -> map.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> map.put(-1, "abc"));
  }
}
