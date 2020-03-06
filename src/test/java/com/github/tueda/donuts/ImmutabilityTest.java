package com.github.tueda.donuts;

import static com.github.tueda.donuts.TestUtils.ints;
import static com.google.common.truth.Truth.assertThat;

import cc.redberry.rings.bigint.BigInteger;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

public class ImmutabilityTest {
  @Test
  public void polynomial() {
    checkPolynomialUnaryOp(
        p -> Polynomial.createFromRaw(p.getVariables(), p.getRawPolynomial().increment()));
    checkPolynomialUnaryOp(Polynomial::negate);
    checkPolynomialBinaryOp(Polynomial::add);
    checkPolynomialBinaryOp(Polynomial::subtract);
    checkPolynomialBinaryOp(Polynomial::multiply);
    checkPolynomialBinaryOp(Polynomial::divideExact);
    checkPolynomialBinaryOp((p1, p2) -> p1.divide(p2).getNumerator());
    checkPolynomialUnaryOp(p -> p.pow(5));
    checkPolynomialUnaryOp(p -> p.pow(BigInteger.valueOf(5)));
    checkPolynomialBinaryOp((p1, p2) -> p1.gcd(p2));
    checkPolynomialBinaryOp((p1, p2) -> Polynomial.gcdOf(p1, p2));
    checkPolynomialMultaryOp((pp) -> Polynomial.gcdOf(pp));
    checkPolynomialBinaryOp((p1, p2) -> p1.lcm(p2));
    checkPolynomialBinaryOp((p1, p2) -> Polynomial.lcmOf(p1, p2));
    checkPolynomialMultaryOp((pp) -> Polynomial.lcmOf(pp));
    checkPolynomialUnaryOp(p -> p.factors()[0]);
    checkPolynomialUnaryOp(p -> p.substitute(Polynomial.of("x"), Polynomial.of("x^2+y")));
    checkPolynomialUnaryOp(p -> p.evaluate(Variable.of("x"), 42));
    checkPolynomialUnaryOp(p -> p.evaluate(Variable.of("x", "y"), ints(42, 81)));
    checkPolynomialUnaryOp(p -> p.evaluateAtZero(Variable.of("x")));
    checkPolynomialUnaryOp(p -> p.evaluateAtZero(VariableSet.of("x", "y")));
    checkPolynomialUnaryOp(p -> p.evaluateAtOne(Variable.of("x")));
    checkPolynomialUnaryOp(p -> p.evaluateAtOne(VariableSet.of("x", "y")));
    checkPolynomialUnaryOp(p -> p.shift(Variable.of("x"), 42));
    checkPolynomialUnaryOp(p -> p.shift(Variable.of("x", "y"), ints(42, 81)));
    checkPolynomialUnaryOp(p -> p.derivative(Variable.of("x")));
    checkPolynomialUnaryOp(p -> p.derivative(Variable.of("x"), 2));
  }

  void checkPolynomialUnaryOp(UnaryOperator<Polynomial> operator) {
    checkPolynomialUnaryOp(operator, "(1+a-b)");
    checkPolynomialUnaryOp(operator, "(1+a-b)*(2-x)");
    checkPolynomialUnaryOp(operator, "(1+a-b)^2*(2+a-x+y)^3");
  }

  void checkPolynomialUnaryOp(UnaryOperator<Polynomial> operator, String s) {
    {
      Polynomial a = new Polynomial(s);
      Polynomial b = new Polynomial(s);
      operator.apply(a);
      assertThat(a).isEqualTo(b);
    }
    {
      Polynomial a = new Polynomial(s);
      Polynomial b = new Polynomial(s);
      VariableSet v = VariableSet.unionOf(a, b);
      a = a.translate(v);
      b = b.translate(v);
      // At this point, a and b share the same variable set.
      operator.apply(a);
      assertThat(a).isEqualTo(b);
    }
  }

  void checkPolynomialBinaryOp(BinaryOperator<Polynomial> operator) {
    checkPolynomialBinaryOp(operator, "6*(1+a-b)*(2-x)*(1+x+y)", "2*(1+a-b)*(2-x)+z-z");
  }

