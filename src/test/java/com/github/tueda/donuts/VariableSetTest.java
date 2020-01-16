package com.github.tueda.donuts;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class VariableSetTest {
  @Test
  public void newInstance() {
    VariableSet s1 = VariableSet.of("x", "y", "z");

    // check auxiliary constructors

    VariableSet s2 = new VariableSet(Variable.of("x"), Variable.of("y"), Variable.of("z"));

    assertThat(s2).isEqualTo(s1);

    List<Variable> v = new ArrayList<>();
    v.add(Variable.of("x"));
    v.add(Variable.of("y"));
    v.add(Variable.of("z"));

    VariableSet s3 = new VariableSet(v);

    assertThat(s3).isEqualTo(s1);
  }

  @Test
  public void equals() {
    VariableSet s1 = VariableSet.of("x", "y", "z");
    VariableSet s2 = VariableSet.of("x", "z", "y", "x");
    VariableSet s3 = VariableSet.of("a", "b", "c");
    VariableSet s4 = VariableSet.of("a");

    assertThat(s1.equals(s1)).isTrue();
    assertThat(s1.equals(s2)).isTrue();
    assertThat(s1.equals(s3)).isFalse();
    assertThat(s1.equals(s4)).isFalse();

    assertThat(s1.equals(1)).isFalse();
  }

  @Test
  public void contains() {
    assertThat(VariableSet.of("a", "b").contains(Variable.of("a"))).isTrue();
    assertThat(VariableSet.of("a", "b").contains(Variable.of("x"))).isFalse();
    assertThat(VariableSet.of("a", "b").contains(1)).isFalse();

    assertThat(VariableSet.of("x2", "x10").contains(Variable.of("x10"))).isTrue();
  }

  @Test
  public void getVariables() {
    VariableSet s1 = VariableSet.of("a", "b");
    VariableSet empty = VariableSet.of();

    assertThat(s1.getVariables()).isEqualTo(s1);
    assertThat(s1.getMinimalVariables()).isEqualTo(empty);
  }

  @Test
  public void intersects() {
    assertThat(VariableSet.of("a", "d", "e").intersects(VariableSet.of("b", "c", "d", "f")))
        .isTrue();
    assertThat(VariableSet.of("a", "e", "g").intersects(VariableSet.of("b", "c", "d", "f")))
        .isFalse();
  }

  @Test
  public void intersection() {
    assertThat(VariableSet.of("a", "b").intersection(VariableSet.of("a", "b")))
        .isEqualTo(VariableSet.of("a", "b"));
    assertThat(VariableSet.of().intersection(VariableSet.of("a", "b"))).isEqualTo(VariableSet.of());
    assertThat(VariableSet.of("a", "b").intersection(VariableSet.of())).isEqualTo(VariableSet.of());
    assertThat(VariableSet.of("a").intersection(VariableSet.of("a", "b")))
        .isEqualTo(VariableSet.of("a"));
    assertThat(VariableSet.of("a", "b").intersection(VariableSet.of("b")))
        .isEqualTo(VariableSet.of("b"));
    assertThat(VariableSet.of("a", "b").intersection(VariableSet.of("c", "d")))
        .isEqualTo(VariableSet.of());
  }

  @Test
  public void union() {
    VariableSet setA = VariableSet.of("a", "b");
    VariableSet setB = VariableSet.of("b", "c");
    VariableSet setC = VariableSet.of("a", "c");
    assertThat(setA.union(setB).union(setC)).isEqualTo(VariableSet.of("a", "b", "c"));

    // check auxiliary methods

    assertThat(VariableSet.unionOf()).isEqualTo(VariableSet.of());

    assertThat(
            VariableSet.unionOf(Polynomial.of("x+y"), Polynomial.of("y+z"), Polynomial.of("z+x")))
        .isEqualTo(VariableSet.of("x", "y", "z"));

    List<Multivariate> v = new ArrayList<>();
    v.add(Polynomial.of("x+y"));
    v.add(Polynomial.of("y+z"));
    v.add(Polynomial.of("z+x"));
    assertThat(VariableSet.unionOf(v)).isEqualTo(VariableSet.of("x", "y", "z"));
  }

  @Test
  public void map() {
    assertThat(VariableSet.of("a", "c", "e").map(VariableSet.of("a", "b", "c", "d", "e")))
        .isEqualTo(new int[] {0, 2, 4});

    assertThat(VariableSet.of("a", "c", "f").map(VariableSet.of("a", "b", "c", "d", "e"))).isNull();

    assertThat(VariableSet.of("x10").map(VariableSet.of("x2", "x10"))).isEqualTo(new int[] {1});
  }
}
