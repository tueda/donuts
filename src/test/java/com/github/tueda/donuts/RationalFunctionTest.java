package com.github.tueda.donuts;

import static com.github.tueda.donuts.TestUtils.bigInts;
import static com.github.tueda.donuts.TestUtils.ints;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cc.redberry.rings.bigint.BigInteger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    assertThrows(ArithmeticException.class, () -> new RationalFunction(1, 0));

    assertThrows(
        ArithmeticException.class,
        () -> new RationalFunction(new BigInteger("1"), new BigInteger("0")));

    assertThrows(
        ArithmeticException.class,
        () -> new RationalFunction(Polynomial.of("1"), Polynomial.of("0")));

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
  public void constants() {
    assertThat(RationalFunction.ZERO).isEqualTo(new RationalFunction(0));
    assertThat(RationalFunction.ONE).isEqualTo(new RationalFunction(1));
  }

  @Test
  public void serialization() throws IOException, ClassNotFoundException {
    // NOTE: this test is imperfect in the sense that the serialization and deserialization are
    // done in the same process.
    RationalFunction a = RationalFunction.of("(1+x+y)^2/(1-x-y)");

    byte[] data;

    {
      ByteArrayOutputStream bstream = new ByteArrayOutputStream();
      ObjectOutputStream ostream = new ObjectOutputStream(bstream);
      ostream.writeObject(a);
      ostream.close();
      data = bstream.toByteArray();
    }

    RationalFunction b;

    {
      ByteArrayInputStream bstream = new ByteArrayInputStream(data);
      ObjectInputStream ostream = new ObjectInputStream(bstream);
      b = (RationalFunction) ostream.readObject();
      ostream.close();
    }

    assertThat(a).isEqualTo(b);
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
  public void hashCodeTest() {
    RationalFunction r = RationalFunction.of("(1+x+y)/(1+x+y)+z");
    RationalFunction s = RationalFunction.of("(1+z)*(1-y)/(1-y)");
    assertThat(r).isEqualTo(s);
    assertThat(r.hashCode()).isEqualTo(s.hashCode());
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

    r = RationalFunction.of("-1/(1-x)");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isFalse();
    assertThat(r.isInteger()).isFalse();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isFalse();

    r = RationalFunction.of("(1+x)/(1-x)");
    assertThat(r.isZero()).isFalse();
    assertThat(r.isOne()).isFalse();
    assertThat(r.isMinusOne()).isFalse();
    assertThat(r.isConstant()).isFalse();
    assertThat(r.isInteger()).isFalse();
    assertThat(r.isVariable()).isFalse();
    assertThat(r.isPolynomial()).isFalse();

    // Note: denominators have always positive signum.
    for (int i1 = -1; i1 <= 1; i1++) {
      for (int i2 = -1; i2 <= 1; i2++) {
        for (int i3 = -1; i3 <= 1; i3++) {
          for (int i4 = -1; i4 <= 1; i4++) {
            for (int i5 = -1; i5 <= 1; i5++) {
              for (int i6 = -1; i6 <= 1; i6++) {
                Polynomial p = new Polynomial(String.format("(%d)+(%d)*x+(%d)*y", i1, i2, i3));
                Polynomial q = new Polynomial(String.format("(%d)+(%d)*x+(%d)*y", i4, i5, i6));
                if (!q.isZero()) {
                  assertWithMessage(String.format("(%s)/(%s)", p, q))
                      .that(new RationalFunction(p, q).getDenominator().signum())
                      .isEqualTo(1);
                }
              }
            }
          }
        }
      }
    }
  }

  @Test
  public void translate() {
    {
      RationalFunction r = RationalFunction.of("a/b");
      VariableSet vs = VariableSet.of("a", "b");
      RationalFunction s = r.translate(vs);
      assertThat(r).isEqualTo(s);
      assertThat(s.getVariables() == vs).isTrue();
    }
    {
      RationalFunction r = RationalFunction.of("a+c/d");
      VariableSet vs = VariableSet.of("a", "b", "c", "d");
      RationalFunction s = r.translate(vs);
      assertThat(r).isEqualTo(s);
      assertThat(s.getVariables() == vs).isTrue();
    }
    {
      RationalFunction r = RationalFunction.of("(a+b+c)/(a+b+c)*d+f");
      VariableSet vs = VariableSet.of("d", "e", "f");
      RationalFunction s = r.translate(vs);
      assertThat(r).isEqualTo(s);
      assertThat(s.getVariables() == vs).isTrue();
    }
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          RationalFunction r = RationalFunction.of("(a+b)/(c+d)+e+g-g");
          RationalFunction s = r.translate(VariableSet.of("a", "b", "c", "d", "f", "g", "h"));
        });
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

  @Test
  void substitute() {
    {
      String s1 = "(1+x+y+z)^6/(1-x-y)^3";
      String s2 = "x*y^2";
      String s3 = "-z+w";
      String s4 =
          "(1+60*w+15*w^2-54*z+150*z*w-150*z^2+180*z^2*w-160*z^3+60*z^3*w-45*"
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
              + "*z+6*x^5*y+x^6)"
              + "/(1-3*w+3*z-3*y+3*y^2-y^3-3*x+6*x*y+3*x^2-3*x^2*y-x^3)";
      assertThat(RationalFunction.of(s1).substitute(Polynomial.of(s2), RationalFunction.of(s3)))
          .isEqualTo(RationalFunction.of(s4));
    }
    {
      String s1 = "(x+x^2)/(1+y)";
      String s2 = "x";
      String s3 = "-1";
      String s4 = "0";
      assertThat(RationalFunction.of(s1).substitute(Polynomial.of(s2), RationalFunction.of(s3)))
          .isEqualTo(RationalFunction.of(s4));
    }
    {
      String s1 = "(1+x+y+z)^6/(1+z)^3";
      String s2 = "x";
      String s3 = "-y";
      String s4 = "(1+z)^3";
      assertThat(RationalFunction.of(s1).substitute(Polynomial.of(s2), RationalFunction.of(s3)))
          .isEqualTo(RationalFunction.of(s4));
    }
    {
      String s1 = "(1+x+y+z)^6/(1-x-y)";
      String s2 = "a";
      String s3 = "1";
      String s4 = s1;
      assertThat(RationalFunction.of(s1).substitute(Polynomial.of(s2), RationalFunction.of(s3)))
          .isEqualTo(RationalFunction.of(s4));
    }
    {
      RationalFunction r1 = RationalFunction.of("(1+x+y+z)^6/(1-x-y)");
      RationalFunction r3 = RationalFunction.of("1");
      assertThrows(IllegalArgumentException.class, () -> r1.substitute(Polynomial.of("1"), r3));
      assertThrows(IllegalArgumentException.class, () -> r1.substitute(Polynomial.of("2*x"), r3));
      assertThrows(IllegalArgumentException.class, () -> r1.substitute(Polynomial.of("x+y+z"), r3));
    }
  }

  @Test
  public void evaluateAtInt() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)/(1+x^2)/(1+y^2)/(1+z)/(1+w)";
    RationalFunction a = RationalFunction.of(s);

    {
      RationalFunction b = a.evaluate(Variable.of("a"), 42);
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluate(Variable.of("x"), 42);
      RationalFunction c = RationalFunction.of(s.replace("x", "42"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluate(Variable.of("a", "b"), ints(7, 9));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluate(Variable.of("a", "x"), ints(7, 9));
      RationalFunction c = RationalFunction.of(s.replace("x", "9"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b =
          a.evaluate(
              Variable.of("v1", "v2", "x", "x1", "y", "y1", "z1"), ints(7, 9, 11, 13, 15, 17, 19));
      RationalFunction c = RationalFunction.of(s.replace("x", "11").replace("y", "15"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluate(Variable.of("x", "y", "z"), ints(7, 9, 11));
      RationalFunction c =
          RationalFunction.of(s.replace("x", "7").replace("y", "9").replace("z", "11"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(
        IllegalArgumentException.class, () -> a.evaluate(Variable.of("x", "y"), ints(1, 2, 3)));

    assertThrows(ArithmeticException.class, () -> a.evaluate(Variable.of("a", "z"), ints(-1, -1)));
  }

  @Test
  public void evaluateAtBigInt() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)/(1+x^2)/(1+y^2)/(1+z)/(1+w)";
    RationalFunction a = RationalFunction.of(s);

    {
      RationalFunction b = a.evaluate(Variable.of("a"), BigInteger.valueOf(42));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluate(Variable.of("x"), BigInteger.valueOf(42));
      RationalFunction c = RationalFunction.of(s.replace("x", "42"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluate(Variable.of("a", "b"), bigInts(7, 9));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluate(Variable.of("a", "x"), bigInts(7, 9));
      RationalFunction c = RationalFunction.of(s.replace("x", "9"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b =
          a.evaluate(
              Variable.of("v1", "v2", "x", "x1", "y", "y1", "z1"),
              bigInts(7, 9, 11, 13, 15, 17, 19));
      RationalFunction c = RationalFunction.of(s.replace("x", "11").replace("y", "15"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluate(Variable.of("x", "y", "z"), bigInts(7, 9, 11));
      RationalFunction c =
          RationalFunction.of(s.replace("x", "7").replace("y", "9").replace("z", "11"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(
        IllegalArgumentException.class, () -> a.evaluate(Variable.of("x", "y"), bigInts(1, 2, 3)));

    assertThrows(
        ArithmeticException.class, () -> a.evaluate(Variable.of("a", "z"), bigInts(-1, -1)));
  }

  @Test
  public void evaluateAtZero() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)/(1+x^2)/(1+y^2)/(1+z)/(z+w)";
    RationalFunction a = RationalFunction.of(s);

    {
      RationalFunction b = a.evaluateAtZero(Variable.of("a"));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtZero(Variable.of("x"));
      RationalFunction c = RationalFunction.of(s.replace("x", "0"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtZero(VariableSet.of("a", "b"));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtZero(VariableSet.of("a", "x"));
      RationalFunction c = RationalFunction.of(s.replace("x", "0"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtZero(VariableSet.of("v1", "v2", "x", "x1", "y", "y1", "z1"));
      RationalFunction c = RationalFunction.of(s.replace("x", "0").replace("y", "0"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtZero(VariableSet.of("x", "y", "z"));
      RationalFunction c =
          RationalFunction.of(s.replace("x", "0").replace("y", "0").replace("z", "0"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(ArithmeticException.class, () -> a.evaluateAtZero(VariableSet.of("z", "w")));
  }

  @Test
  public void evaluateAtOne() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)/(1+x^2)/(1+y^2)/(1+z)/(z+w-2)";
    RationalFunction a = RationalFunction.of(s);

    {
      RationalFunction b = a.evaluateAtOne(Variable.of("a"));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtOne(Variable.of("x"));
      RationalFunction c = RationalFunction.of(s.replace("x", "1"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtOne(VariableSet.of("a", "b"));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtOne(VariableSet.of("a", "x"));
      RationalFunction c = RationalFunction.of(s.replace("x", "1"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtOne(VariableSet.of("v1", "v2", "x", "x1", "y", "y1", "z1"));
      RationalFunction c = RationalFunction.of(s.replace("x", "1").replace("y", "1"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.evaluateAtOne(VariableSet.of("x", "y", "z"));
      RationalFunction c =
          RationalFunction.of(s.replace("x", "1").replace("y", "1").replace("z", "1"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(ArithmeticException.class, () -> a.evaluateAtOne(VariableSet.of("z", "w")));
  }

  @Test
  public void shiftByInt() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)/(1+x^2)/(1+y^2)/(1+z)";
    RationalFunction a = RationalFunction.of(s);

    {
      RationalFunction b = a.shift(Variable.of("a"), 42);
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.shift(Variable.of("x"), 42);
      RationalFunction c = RationalFunction.of(s.replace("x", "(x+42)"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.shift(Variable.of("a", "b"), ints(7, 9));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.shift(Variable.of("a", "x"), ints(7, 9));
      RationalFunction c = RationalFunction.of(s.replace("x", "(x+9)"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b =
          a.shift(
              Variable.of("v1", "v2", "x", "x1", "y", "y1", "z1"), ints(7, 9, 11, 13, 15, 17, 19));
      RationalFunction c = RationalFunction.of(s.replace("x", "(x+11)").replace("y", "(y+15)"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.shift(Variable.of("x", "y", "z"), ints(7, 9, 11));
      RationalFunction c =
          RationalFunction.of(s.replace("x", "(x+7)").replace("y", "(y+9)").replace("z", "(z+11)"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(
        IllegalArgumentException.class, () -> a.shift(Variable.of("x", "y"), ints(1, 2, 3)));
  }

  @Test
  public void shiftByBigInt() {
    String s = "(1+x)^4*(2+y)^3*(3+z)^2*(4+w)/(1+x^2)/(1+y^2)/(1+z)";
    RationalFunction a = RationalFunction.of(s);

    {
      RationalFunction b = a.shift(Variable.of("a"), BigInteger.valueOf(42));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.shift(Variable.of("x"), BigInteger.valueOf(42));
      RationalFunction c = RationalFunction.of(s.replace("x", "(x+42)"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.shift(Variable.of("a", "b"), bigInts(7, 9));
      RationalFunction c = RationalFunction.of(s);
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.shift(Variable.of("a", "x"), bigInts(7, 9));
      RationalFunction c = RationalFunction.of(s.replace("x", "(x+9)"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b =
          a.shift(
              Variable.of("v1", "v2", "x", "x1", "y", "y1", "z1"),
              bigInts(7, 9, 11, 13, 15, 17, 19));
      RationalFunction c = RationalFunction.of(s.replace("x", "(x+11)").replace("y", "(y+15)"));
      assertThat(b).isEqualTo(c);
    }

    {
      RationalFunction b = a.shift(Variable.of("x", "y", "z"), bigInts(7, 9, 11));
      RationalFunction c =
          RationalFunction.of(s.replace("x", "(x+7)").replace("y", "(y+9)").replace("z", "(z+11)"));
      assertThat(b).isEqualTo(c);
    }

    assertThrows(
        IllegalArgumentException.class, () -> a.shift(Variable.of("x", "y"), bigInts(1, 2, 3)));
  }

  @Test
  public void derivative() {
    {
      RationalFunction r1 = RationalFunction.of("(1+x+y)^3/(1-3*x+y)^2");
      RationalFunction r2 = RationalFunction.of("0");
      assertThat(r1.derivative(Variable.of("z"))).isEqualTo(r2);
    }
    {
      RationalFunction r1 = RationalFunction.of("(1+x+y)^3/(1-3*x+y)^2");
      RationalFunction r2 = RationalFunction.of("-3*(1+x+y)^2*(x-3*(1+y))/(1-3*x+y)^3");
      assertThat(r1.derivative(Variable.of("x"))).isEqualTo(r2);
    }
    {
      RationalFunction r1 = RationalFunction.of("(1+x+y)^3/(1-3*a+y)^2");
      RationalFunction r2 = RationalFunction.of("3*(1+x+y)^2/(1-3*a+y)^2");
      assertThat(r1.derivative(Variable.of("x"))).isEqualTo(r2);
    }
    {
      RationalFunction r1 = RationalFunction.of("(1+a+y)^3/(1-3*x+y)^2");
      RationalFunction r2 = RationalFunction.of("6*(1+a+y)^3/(1-3*x+y)^3");
      assertThat(r1.derivative(Variable.of("x"))).isEqualTo(r2);
    }
    {
      RationalFunction r1 = RationalFunction.of("(1+a+y)^3/(1-3*b+y)^2+x-x");
      RationalFunction r2 = RationalFunction.of("0");
      assertThat(r1.derivative(Variable.of("x"))).isEqualTo(r2);
    }
    {
      RationalFunction r1 = RationalFunction.of("(1+x+y)^3/(1-3*x+y)^2");
      RationalFunction r2 = r1;
      assertThat(r1.derivative(Variable.of("x"), 0)).isEqualTo(r2);
    }
    {
      RationalFunction r1 = RationalFunction.of("(1+x+y)^3/(1-3*x+y)^2");
      RationalFunction r2 = RationalFunction.of("96*(1+y)^2*(1+x+y)/(1-3*x+y)^4");
      assertThat(r1.derivative(Variable.of("x"), 2)).isEqualTo(r2);
    }
    {
      RationalFunction r1 = RationalFunction.of("(1+x+y)^3/(1-3*x+y)^2");
      RationalFunction r2 = RationalFunction.of("(51840*(1+y)^2*(7+3*x+7*y))/(1-3*x+y)^7");
      assertThat(r1.derivative(Variable.of("x"), 5)).isEqualTo(r2);
    }
    {
      RationalFunction r1 = RationalFunction.of("(1+x+y)^3/(1-3*x+y)^2");
      assertThrows(IllegalArgumentException.class, () -> r1.derivative(Variable.of("x"), -1));
    }
  }
}
