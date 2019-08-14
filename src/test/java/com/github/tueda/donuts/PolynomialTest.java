package com.github.tueda.donuts;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cc.redberry.rings.bigint.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
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

    p = Polynomial.of("0");
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

    p = Polynomial.of("1");
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

    p = Polynomial.of("-1");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isTrue();
    assertThat(p.isConstant()).isTrue();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(0);

    p = Polynomial.of("x");
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

    p = Polynomial.of("-x");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(1);

    p = Polynomial.of("2*x");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(1);

    p = Polynomial.of("x^2");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isTrue();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(2);

    p = Polynomial.of("(1+x+y)^2");
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
  public void immutability() {
    checkUnaryOperatorImmutability(Polynomial::negate);
    checkBinaryOperatorImmutability(Polynomial::add);
    checkBinaryOperatorImmutability(Polynomial::subtract);
    checkBinaryOperatorImmutability(Polynomial::multiply);
    checkBinaryOperatorImmutability(Polynomial::divideExact);
    checkUnaryOperatorImmutability(p -> p.pow(5));
    checkUnaryOperatorImmutability(p -> p.pow(new BigInteger("5")));
    checkBinaryOperatorImmutability((p1, p2) -> p1.gcd(p2));
    checkBinaryOperatorImmutability((p1, p2) -> Polynomial.gcd(p1, p2));
  }

  void checkUnaryOperatorImmutability(UnaryOperator<Polynomial> operator) {
    String s = "(1+a-b)*(2-x)";
    Polynomial a = new Polynomial(s);
    Polynomial b = new Polynomial(s);
    VariableSet v = VariableSet.union(a, b);
    a = a.translate(v);
    b = b.translate(v);
    operator.apply(a);
    assertThat(a).isEqualTo(b);
  }

  void checkBinaryOperatorImmutability(BinaryOperator<Polynomial> operator) {
    {
      String s1 = "6*(1+a-b)*(2-x)*(1+x+y)";
      String s2 = "2*(1+a-b)*(2-x)+z-z";
      Polynomial a1 = new Polynomial(s1);
      Polynomial a2 = new Polynomial(s2);
      Polynomial b1 = new Polynomial(s1);
      Polynomial b2 = new Polynomial(s2);
      operator.apply(a1, a2);
      assertThat(a1).isEqualTo(b1);
      assertThat(a2).isEqualTo(b2);
    }
    {
      String s1 = "6*(1+a-b)*(2-x)*(1+x+y)";
      String s2 = "2*(1+a-b)*(2-x)+z-z";
      Polynomial a1 = new Polynomial(s1);
      Polynomial a2 = new Polynomial(s2);
      Polynomial b1 = new Polynomial(s1);
      Polynomial b2 = new Polynomial(s2);
      VariableSet v = VariableSet.union(a1, a2, b1, b2);
      a1 = a1.translate(v);
      a2 = a2.translate(v);
      b1 = b1.translate(v);
      b2 = b2.translate(v);
      operator.apply(a1, a2);
      assertThat(a1).isEqualTo(b1);
      assertThat(a2).isEqualTo(b2);
    }
  }

  @Test
  public void iterator() {
    Polynomial p = Polynomial.of("(x + y - 2 * z)^2");
    Polynomial q = Polynomial.of("0");
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
  public void getMinimalVariables() {
    Polynomial p1 = Polynomial.of("  + b + c + d + e");
    Polynomial p2 = Polynomial.of("a - b + c - d");
    Polynomial p = p1.add(p2);
    assertThat(p.getMinimalVariables()).isEqualTo(VariableSet.of("a", "c", "e"));
  }

  @Test
  public void degree() {
    Polynomial p = Polynomial.of("(1 + x + y + z + w)^2 + (y + z + w)^3 + (z + w)^4 + x*y*z*w^5");

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

    assertThat(p.coefficientOf(Variable.of("x"), 0)).isEqualTo(Polynomial.of("(1 + y + z)^4"));
    assertThat(p.coefficientOf(Variable.of("x"), 1)).isEqualTo(Polynomial.of("4 * (1 + y + z)^3"));
    assertThat(p.coefficientOf(Variable.of("x"), 2)).isEqualTo(Polynomial.of("6 * (1 + y + z)^2"));
    assertThat(p.coefficientOf(Variable.of("y"), 3)).isEqualTo(Polynomial.of("4 * (1 + x + z)"));
    assertThat(p.coefficientOf(Variable.of("z"), 4)).isEqualTo(Polynomial.of("1"));
    assertThat(p.coefficientOf(Variable.of("a"), 1)).isEqualTo(Polynomial.of("0"));

    assertThat(p.coefficientOf(Variable.of("a", "y", "b"), new int[] {0, 2, 0}))
        .isEqualTo(Polynomial.of("6 * (1 + x + z)^2"));
    assertThat(p.coefficientOf(Variable.of("y", "z"), new int[] {1, 2}))
        .isEqualTo(Polynomial.of("12 * (1 + x)"));
    assertThat(p.coefficientOf(Variable.of("a", "x"), new int[] {1, 2}))
        .isEqualTo(Polynomial.of("0"));
  }

  @Test
  public void negate() {
    Polynomial zero = new Polynomial();
    Polynomial p1 = zero.negate();
    Polynomial p2 = Polynomial.of("1 - x");
    Polynomial p3 = p2.negate();
    Polynomial p4 = p2.add(p3);
    assertThat(p1).isEqualTo(zero);
    assertThat(p4).isEqualTo(zero);
  }

  @Test
  public void add() {
    Polynomial p1 = Polynomial.of("x + y");
    Polynomial p2 = Polynomial.of("y + z");
    Polynomial p3 = Polynomial.of("z + x");
    Polynomial q = p1.add(p2).add(p3);
    Polynomial r = Polynomial.of("2 * x + 2 * y + 2 * z + w - 1 * w");
    assertThat(q).isEqualTo(r);
  }

  @Test
  public void subtract() {
    Polynomial p1 = Polynomial.of("x + y + z");
    Polynomial p2 = Polynomial.of("x");
    Polynomial p3 = Polynomial.of("y + z");
    Polynomial q = p1.subtract(p2).subtract(p3);
    Polynomial r = new Polynomial();
    assertThat(q).isEqualTo(r);
  }

  @Test
  public void multiply() {
    Polynomial p1 = Polynomial.of("x + y + z");
    Polynomial p2 = Polynomial.of("x - y - z");
    Polynomial q = p1.multiply(p2);
    Polynomial r = Polynomial.of("x^2 - (y + z)^2");
    assertThat(q).isEqualTo(r);
  }

  @Test
  public void pow() {
    Polynomial p1 = Polynomial.of("1-x+y");
    Polynomial p2 = p1.pow(2);
    Polynomial p3 = p1.pow(3);
    Polynomial p4 = Polynomial.of("(1-x+y)^2");
    Polynomial p5 = Polynomial.of("(1-x+y)^3");
    assertThat(p2).isEqualTo(p4);
    assertThat(p3).isEqualTo(p5);

    Polynomial p6 = Polynomial.of("1-x").pow(new BigInteger("30"));
    Polynomial p7 = Polynomial.of("(1-x)^30");
    assertThat(p6).isEqualTo(p7);
  }

  @Test
  public void gcd() {
    Polynomial p = Polynomial.of("x + y");
    Polynomial q = Polynomial.of("y + z");
    Polynomial r = Polynomial.of("1 + x + w");
    Polynomial g = Polynomial.of("2 + x + y + z");
    Polynomial a = g.multiply(p);
    Polynomial b = g.multiply(q);
    Polynomial c = g.multiply(r);

    Polynomial gcd1 = a.gcd(b);
    assertThat(gcd1).isEqualTo(g);

    Polynomial gcd2 = Polynomial.gcd(a, b, c);
    assertThat(gcd2).isEqualTo(g);

    Polynomial[] polys2 = {a, b, c};
    Polynomial gcd3 = Polynomial.gcd(polys2);
    assertThat(gcd3).isEqualTo(g);

    List<Polynomial> polys3 = new ArrayList<>();
    polys3.add(a);
    polys3.add(b);
    polys3.add(c);
    Polynomial gcd4 = Polynomial.gcd(polys3);
    assertThat(gcd4).isEqualTo(g);

    Polynomial d = a.divideExact(gcd1);
    Polynomial e = b.divideExact(gcd1);
    Polynomial f = c.divideExact(gcd1);
    assertThat(d).isEqualTo(p);
    assertThat(e).isEqualTo(q);
    assertThat(f).isEqualTo(r);

    List<Polynomial> polys4 = new ArrayList<>();
    Polynomial gcd5 = Polynomial.gcd(polys4);
    Polynomial zero = new Polynomial();
    assertThat(gcd5).isEqualTo(zero);

    List<Polynomial> polys5 = new ArrayList<>();
    polys5.add(a);
    Polynomial gcd6 = Polynomial.gcd(polys5);
    assertThat(gcd6).isEqualTo(a);
  }
}
