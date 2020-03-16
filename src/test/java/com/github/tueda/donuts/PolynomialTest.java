package com.github.tueda.donuts;

import static com.github.tueda.donuts.TestUtils.bigInts;
import static com.github.tueda.donuts.TestUtils.ints;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cc.redberry.rings.bigint.BigInteger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    assertThrows(
        IllegalArgumentException.class,
        () -> new Polynomial("3.1415926535897932384626433832795028841971693993751"));
  }

  @Test
  public void constants() {
    assertThat(Polynomial.ZERO).isEqualTo(new Polynomial(0));
    assertThat(Polynomial.ONE).isEqualTo(new Polynomial(1));
  }

  @Test
  public void of() {
    RationalFunction r1 = RationalFunction.of("1+x/y");
    RationalFunction r2 = RationalFunction.of("1+y/z");
    RationalFunction r3 = RationalFunction.of("1+z/x");
    RationalFunction[] rr = RationalFunction.of("1+x/y", "1+y/z", "1+z/x");

    assertThat(rr.length).isEqualTo(3);
    assertThat(rr[0]).isEqualTo(r1);
    assertThat(rr[1]).isEqualTo(r2);
    assertThat(rr[2]).isEqualTo(r3);
  }

  @Test
  public void serialization() throws IOException, ClassNotFoundException {
    // NOTE: this test is imperfect in the sense that the serialization and deserialization are
    // done in the same process.
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
      ObjectInputStream ostream = new ObjectInputStream(bstream);
      b = (Polynomial) ostream.readObject();
      ostream.close();
    }

    assertThat(a).isEqualTo(b);
  }

  @Test
  public void equals() {
    Polynomial x = Polynomial.of("x");
    Polynomial y = Polynomial.of("y");
    Polynomial z = Polynomial.of("z");

    Polynomial zero = Polynomial.of("0");

    Polynomial p = x.add(y).add(z);
    Polynomial q = z.add(y).add(x);

    assertThat(x.equals(x)).isTrue();

    assertThat(x).isEqualTo(x);
    assertThat(x).isNotEqualTo(y);

    assertThat(x).isNotEqualTo(1);
    assertThat(x).isNotEqualTo(y.add(z));
    assertThat(p.subtract(p)).isEqualTo(Polynomial.of("0"));

    assertThat(Polynomial.of("1")).isEqualTo(new Polynomial(new BigInteger("1")));

    // Unfortunately, the followings won't be equal.
    assertThat(Polynomial.of("1")).isNotEqualTo(1);
    assertThat(Polynomial.of("x")).isNotEqualTo(Variable.of("x"));
  }

  @Test
  public void hashCodeTest() {
    Polynomial p = Polynomial.of("1+x");
    Polynomial q = Polynomial.of("2+x+y-1-y");
    assertThat(p).isEqualTo(q);
    assertThat(p.hashCode()).isEqualTo(q.hashCode());
  }

  @Test
  public void properties() {
    Polynomial p;

    p = Polynomial.of("0");
    assertThat(p.isZero()).isTrue();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isTrue();
    assertThat(p.isShortValue()).isTrue();
    assertThat(p.isIntValue()).isTrue();
    assertThat(p.isLongValue()).isTrue();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(0);
    assertThat(p.degree()).isEqualTo(0);
    assertThat(p.signum()).isEqualTo(0);
    assertThat(p.toString()).isEqualTo("0");

    p = Polynomial.of("1");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isTrue();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isTrue();
    assertThat(p.isShortValue()).isTrue();
    assertThat(p.isIntValue()).isTrue();
    assertThat(p.isLongValue()).isTrue();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isTrue();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(0);
    assertThat(p.signum()).isEqualTo(1);
    assertThat(p.toString()).isEqualTo("1");

    p = Polynomial.of("-1");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isTrue();
    assertThat(p.isConstant()).isTrue();
    assertThat(p.isShortValue()).isTrue();
    assertThat(p.isIntValue()).isTrue();
    assertThat(p.isLongValue()).isTrue();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(0);
    assertThat(p.signum()).isEqualTo(-1);

    p = Polynomial.of("x");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isShortValue()).isFalse();
    assertThat(p.isIntValue()).isFalse();
    assertThat(p.isLongValue()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isTrue();
    assertThat(p.isVariable()).isTrue();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(1);
    assertThat(p.signum()).isEqualTo(1);
    assertThat(p.toString()).isEqualTo("x");

    p = Polynomial.of("-x");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isShortValue()).isFalse();
    assertThat(p.isIntValue()).isFalse();
    assertThat(p.isLongValue()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(1);
    assertThat(p.signum()).isEqualTo(-1);

    p = Polynomial.of("2*x");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isShortValue()).isFalse();
    assertThat(p.isIntValue()).isFalse();
    assertThat(p.isLongValue()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isFalse();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(1);
    assertThat(p.signum()).isEqualTo(1);

    p = Polynomial.of("x^2");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isShortValue()).isFalse();
    assertThat(p.isIntValue()).isFalse();
    assertThat(p.isLongValue()).isFalse();
    assertThat(p.isMonomial()).isTrue();
    assertThat(p.isMonic()).isTrue();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(1);
    assertThat(p.degree()).isEqualTo(2);
    assertThat(p.signum()).isEqualTo(1);

    p = Polynomial.of("(1+x+y)^2");
    assertThat(p.isZero()).isFalse();
    assertThat(p.isOne()).isFalse();
    assertThat(p.isMinusOne()).isFalse();
    assertThat(p.isConstant()).isFalse();
    assertThat(p.isShortValue()).isFalse();
    assertThat(p.isIntValue()).isFalse();
    assertThat(p.isLongValue()).isFalse();
    assertThat(p.isMonomial()).isFalse();
    assertThat(p.isMonic()).isTrue();
    assertThat(p.isVariable()).isFalse();
    assertThat(p.size()).isEqualTo(6);
    assertThat(p.degree()).isEqualTo(2);
    assertThat(p.signum()).isEqualTo(1);
  }

  @Test
  public void asShortValue() {
    assertThat(Polynomial.of("0").asShortValue()).isEqualTo(0);
    assertThat(Polynomial.of("1").asShortValue()).isEqualTo(1);
    assertThat(Polynomial.of("-2^15").asShortValue()).isEqualTo(-32768);
    assertThat(Polynomial.of("2^15-1").asShortValue()).isEqualTo(32767);

    assertThrows(IllegalStateException.class, () -> Polynomial.of("x").asShortValue());
    assertThrows(IllegalStateException.class, () -> Polynomial.of("-2^15-1").asShortValue());
    assertThrows(IllegalStateException.class, () -> Polynomial.of("2^15").asShortValue());
  }

  @Test
  public void asIntValue() {
    assertThat(Polynomial.of("0").asIntValue()).isEqualTo(0);
    assertThat(Polynomial.of("1").asIntValue()).isEqualTo(1);
    assertThat(Polynomial.of("-2^31").asIntValue()).isEqualTo(-2147483648);
    assertThat(Polynomial.of("2^31-1").asIntValue()).isEqualTo(2147483647);

    assertThrows(IllegalStateException.class, () -> Polynomial.of("x").asIntValue());
    assertThrows(IllegalStateException.class, () -> Polynomial.of("-2^31-1").asIntValue());
    assertThrows(IllegalStateException.class, () -> Polynomial.of("2^31").asIntValue());
  }

  @Test
  public void asLongValue() {
    assertThat(Polynomial.of("0").asLongValue()).isEqualTo(0);
    assertThat(Polynomial.of("1").asLongValue()).isEqualTo(1);
    assertThat(Polynomial.of("-2^63").asLongValue()).isEqualTo(-9223372036854775808L);
    assertThat(Polynomial.of("2^63-1").asLongValue()).isEqualTo(9223372036854775807L);

    assertThrows(IllegalStateException.class, () -> Polynomial.of("x").asLongValue());
    assertThrows(IllegalStateException.class, () -> Polynomial.of("-2^63-1").asLongValue());
    assertThrows(IllegalStateException.class, () -> Polynomial.of("2^63").asLongValue());
  }

  @Test
  public void asVariable() {
    Polynomial x = Polynomial.of("x");
    Polynomial y = Polynomial.of("y");

    Polynomial x2 = x.add(y).subtract(y);
    Polynomial y2 = y.add(x).subtract(x);

    assertThat(x.asVariable()).isEqualTo(Variable.of("x"));
    assertThat(x2.asVariable()).isEqualTo(Variable.of("x"));
    assertThat(y2.asVariable()).isEqualTo(Variable.of("y"));

    assertThrows(IllegalStateException.class, () -> Polynomial.of("0").asVariable());
    assertThrows(IllegalStateException.class, () -> Polynomial.of("x*y").asVariable());
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
    assertThat(Polynomial.of("1").getMinimalVariables()).isEqualTo(VariableSet.of());

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
    assertThat(p.coefficientOf(Variable.of("a"), 0)).isEqualTo(p);
    assertThat(p.coefficientOf(Variable.of("a"), 1)).isEqualTo(Polynomial.of("0"));

    assertThat(p.coefficientOf(Variable.of("a", "y", "b"), new int[] {0, 2, 0}))
        .isEqualTo(Polynomial.of("6 * (1 + x + z)^2"));
    assertThat(p.coefficientOf(Variable.of("z", "y", "x"), new int[] {0, 0, 1}))
        .isEqualTo(Polynomial.of("4"));
    assertThat(p.coefficientOf(Variable.of("z", "y", "x"), new int[] {1, 1, 2}))
        .isEqualTo(Polynomial.of("12"));
    assertThat(p.coefficientOf(Variable.of("y", "z"), new int[] {1, 2}))
        .isEqualTo(Polynomial.of("12 * (1 + x)"));
    assertThat(p.coefficientOf(Variable.of("a", "x"), new int[] {1, 2}))
        .isEqualTo(Polynomial.of("0"));
    assertThat(p.coefficientOf(Variable.of("a", "b", "c"), new int[] {1, 1, 1}))
        .isEqualTo(Polynomial.of("0"));
    assertThat(p.coefficientOf(Variable.of("a", "b", "c"), new int[] {0, 0, 0})).isEqualTo(p);
    assertThat(p.coefficientOf(Variable.of(), new int[] {})).isEqualTo(p);

    assertThrows(
        IllegalArgumentException.class,
        () -> p.coefficientOf(Variable.of("a", "b"), new int[] {1, 2, 3}));
  }

  @Test
  public void translate() {
    {
      Polynomial p = Polynomial.of("a+b");
      VariableSet vs = VariableSet.of("a", "b");
      Polynomial q = p.translate(vs);
      assertThat(p).isEqualTo(q);
      assertThat(q.getVariables() == vs).isTrue();
    }
    {
      Polynomial p = Polynomial.of("a+b");
      VariableSet vs = VariableSet.of("a", "b", "c");
      Polynomial q = p.translate(vs);
      assertThat(p).isEqualTo(q);
      assertThat(q.getVariables() == vs).isTrue();
    }
    {
      Polynomial p = Polynomial.of("b+e+(a+c+d)-(a+c+d)");
      VariableSet vs = VariableSet.of("a", "b", "c", "e");
      Polynomial q = p.translate(vs);
      assertThat(p).isEqualTo(q);
      assertThat(q.getVariables() == vs).isTrue();
    }
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Polynomial p = Polynomial.of("b+e+(a+c+d)-(a+c+d)");
          Polynomial q = p.translate(VariableSet.of("a", "b", "c", "d"));
        });
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
  public void divide() {
    assertThat(Polynomial.of("1+x").divide(Polynomial.of("1-y")))
        .isEqualTo(RationalFunction.of("(1+x)/(1-y)"));
  }

  @Test
  public void divideExact() {
    Polynomial a = Polynomial.of("1+x");
    Polynomial b = Polynomial.of("1-y");
    Polynomial ab = a.multiply(b);

    assertThat(a.divideExact(a)).isEqualTo(Polynomial.ONE);
    assertThat(ab.divideExact(b)).isEqualTo(a);

    assertThrows(ArithmeticException.class, () -> a.divideExact(b));
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
  public void sumOf() {
    Polynomial p1 = Polynomial.of("x + y");
    Polynomial p2 = Polynomial.of("y + z");
    Polynomial p3 = Polynomial.of("z + t");
    Polynomial p4 = Polynomial.of("1 + y + z");

    assertThat(Polynomial.sumOf()).isEqualTo(Polynomial.of("0"));
    assertThat(Polynomial.sumOf(p1)).isEqualTo(Polynomial.of("x + y"));
    assertThat(Polynomial.sumOf(p1, p2)).isEqualTo(Polynomial.of("x + 2*y + z"));
    assertThat(Polynomial.sumOf(p1, p2, p3)).isEqualTo(Polynomial.of("x + 2*y + 2*z + t"));
    assertThat(Polynomial.sumOf(p1, p2, p3, p4)).isEqualTo(Polynomial.of("1 + x + 3*y + 3*z + t"));

    // same variable set

    assertThat(Polynomial.sumOf(p1, p1, p1, p1)).isEqualTo(Polynomial.of("4 * x + 4 * y"));

    // auxiliary methods

    {
      List<Polynomial> pp = new ArrayList<>();
      assertThat(Polynomial.sumOf(pp)).isEqualTo(Polynomial.of("0"));
      pp.add(p1);
      assertThat(Polynomial.sumOf(pp)).isEqualTo(Polynomial.of("x + y"));
      pp.add(p2);
      assertThat(Polynomial.sumOf(pp)).isEqualTo(Polynomial.of("x + 2*y + z"));
      pp.add(p3);
      assertThat(Polynomial.sumOf(pp)).isEqualTo(Polynomial.of("x + 2*y + 2*z + t"));
      pp.add(p4);
      assertThat(Polynomial.sumOf(pp)).isEqualTo(Polynomial.of("1 + x + 3*y + 3*z + t"));
    }

    {
      Polynomial[] pp = new Polynomial[] {p1, p2, p3, p4};
      assertThat(Polynomial.sumOf(Arrays.stream(pp)))
          .isEqualTo(Polynomial.of("1 + x + 3*y + 3*z + t"));
    }
  }

  @Test
  public void productOf() {
    Polynomial p1 = Polynomial.of("x + y");
    Polynomial p2 = Polynomial.of("y + z");
    Polynomial p3 = Polynomial.of("z + t");
    Polynomial p4 = Polynomial.of("1 + y + z");

    assertThat(Polynomial.productOf()).isEqualTo(Polynomial.of("1"));
    assertThat(Polynomial.productOf(p1)).isEqualTo(Polynomial.of("x + y"));
    assertThat(Polynomial.productOf(p1, p2)).isEqualTo(Polynomial.of("(x + y) * (y + z)"));
    assertThat(Polynomial.productOf(p1, p2, p3))
        .isEqualTo(Polynomial.of("(x + y) * (y + z) * (z + t)"));
    assertThat(Polynomial.productOf(p1, p2, p3, p4))
        .isEqualTo(Polynomial.of("(x + y) * (y + z) * (z + t) * (1 + y + z)"));

    // same variable set

    assertThat(Polynomial.productOf(p1, p1, p1, p1)).isEqualTo(Polynomial.of("(x + y)^4"));

    // auxiliary methods

    {
      List<Polynomial> pp = new ArrayList<>();
      assertThat(Polynomial.productOf(pp)).isEqualTo(Polynomial.of("1"));
      pp.add(p1);
      assertThat(Polynomial.productOf(pp)).isEqualTo(Polynomial.of("x + y"));
      pp.add(p2);
      assertThat(Polynomial.productOf(pp)).isEqualTo(Polynomial.of("(x + y) * (y + z)"));
      pp.add(p3);
      assertThat(Polynomial.productOf(pp)).isEqualTo(Polynomial.of("(x + y) * (y + z) * (z + t)"));
      pp.add(p4);
      assertThat(Polynomial.productOf(pp))
          .isEqualTo(Polynomial.of("(x + y) * (y + z) * (z + t) * (1 + y + z)"));
    }

    {
      Polynomial[] pp = new Polynomial[] {p1, p2, p3, p4};
      assertThat(Polynomial.productOf(Arrays.stream(pp)))
          .isEqualTo(Polynomial.of("(x + y) * (y + z) * (z + t) * (1 + y + z)"));
    }
  }

  @Test
  public void gcd() {
    Polynomial a = Polynomial.of("x + y");
    Polynomial b = Polynomial.of("y + z");
    Polynomial g = Polynomial.of("2 + x + y + z");
    Polynomial ag = a.multiply(g);
    Polynomial bg = b.multiply(g);

    {
      Polynomial gg = ag.gcd(ag);
      assertThat(gg).isEqualTo(ag);
    }

    {
      Polynomial gg = ag.gcd(bg);
      assertThat(gg).isEqualTo(g);
    }

    {
      Polynomial gg = ag.gcd(b);
      assertThat(gg).isEqualTo(Polynomial.ONE);
    }
  }

  @Test
  public void gcdOf() {
    Polynomial a = Polynomial.of("x + y");
    Polynomial b = Polynomial.of("y + z");
    Polynomial c = Polynomial.of("1 + x + w");
    Polynomial g = Polynomial.of("2 + x + y + z");
    Polynomial ag = a.multiply(g);
    Polynomial bg = b.multiply(g);
    Polynomial cg = c.multiply(g);

    assertThat(Polynomial.gcdOf()).isEqualTo(Polynomial.ZERO);
    assertThat(Polynomial.gcdOf(ag)).isEqualTo(ag);
    assertThat(Polynomial.gcdOf(ag, bg)).isEqualTo(g);
    assertThat(Polynomial.gcdOf(ag, bg, cg)).isEqualTo(g);

    assertThat(Polynomial.gcdOf(ag, ag)).isEqualTo(ag);
    assertThat(Polynomial.gcdOf(ag, ag, ag)).isEqualTo(ag);

    assertThat(Polynomial.gcdOf(a, b, c)).isEqualTo(Polynomial.ONE);

    // auxiliary methods

    {
      List<Polynomial> pp = new ArrayList<>();
      assertThat(Polynomial.gcdOf(pp)).isEqualTo(Polynomial.ZERO);
      pp.add(ag);
      assertThat(Polynomial.gcdOf(pp)).isEqualTo(ag);
      pp.add(bg);
      assertThat(Polynomial.gcdOf(pp)).isEqualTo(g);
      pp.add(cg);
      assertThat(Polynomial.gcdOf(pp)).isEqualTo(g);
    }

    {
      Polynomial[] pp = new Polynomial[] {ag, bg, cg};
      assertThat(Polynomial.gcdOf(Arrays.stream(pp))).isEqualTo(g);
    }
  }

  @Test
  public void lcm() {
    {
      String s1 = "(1 + x)^2 * (2 + y)             * (4 + w)^5";
      String s2 = "(1 + x)   * (2 + y)^3 * (3 + z)";
      String s3 = "(1 + x)^2 * (2 + y)^3 * (3 + z) * (4 + w)^5";

      assertThat(Polynomial.of(s1).lcm(Polynomial.of(s2))).isEqualTo(Polynomial.of(s3));
    }

    {
      Polynomial zero = Polynomial.of("0");
      Polynomial one = Polynomial.of("1");
      Polynomial p = Polynomial.of("1+x");

      assertThat(zero.lcm(zero)).isEqualTo(zero);

      assertThat(p.lcm(zero)).isEqualTo(zero);
      assertThat(zero.lcm(p)).isEqualTo(zero);

      assertThat(p.lcm(one)).isEqualTo(p);
      assertThat(one.lcm(p)).isEqualTo(p);

      assertThat(p.lcm(p)).isEqualTo(p);
    }
  }

  @Test
  public void lcmOf() {
    {
      Polynomial p1 = Polynomial.of("1+x");
      Polynomial p2 = Polynomial.of("1-x");
      Polynomial p3 = Polynomial.of("1-x^2");

      assertThrows(IllegalArgumentException.class, () -> Polynomial.lcmOf());
      assertThat(Polynomial.lcmOf(p1)).isEqualTo(p1);
      assertThat(Polynomial.lcmOf(p1, p2)).isEqualTo(p1.multiply(p2));
      assertThat(Polynomial.lcmOf(p1, p2, p3)).isEqualTo(p1.multiply(p2));

      assertThat(Polynomial.lcmOf(p1, p1)).isEqualTo(p1);
      assertThat(Polynomial.lcmOf(p1, p1, p1)).isEqualTo(p1);
    }

    // auxiliary methods

    {
      Polynomial p1 = Polynomial.of("(1+x)^2*(1+y)");
      Polynomial p2 = Polynomial.of("(1+x)*(1+y)^2");
      Polynomial p3 = Polynomial.of("(1+y)^2");
      Polynomial r = Polynomial.of("(1+x)^2*(1+y)^2");
      Polynomial[] pp = {p1, p2, p3};
      assertThrows(IllegalArgumentException.class, () -> Polynomial.lcmOf(new Polynomial[] {}));
      assertThat(Polynomial.lcmOf(new Polynomial[] {p1})).isEqualTo(p1);
      assertThat(Polynomial.lcmOf(new Polynomial[] {p1, p2})).isEqualTo(r);
      assertThat(Polynomial.lcmOf(new Polynomial[] {p1, p2, p3})).isEqualTo(r);
      assertThat(Polynomial.lcmOf(Arrays.asList(pp))).isEqualTo(r);
      assertThat(Polynomial.lcmOf(Arrays.stream(pp))).isEqualTo(r);
    }

    {
      Polynomial p1 = Polynomial.of("1");
      Polynomial p2 = Polynomial.of("1+x");
      Polynomial p3 = Polynomial.of("1+y");
      Polynomial r = p2.multiply(p3);
      assertThat(Polynomial.lcmOf(new Polynomial[] {p1, p2, p3})).isEqualTo(r);
    }
  }

  @Test
  public void factors() {
    checkNoFactorization("0");
    checkNoFactorization("12");
    checkNoFactorization("x");

    assertThat(Polynomial.of("-3*x^2").factors())
        .isEqualTo(Polynomial.of(new String[] {"-3", "x", "x"}));
    assertThat(Polynomial.of("-7*x^2*y").factors())
        .isEqualTo(Polynomial.of(new String[] {"-7", "y", "x", "x"}));
    assertThat(Polynomial.of("(x+y)").factors()).isEqualTo(Polynomial.of(new String[] {"x+y"}));
    assertThat(Polynomial.of("-(x+y)").factors()).isEqualTo(Polynomial.of("-1", "x+y"));
    assertThat(Polynomial.of("2*(x+y)").factors()).isEqualTo(Polynomial.of("2", "x+y"));
    assertThat(Polynomial.of("-2*(x+y)").factors()).isEqualTo(Polynomial.of("-2", "x+y"));
    assertThat(Polynomial.of("2*(x-y)").factors()).isEqualTo(Polynomial.of("2", "x-y"));
    assertThat(Polynomial.of("-2*(x-y)").factors()).isEqualTo(Polynomial.of("-2", "x-y"));

    assertThat(Polynomial.of("x^2-y^2").factors()).isEqualTo(Polynomial.of("x-y", "x+y"));
    assertThat(Polynomial.of("-3*x*z^2*(x^2-y^2)^3").factors())
        .isEqualTo(Polynomial.of("-3", "z", "z", "x", "x-y", "x-y", "x-y", "x+y", "x+y", "x+y"));
  }

  void checkNoFactorization(String s) {
    Polynomial p = new Polynomial(s);
    assertThat(p.factors()).isEqualTo(new Polynomial[] {p});
  }

  @Test
  public void substitute() {
    {
      String s1 = "(1+x+y+z)^6";
      String s2 = "x*y^2";
      String s3 = "-z+w";
      String s4 =
          "1+60*w+15*w^2-54*z+150*z*w-150*z^2+180*z^2*w-160*z^3+60*z^3*w-45*"
              + "z^4+6*z^5+z^6+6*y+60*y*w-30*y*z+120*y*z*w-60*y*z^2+60*y*z^2*w+30*"
              + "y*z^4+6*y*z^5+15*y^2+30*y^2*w+30*y^2*z+30*y^2*z*w+60*y^2*z^2+60*"
              + "y^2*z^3+15*y^2*z^4+20*y^3+6*y^3*w+54*y^3*z+60*y^3*z^2+20*y^3*z^3+"
              + "15*y^4+30*y^4*z+15*y^4*z^2+6*y^5+6*y^5*z+y^6+6*x+90*x*w-60*x*z+"
              + "180*x*z*w-120*x*z^2+90*x*z^2*w-30*x*z^3+30*x*z^4+6*x*z^5+30*x*y+"
              + "60*x*y*w+60*x*y*z+60*x*y*z*w+120*x*y*z^2+120*x*y*z^3+30*x*y*z^4+"
              + "15*x^2+60*x^2*w+60*x^2*z*w+30*x^2*z^2+60*x^2*z^3+15*x^2*z^4+60*"
              + "x^2*y+20*x^2*y*w+160*x^2*y*z+180*x^2*y*z^2+60*x^2*y*z^3+20*x^3+15"
              + "*x^3*w+45*x^3*z+60*x^3*z^2+20*x^3*z^3+60*x^3*y+120*x^3*y*z+60*x^3"
              + "*y*z^2+15*x^4+30*x^4*z+15*x^4*z^2+30*x^4*y+30*x^4*y*z+6*x^5+6*x^5"
              + "*z+6*x^5*y+x^6";
      assertThat(Polynomial.of(s1).substitute(Polynomial.of(s2), Polynomial.of(s3)))
          .isEqualTo(Polynomial.of(s4));
    }
    {
      String s1 = "(1+x+y+z)^6";
      String s2 = "a";
      String s3 = "1";
      String s4 = s1;
      assertThat(Polynomial.of(s1).substitute(Polynomial.of(s2), Polynomial.of(s3)))
          .isEqualTo(Polynomial.of(s4));
    }
    {
      Polynomial p1 = Polynomial.of("(1+x+y+z)^6");
      Polynomial p3 = Polynomial.of("1");
      assertThrows(IllegalArgumentException.class, () -> p1.substitute(Polynomial.of("1"), p3));
      assertThrows(IllegalArgumentException.class, () -> p1.substitute(Polynomial.of("2*x"), p3));
      assertThrows(IllegalArgumentException.class, () -> p1.substitute(Polynomial.of("x+y+z"), p3));
    }
  }

  @Test
  public void evaluateAtInt() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)";
    Polynomial a = Polynomial.of(s);

    {
      Polynomial b = a.evaluate(Variable.of("a"), 42);
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluate(Variable.of("x"), 42);
      Polynomial c = Polynomial.of(s.replace("x", "42"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluate(Variable.of("a", "b"), ints(7, 9));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluate(Variable.of("a", "x"), ints(7, 9));
      Polynomial c = Polynomial.of(s.replace("x", "9"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b =
          a.evaluate(
              Variable.of("v1", "v2", "x", "x1", "y", "y1", "z1"), ints(7, 9, 11, 13, 15, 17, 19));
      Polynomial c = Polynomial.of(s.replace("x", "11").replace("y", "15"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluate(Variable.of("x", "y", "z"), ints(7, 9, 11));
      Polynomial c = Polynomial.of(s.replace("x", "7").replace("y", "9").replace("z", "11"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(
        IllegalArgumentException.class, () -> a.evaluate(Variable.of("x", "y"), ints(1, 2, 3)));
  }

  @Test
  public void evaluateAtBigInt() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)";
    Polynomial a = Polynomial.of(s);

    {
      Polynomial b = a.evaluate(Variable.of("a"), BigInteger.valueOf(42));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluate(Variable.of("x"), BigInteger.valueOf(42));
      Polynomial c = Polynomial.of(s.replace("x", "42"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluate(Variable.of("a", "b"), bigInts(7, 9));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluate(Variable.of("a", "x"), bigInts(7, 9));
      Polynomial c = Polynomial.of(s.replace("x", "9"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b =
          a.evaluate(
              Variable.of("v1", "v2", "x", "x1", "y", "y1", "z1"),
              bigInts(7, 9, 11, 13, 15, 17, 19));
      Polynomial c = Polynomial.of(s.replace("x", "11").replace("y", "15"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluate(Variable.of("x", "y", "z"), bigInts(7, 9, 11));
      Polynomial c = Polynomial.of(s.replace("x", "7").replace("y", "9").replace("z", "11"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(
        IllegalArgumentException.class, () -> a.evaluate(Variable.of("x", "y"), bigInts(1, 2, 3)));
  }

  @Test
  public void evaluateAtZero() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)";
    Polynomial a = Polynomial.of(s);

    {
      Polynomial b = a.evaluateAtZero(Variable.of("a"));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtZero(Variable.of("x"));
      Polynomial c = Polynomial.of(s.replace("x", "0"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtZero(VariableSet.of("a", "b"));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtZero(VariableSet.of("a", "x"));
      Polynomial c = Polynomial.of(s.replace("x", "0"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtZero(VariableSet.of("v1", "v2", "x", "x1", "y", "y1", "z1"));
      Polynomial c = Polynomial.of(s.replace("x", "0").replace("y", "0"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtZero(VariableSet.of("x", "y", "z"));
      Polynomial c = Polynomial.of(s.replace("x", "0").replace("y", "0").replace("z", "0"));
      assertThat(b).isEqualTo(c);
    }
  }

  @Test
  public void evaluateAtOne() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)";
    Polynomial a = Polynomial.of(s);

    {
      Polynomial b = a.evaluateAtOne(Variable.of("a"));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtOne(Variable.of("x"));
      Polynomial c = Polynomial.of(s.replace("x", "1"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtOne(VariableSet.of("a", "b"));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtOne(VariableSet.of("a", "x"));
      Polynomial c = Polynomial.of(s.replace("x", "1"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtOne(VariableSet.of("v1", "v2", "x", "x1", "y", "y1", "z1"));
      Polynomial c = Polynomial.of(s.replace("x", "1").replace("y", "1"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.evaluateAtOne(VariableSet.of("x", "y", "z"));
      Polynomial c = Polynomial.of(s.replace("x", "1").replace("y", "1").replace("z", "1"));
      assertThat(b).isEqualTo(c);
    }
  }

  @Test
  public void shiftByInt() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)";
    Polynomial a = Polynomial.of(s);

    {
      Polynomial b = a.shift(Variable.of("a"), 42);
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.shift(Variable.of("x"), 42);
      Polynomial c = Polynomial.of(s.replace("x", "(x+42)"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.shift(Variable.of("a", "b"), ints(7, 9));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.shift(Variable.of("a", "x"), ints(7, 9));
      Polynomial c = Polynomial.of(s.replace("x", "(x+9)"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b =
          a.shift(
              Variable.of("v1", "v2", "x", "x1", "y", "y1", "z1"), ints(7, 9, 11, 13, 15, 17, 19));
      Polynomial c = Polynomial.of(s.replace("x", "(x+11)").replace("y", "(y+15)"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.shift(Variable.of("x", "y", "z"), ints(7, 9, 11));
      Polynomial c =
          Polynomial.of(s.replace("x", "(x+7)").replace("y", "(y+9)").replace("z", "(z+11)"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(
        IllegalArgumentException.class, () -> a.shift(Variable.of("x", "y"), ints(1, 2, 3)));
  }

  @Test
  public void shiftByBigInt() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)";
    Polynomial a = Polynomial.of(s);

    {
      Polynomial b = a.shift(Variable.of("a"), BigInteger.valueOf(42));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.shift(Variable.of("x"), BigInteger.valueOf(42));
      Polynomial c = Polynomial.of(s.replace("x", "(x+42)"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.shift(Variable.of("a", "b"), bigInts(7, 9));
      Polynomial c = Polynomial.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.shift(Variable.of("a", "x"), bigInts(7, 9));
      Polynomial c = Polynomial.of(s.replace("x", "(x+9)"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b =
          a.shift(
              Variable.of("v1", "v2", "x", "x1", "y", "y1", "z1"),
              bigInts(7, 9, 11, 13, 15, 17, 19));
      Polynomial c = Polynomial.of(s.replace("x", "(x+11)").replace("y", "(y+15)"));
      assertThat(b).isEqualTo(c);
    }

    {
      Polynomial b = a.shift(Variable.of("x", "y", "z"), bigInts(7, 9, 11));
      Polynomial c =
          Polynomial.of(s.replace("x", "(x+7)").replace("y", "(y+9)").replace("z", "(z+11)"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(
        IllegalArgumentException.class, () -> a.shift(Variable.of("x", "y"), bigInts(1, 2, 3)));
  }

  @Test
  public void derivative() {
    {
      Polynomial p1 = Polynomial.of("(1+x+y)^3");
      Polynomial p2 = Polynomial.of("0");
      assertThat(p1.derivative(Variable.of("z"))).isEqualTo(p2);
    }
    {
      Polynomial p1 = Polynomial.of("(1+x+y)^3");
      Polynomial p2 = Polynomial.of("3*(1+x+y)^2");
      assertThat(p1.derivative(Variable.of("x"))).isEqualTo(p2);
    }
    {
      Polynomial p1 = Polynomial.of("(1+x+y)^3");
      Polynomial p2 = Polynomial.of("6*(1+x+y)");
      assertThat(p1.derivative(Variable.of("x"), 2)).isEqualTo(p2);
    }
    {
      Polynomial p1 = Polynomial.of("(1+x+y)^3");
      Polynomial p2 = p1;
      assertThat(p1.derivative(Variable.of("x"), 0)).isEqualTo(p2);
    }
    {
      Polynomial p1 = Polynomial.of("(1+x+y)^3");
      assertThrows(IllegalArgumentException.class, () -> p1.derivative(Variable.of("x"), -1));
    }
  }
}
