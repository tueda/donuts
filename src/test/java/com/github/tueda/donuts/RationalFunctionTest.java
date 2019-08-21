package com.github.tueda.donuts;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cc.redberry.rings.bigint.BigInteger;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

public class RationalFunctionTest {
  @Test
  public void newInstance() {
    {
      RationalFunction r1 = new RationalFunction();
      RationalFunction r2 = new RationalFunction(0);
      assertThat(r1).isEqualTo(r2);
    }

    {
      RationalFunction r1 = new RationalFunction(1000);
      RationalFunction r2 = new RationalFunction(new BigInteger("1000"));
      assertThat(r1).isEqualTo(r2);
    }

    {
      RationalFunction r1 = new RationalFunction(Polynomial.of("x").add(Polynomial.of("1-x")));
      RationalFunction r2 = new RationalFunction(1);
      assertThat(r1).isEqualTo(r2);
    }

    {
      RationalFunction r1 = new RationalFunction(123, 456);
      RationalFunction r2 = new RationalFunction("123 / 456");
      assertThat(r1).isEqualTo(r2);
    }

    {
      RationalFunction r1 = new RationalFunction(new BigInteger("123"), new BigInteger("456"));
      RationalFunction r2 = new RationalFunction("123 / 456");
      assertThat(r1).isEqualTo(r2);
    }

    {
      Polynomial p = Polynomial.of("x").add(Polynomial.of("1-x"));
      Polynomial q = Polynomial.of("y").add(Polynomial.of("2-y"));
      RationalFunction r1 = new RationalFunction(p, q);
      RationalFunction r2 = new RationalFunction(1, 2);
      assertThat(r1).isEqualTo(r2);
    }

    {
      RationalFunction r1 = new RationalFunction("2/x");
      RationalFunction r2 = new RationalFunction(Polynomial.of("2"), Polynomial.of("x"));
      assertThat(r1).isEqualTo(r2);
    }

    {
      RationalFunction r1 = new RationalFunction("x + y + z");
      RationalFunction r2 = new RationalFunction("(- x^2 - x*y - x*z + x + y + z) / (1 - x)");
      assertThat(r1).isEqualTo(r2);
    }

    // Unfortunately it won't work.
    // java.lang.ArithmeticException (Negative exponent) at cc.redberry.rings.bigint.BigInteger.pow
    assertThrows(IllegalArgumentException.class, () -> new RationalFunction("2^(-2)"));

    // java.lang.RuntimeException (illegal operand) at cc.redberry.rings.io.Coder.mkOperand
    assertThrows(IllegalArgumentException.class, () -> new RationalFunction("1.2"));
    assertThrows(IllegalArgumentException.class, () -> new RationalFunction("x;"));

    // java.lang.IllegalArgumentException (Can't parse) at cc.redberry.rings.io.Coder.parse
    assertThrows(IllegalArgumentException.class, () -> new RationalFunction("(("));

    // java.lang.IllegalArgumentException (Exponents must be positive integers)
    // at cc.redberry.rings.io.Coder.popEvaluate
    assertThrows(IllegalArgumentException.class, () -> new RationalFunction("x^x"));

    // java.lang.IllegalArgumentException (Illegal character)
    // at cc.redberry.rings.io.Tokenizer.checkChar
    assertThrows(IllegalArgumentException.class, () -> new RationalFunction("="));

    // java.lang.IllegalArgumentException (spaces in variable name are forbidden)
    // at cc.redberry.rings.io.Tokenizer.nextToken
    assertThrows(IllegalArgumentException.class, () -> new RationalFunction("x x"));

    // java.util.NoSuchElementException at java.util.ArrayDeque.removeFirst
    assertThrows(IllegalArgumentException.class, () -> new RationalFunction(""));

    assertThrows(
        IllegalArgumentException.class,
        () -> new RationalFunction("3.1415926535897932384626433832795028841971693993751"));
  }