  void checkPolynomialBinaryOp(BinaryOperator<Polynomial> operator, String s1, String s2) {
    {
      Polynomial a1 = new Polynomial(s1);
      Polynomial a2 = new Polynomial(s2);
      Polynomial b1 = new Polynomial(s1);
      Polynomial b2 = new Polynomial(s2);
      operator.apply(a1, a2);
      assertThat(a1).isEqualTo(b1);
      assertThat(a2).isEqualTo(b2);
    }
    {
      Polynomial a1 = new Polynomial(s1);
      Polynomial a2 = new Polynomial(s2);
      Polynomial b1 = new Polynomial(s1);
      Polynomial b2 = new Polynomial(s2);
      VariableSet v = VariableSet.unionOf(a1, a2, b1, b2);
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

  void checkPolynomialMultaryOp(Function<Polynomial[], Polynomial> operator) {
    {
      String s1 = "90*(1+a)";
      String s2 = "-6*(1+a)*(2+b)";
      String s3 = "-9*(1+a)*(2+b)*(3+d)";
      String s4 = "12*(1+a)^2*(2+b)*(4+d)";
      String s5 = "24*(1+a)*(2+c)*(5+e)";
      String[] ss = {s1, s2, s3, s4, s5};
      checkPolynomialMultaryOp(operator, ss);
    }
    {
      String s1 = "1";
      String s2 = "x";
      String s3 = "y";
      String s4 = "1";
      String s5 = "2";
      String[] ss = {s1, s2, s3, s4, s5};
      checkPolynomialMultaryOp(operator, ss);
    }
  }

  void checkPolynomialMultaryOp(Function<Polynomial[], Polynomial> operator, String[] ss) {
    {
      Polynomial[] a = Arrays.stream(ss).map(Polynomial::new).toArray(Polynomial[]::new);
      Polynomial[] b = Arrays.stream(ss).map(Polynomial::new).toArray(Polynomial[]::new);
      operator.apply(a);
      for (int i = 0; i < b.length; i++) {
        assertThat(a[i]).isEqualTo(b[i]);
      }
    }
    {
      Polynomial[] a = Arrays.stream(ss).map(Polynomial::new).toArray(Polynomial[]::new);
      Polynomial[] b = Arrays.stream(ss).map(Polynomial::new).toArray(Polynomial[]::new);
      VariableSet va = VariableSet.unionOf(a);
      VariableSet vb = VariableSet.unionOf(b);
      VariableSet v = va.union(vb);
      for (int i = 0; i < b.length; i++) {
        a[i] = a[i].translate(v);
        b[i] = b[i].translate(v);
      }
      // At this point, all the polynomials share the same variable set.
      operator.apply(a);
      for (int i = 0; i < b.length; i++) {
        assertThat(a[i]).isEqualTo(b[i]);
      }
    }
  }

  @Test
  public void rationalFunction() {
    checkRationalFunctionUnaryOp(
        r -> {
          r.getRawRational().numerator().increment();
          r.getRawRational().denominator().decrement();
          return new RationalFunction();
        });
    checkRationalFunctionUnaryOp(RationalFunction::negate);
    checkRationalFunctionUnaryOp(RationalFunction::reciprocal);
    checkRationalFunctionBinaryOp(RationalFunction::add);
    checkRationalFunctionBinaryOp(RationalFunction::subtract);
    checkRationalFunctionBinaryOp(RationalFunction::multiply);
    checkRationalFunctionBinaryOp(RationalFunction::divide);
    checkRationalFunctionUnaryOp(r -> r.pow(5));
    checkRationalFunctionUnaryOp(r -> r.pow(BigInteger.valueOf(5)));
    checkRationalFunctionUnaryOp(
        r -> r.substitute(Polynomial.of("x"), RationalFunction.of("x^2+y/(1+z)")));
    checkRationalFunctionUnaryOp(r -> r.evaluate(Variable.of("x"), 42));
    checkRationalFunctionUnaryOp(r -> r.evaluate(Variable.of("x", "y"), ints(42, 81)));
    checkRationalFunctionUnaryOp(r -> r.evaluateAtZero(Variable.of("x")));
    checkRationalFunctionUnaryOp(r -> r.evaluateAtZero(VariableSet.of("x", "y")));
    checkRationalFunctionUnaryOp(r -> r.evaluateAtOne(Variable.of("x")));
    checkRationalFunctionUnaryOp(r -> r.evaluateAtOne(VariableSet.of("x", "y")));
    checkRationalFunctionUnaryOp(r -> r.shift(Variable.of("x"), 42));
    checkRationalFunctionUnaryOp(r -> r.shift(Variable.of("x", "y"), ints(42, 81)));
    checkRationalFunctionUnaryOp(r -> r.derivative(Variable.of("x")));
    checkRationalFunctionUnaryOp(r -> r.derivative(Variable.of("x"), 2));
  }

  void checkRationalFunctionUnaryOp(UnaryOperator<RationalFunction> operator) {
    checkRationalFunctionUnaryOp(operator, "(1+a+b)/(1+c+d)");
    checkRationalFunctionUnaryOp(operator, "(1+a+b+x)/(1+c+d)");
    checkRationalFunctionUnaryOp(operator, "(1+a+b)/(1+c+d-x)");
    checkRationalFunctionUnaryOp(operator, "(1+a+b+2*x)/(1+c+d+3*x)");
    checkRationalFunctionUnaryOp(operator, "(1+a+b^2+x+x^2+x^3)/(1+c+d^2+3*x+5*x^5)");
  }

  void checkRationalFunctionUnaryOp(UnaryOperator<RationalFunction> operator, String s) {
    {
      RationalFunction a = new RationalFunction(s);
      RationalFunction b = new RationalFunction(s);
      operator.apply(a);
      assertThat(a).isEqualTo(b);
    }
    {
      RationalFunction a = new RationalFunction(s);
      RationalFunction b = new RationalFunction(s);
      VariableSet v = VariableSet.unionOf(a, b);
      a = a.translate(v);
      b = b.translate(v);
      // At this point, a and b share the same variable set.
      operator.apply(a);
      assertThat(a).isEqualTo(b);
    }
  }

  void checkRationalFunctionBinaryOp(BinaryOperator<RationalFunction> operator) {
    checkRationalFunctionBinaryOp(operator, "6*(1+a-b)*(2-x)*(1+x+y)", "2*(1+a-b)*(2-x)+z-z");
  }

  void checkRationalFunctionBinaryOp(
      BinaryOperator<RationalFunction> operator, String s1, String s2) {
    {
      RationalFunction a1 = new RationalFunction(s1);
      RationalFunction a2 = new RationalFunction(s2);
      RationalFunction b1 = new RationalFunction(s1);
      RationalFunction b2 = new RationalFunction(s2);
      operator.apply(a1, a2);
      assertThat(a1).isEqualTo(b1);
      assertThat(a2).isEqualTo(b2);
    }
    {
      RationalFunction a1 = new RationalFunction(s1);
      RationalFunction a2 = new RationalFunction(s2);
      RationalFunction b1 = new RationalFunction(s1);
      RationalFunction b2 = new RationalFunction(s2);
      VariableSet v = VariableSet.unionOf(a1, a2, b1, b2);
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
}
