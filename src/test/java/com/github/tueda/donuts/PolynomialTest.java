package com.github.tueda.donuts;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PolynomialTest {
  @Test
  public void newInstance() {
    Polynomial p1 = new Polynomial("x + y + z");
    Polynomial p2 = new Polynomial("z + y + x");
    assertThat(p1).isEqualTo(p2);

    p1 = new Polynomial("123");
    p2 = new Polynomial(123);
    assertThat(p1).isEqualTo(p2);

    // java.lang.ArithmeticException (not divisible) at cc.redberry.rings.Ring.divideExact
    assertThrows(IllegalArgumentException.class, () -> new Polynomial("2/x"));

    // java.lang.ArithmeticException (Negative exponent) at cc.redberry.rings.bigint.BigInteger.pow
    assertThrows(IllegalArgumentException.class, () -> new Polynomial("2^(-2)"));

    // java.lang.RuntimeException (illegal operand) at cc.redberry.rings.io.Coder.mkOperand
    assertThrows(IllegalArgumentException.class, () -> new Polynomial("1.2"));
    assertThrows(IllegalArgumentException.class, () -> new Polynomial("x;"));

    // java.lang.IllegalArgumentException (Can't parse) at cc.redberry.rings.io.Coder.parse
    assertThrows(IllegalArgumentException.class, () -> new Polynomial("(("));

    // java.lang.IllegalArgumentException (Exponents must be positive integers)
    // at cc.redberry.rings.io.Coder.popEvaluate
    assertThrows(IllegalArgumentException.class, () -> new Polynomial("x^x"));

    // java.lang.IllegalArgumentException (Illegal character)
    // at cc.redberry.rings.io.Tokenizer.checkChar
    assertThrows(IllegalArgumentException.class, () -> new Polynomial("="));

    // java.lang.IllegalArgumentException (spaces in variable name are forbidden)
    // at cc.redberry.rings.io.Tokenizer.nextToken
    assertThrows(IllegalArgumentException.class, () -> new Polynomial("x x"));

    // java.util.NoSuchElementException at java.util.ArrayDeque.removeFirst
    assertThrows(IllegalArgumentException.class, () -> new Polynomial(""));
  }

  @Test
  public void properties() {
    Polynomial p;

    p = new Polynomial();
    assertThat(p.isZero()).isTrue();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isTrue();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(0);
    assertThat(p.degree()).isEqualTo(0);
    assertThat(p.toString()).isEqualTo("0");

    p = new Polynomial(1);
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isTrue();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isTrue();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isTrue();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(0);
    assertThat(p.toString()).isEqualTo("1");

    p = new Polynomial(-1);
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isTrue();
    assertThat(p.isConstant()).isTrue();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(0);

    p = new Polynomial("x");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isTrue();
    assertThat(p.isVariable()).isTrue();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(1);
    assertThat(p.toString()).isEqualTo("x");

    p = new Polynomial("-x");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(1);

    p = new Polynomial("2*x");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(1);

    p = new Polynomial("x^2");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isTrue();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(2);

    p = new Polynomial("(1+x+y)^2");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isMonomial()).isFalse();
    assertThat(p.isMonic()).isTrue();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(6);
    assertThat(p.degree()).isEqualTo(2);
  }

  @Test
  public void iterator() {
    Polynomial p = new Polynomial("(x + y - 2 * z)^2");
    Polynomial q = new Polynomial();
    int n = 0;
    for (Polynomial t : p) {
      assertThat(t.isMonomial()).isTrue();
      q = q.add(t);
      n++;
    }
    assertThat(q).isEqualTo(p);
    assertThat(n).isEqualTo(p.size());
  }

  @Test
  public void degree() {
    Polynomial p = new Polynomial("(1 + x + y + z + w)^2 + (y + z + w)^3 + (z + w)^4 + x*y*z*w^5");

    assertThat(p.degree()).isEqualTo(8);

    assertThat(p.degree(Variable.of("w"))).isEqualTo(5);
    assertThat(p.degree(Variable.of("x"))).isEqualTo(2);
    assertThat(p.degree(Variable.of("a"))).isEqualTo(0);

    assertThat(p.degree(VariableSet.of("x", "z", "w"))).isEqualTo(7);
    assertThat(p.degree(VariableSet.of("a", "x", "z", "z0", "z1"))).isEqualTo(4);
    assertThat(p.degree(VariableSet.of("a", "b", "y"))).isEqualTo(3);
    assertThat(p.degree(VariableSet.of("a", "b", "c"))).isEqualTo(0);
  }

  @Test
  public void coefficientOf() {
    Polynomial p = new Polynomial("(1 + x + y + z)^4");

    assertThat(p.coefficientOf(Variable.of("x"), 0)).isEqualTo(new Polynomial("(1 + y + z)^4"));
    assertThat(p.coefficientOf(Variable.of("x"), 1)).isEqualTo(new Polynomial("4 * (1 + y + z)^3"));
    assertThat(p.coefficientOf(Variable.of("x"), 2)).isEqualTo(new Polynomial("6 * (1 + y + z)^2"));
    assertThat(p.coefficientOf(Variable.of("y"), 3)).isEqualTo(new Polynomial("4 * (1 + x + z)"));
    assertThat(p.coefficientOf(Variable.of("z"), 4)).isEqualTo(new Polynomial("1"));
    assertThat(p.coefficientOf(Variable.of("a"), 1)).isEqualTo(new Polynomial("0"));

    assertThat(p.coefficientOf(Variable.of("a", "y", "b"), new int[] {0, 2, 0}))
        .isEqualTo(new Polynomial("6 * (1 + x + z)^2"));
    assertThat(p.coefficientOf(Variable.of("y", "z"), new int[] {1, 2}))
        .isEqualTo(new Polynomial("12 * (1 + x)"));
    assertThat(p.coefficientOf(Variable.of("a", "x"), new int[] {1, 2}))
        .isEqualTo(new Polynomial("0"));
  }

  @Test
  public void negate() {
    Polynomial zero = new Polynomial();
    Polynomial p1 = zero.negate();
    Polynomial p2 = new Polynomial("1 - x");
    Polynomial p3 = p2.negate();
    Polynomial p4 = p2.add(p3);
    assertThat(p1).isEqualTo(zero);
    assertThat(p4).isEqualTo(zero);
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