  @Test
  public void equals() {
    RationalFunction x = RationalFunction.of("x");
    RationalFunction y = RationalFunction.of("y");
    RationalFunction z = RationalFunction.of("z");

    RationalFunction zero = RationalFunction.of("0");

    RationalFunction p = x.add(y).add(z).divide(x.subtract(y).add(z));
    RationalFunction q = z.add(y).add(x).divide(x.subtract(y).add(z));

    assertThat(p.equals(p)).isTrue();

    assertThat(p).isEqualTo(p);
    assertThat(p).isEqualTo(q);

    // Unfortunately, the followings won't be equal.
    assertThat(RationalFunction.of("1")).isNotEqualTo(1);
    assertThat(RationalFunction.of("x")).isNotEqualTo(Variable.of("x"));
    assertThat(RationalFunction.of("1+x")).isNotEqualTo(Polynomial.of("1+x"));
  }

  @Test
  public void getMinimalVariables() {
    assertThat(RationalFunction.of("1").getMinimalVariables()).isEqualTo(VariableSet.of());

    RationalFunction r1 = RationalFunction.of("(a+b+c+d+e)/(1+b)");
    RationalFunction r2 = RationalFunction.of("(a+c+e)/(1+b)");
    RationalFunction r3 = r1.subtract(r2);
    assertThat(r3.getMinimalVariables()).isEqualTo(VariableSet.of("b", "d"));
  }

  @Test
  public void properties() {
    RationalFunction r;

    r = RationalFunction.of("0");
    assertThat(r.isZero()).isTrue();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isTrue();
    assertThat(r.isInteger()).isTrue();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isTrue();
    assertThat(r.toString()).isEqualTo("0");

    r = RationalFunction.of("1");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isTrue();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isTrue();
    assertThat(r.isInteger()).isTrue();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isTrue();
    assertThat(r.toString()).isEqualTo("1");

    r = RationalFunction.of("-1");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isTrue();
    assertThat(r.isConstant()).isTrue();
    assertThat(r.isInteger()).isTrue();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isTrue();
    assertThat(r.toString()).isEqualTo("-1");

    r = RationalFunction.of("x");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isFalse();
    assertThat(r.isInteger()).isFalse();
    assertThat(r.isVariable()).isTrue();
    assertThat(r.isPolynomial()).isTrue();
    assertThat(r.toString()).isEqualTo("x");

    r = RationalFunction.of("-x");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isFalse();
    assertThat(r.isInteger()).isFalse();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isTrue();

    r = RationalFunction.of("2*x");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isFalse();
    assertThat(r.isInteger()).isFalse();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isTrue();

    r = RationalFunction.of("x^2");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isFalse();
    assertThat(r.isInteger()).isFalse();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isTrue();

    r = RationalFunction.of("(1+x+y)^2");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isFalse();
    assertThat(r.isInteger()).isFalse();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isTrue();

    r = RationalFunction.of("(1+x)/(1-x)");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isFalse();
    assertThat(r.isInteger()).isFalse();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isFalse();
  }

  @Test
  public void immutability() {
    checkUnaryOperatorImmutability(RationalFunction::negate);
    checkUnaryOperatorImmutability(RationalFunction::reciprocal);
    checkBinaryOperatorImmutability(RationalFunction::add);
    checkBinaryOperatorImmutability(RationalFunction::subtract);
    checkBinaryOperatorImmutability(RationalFunction::multiply);
    checkBinaryOperatorImmutability(RationalFunction::divide);
    checkUnaryOperatorImmutability(p -> p.pow(5));
    checkUnaryOperatorImmutability(p -> p.pow(new BigInteger("5")));
  }

  void checkUnaryOperatorImmutability(UnaryOperator<RationalFunction> operator) {
    String s = "(1+a-b)/(2-x)";
    RationalFunction a = new RationalFunction(s);
    RationalFunction b = new RationalFunction(s);
    VariableSet v = VariableSet.union(a, b);
    a = a.translate(v);
    b = b.translate(v);
    // At this point, a and b share the same variable set.
    operator.apply(a);
    assertThat(a).isEqualTo(b);
  }

  void checkBinaryOperatorImmutability(BinaryOperator<RationalFunction> operator) {
    {
      String s1 = "6*(1+a-b)*(2-x)*(1+x+y)";
      String s2 = "2*(1+a-b)*(2-x)+z-z";
      RationalFunction a1 = new RationalFunction(s1);
      RationalFunction a2 = new RationalFunction(s2);
      RationalFunction b1 = new RationalFunction(s1);
      RationalFunction b2 = new RationalFunction(s2);
      operator.apply(a1, a2);
      assertThat(a1).isEqualTo(b1);
      assertThat(a2).isEqualTo(b2);
    }
    {
      String s1 = "6*(1+a-b)*(2-x)*(1+x+y)";
      String s2 = "2*(1+a-b)*(2-x)+z-z";
      RationalFunction a1 = new RationalFunction(s1);
      RationalFunction a2 = new RationalFunction(s2);
      RationalFunction b1 = new RationalFunction(s1);
      RationalFunction b2 = new RationalFunction(s2);
      VariableSet v = VariableSet.union(a1, a2, b1, b2);
      a1 = a1.translate(v);
      a2 = a2.translate(v);
      b1 = b1.translate(v);
      b2 = b2.translate(v);
      // At this point, a1, a2, b1 and b2 share the same variable set.
      operator.apply(a1, a2);
      assertThat(a1).isEqualTo(b1);
      assertThat(a2).isEqualTo(b2);
    }
  }

  @Test
  void negate() {
    RationalFunction r = RationalFunction.of("(1-x)/(1-y)");
    RationalFunction s = r.negate();
    RationalFunction t = s.negate();

    RationalFunction zero = RationalFunction.of("0");

    assertThat(r.add(s)).isEqualTo(zero);
    assertThat(r).isEqualTo(t);

    assertThat(zero.negate()).isEqualTo(zero);
  }

  @Test
  void reciprocal() {
    RationalFunction r = RationalFunction.of("(1-x)/(1-y)");
    RationalFunction s = r.reciprocal();
    RationalFunction t = s.reciprocal();

    RationalFunction one = RationalFunction.of("1");

    assertThat(r.multiply(s)).isEqualTo(one);
    assertThat(r).isEqualTo(t);

    assertThat(one.reciprocal()).isEqualTo(one);
  }

  @Test
  void add() {
    RationalFunction r = RationalFunction.of("(1-y)/(1+x)/(1+x-y+z)");
    RationalFunction s = RationalFunction.of("(x+z)/(1+x)/(1+x-y+z)");
    RationalFunction t = RationalFunction.of("1/(1+x)");

    assertThat(r.add(s)).isEqualTo(t);
  }

  @Test
  void sub() {
    RationalFunction r = RationalFunction.of("1/(1+x)/(1+x-y+z)");
    RationalFunction s = RationalFunction.of("-(x+z-y)/(1+x)/(1+x-y+z)");
    RationalFunction t = RationalFunction.of("1/(1+x)");

    assertThat(r.subtract(s)).isEqualTo(t);
  }

  @Test
  void multiply() {
    RationalFunction r = RationalFunction.of("1-1/(1+x)");
    RationalFunction s = RationalFunction.of("-y/(1-y)");
    RationalFunction t = RationalFunction.of("x*y/(1+x)/(y-1)");

    assertThat(r.multiply(s)).isEqualTo(t);
  }

  @Test
  void divide() {
    RationalFunction r = RationalFunction.of("1/(1+x+y)");
    RationalFunction s = RationalFunction.of("1/(1+y+z)");
    RationalFunction t = RationalFunction.of("(1+y+z)/(1+x+y)");

    assertThat(r.divide(s)).isEqualTo(t);
  }

  @Test
  void pow() {
    RationalFunction r1 = RationalFunction.of("-(1+x)/(1-y)");
    RationalFunction r4 = RationalFunction.of("(1+x)^4/(1-y)^4");
    RationalFunction r5 = RationalFunction.of("-(1+x)^5/(1-y)^5");
    RationalFunction r8 = r4.multiply(r4);
    RationalFunction r9 = r4.multiply(r5);

    RationalFunction one = RationalFunction.of("1");

    assertThat(r1.pow(4)).isEqualTo(r4);
    assertThat(r1.pow(5)).isEqualTo(r5);
    assertThat(r1.pow(new BigInteger("8"))).isEqualTo(r8);
    assertThat(r1.pow(new BigInteger("9"))).isEqualTo(r9);

    assertThat(r1.pow(-5).multiply(r5)).isEqualTo(one);
  }
}
