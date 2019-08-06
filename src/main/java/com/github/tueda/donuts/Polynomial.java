package com.github.tueda.donuts;

import cc.redberry.rings.Rings;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.poly.PolynomialMethods;
import cc.redberry.rings.poly.multivar.MonomialOrder;
import cc.redberry.rings.poly.multivar.MultivariateDivision;
import cc.redberry.rings.poly.multivar.MultivariatePolynomial;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.BinaryOperator;

/** An immutable multivariate polynomial with integer coefficients. */
public final class Polynomial implements Serializable {
  private static final long serialVersionUID = 1L;

  /** Zero polynomial. */
  /* default */ static final MultivariatePolynomial<BigInteger> RAW_ZERO =
      MultivariatePolynomial.zero(0, Rings.Z, MonomialOrder.DEFAULT);

  /** The set of variables. */
  private final VariableSet variables;

  /** The raw polynomial object. */
  private final MultivariatePolynomial<BigInteger> raw;

  /** Constructs a zero polynomial. */
  public Polynomial() {
    variables = VariableSet.EMPTY;
    raw = RAW_ZERO;
  }

  /** Constructs a polynomial from the given integer. */
  public Polynomial(final long value) {
    variables = VariableSet.EMPTY;
    raw = RAW_ZERO.createConstant(value);
  }

  /** Constructs a polynomial from the given integer. */
  public Polynomial(final BigInteger value) {
    variables = VariableSet.EMPTY;
    raw = RAW_ZERO.createConstant(value);
  }

  /**
   * Constructs a polynomial from the given string.
   *
   * @throws IllegalArgumentException when {@code string} does not represent a polynomial
   */
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public Polynomial(final String string) {
    final String[] names = Variable.guessVariableNames(string);
    variables = VariableSet.createVariableSetFromRawArray(names);
    try {
      raw = MultivariatePolynomial.parse(string, names);
    } catch (RuntimeException e) {
      if (isParserError(e)) {
        final String s = string.length() <= 32 ? string : string.substring(0, 32) + "...";
        throw new IllegalArgumentException(String.format("Failed to parse \"%s\"", s), e);
      } else {
        throw e;
      }
    }
  }

  private boolean isParserError(final Throwable e) {
    final StackTraceElement el = e.getStackTrace()[0];
    final String s = el.getClassName() + "." + el.getMethodName();
    return s.startsWith("cc.redberry.rings.Ring.divideExact")
        || s.startsWith("cc.redberry.rings.bigint.BigInteger.pow")
        || s.startsWith("cc.redberry.rings.io.Coder.mkOperand")
        || s.startsWith("cc.redberry.rings.io.Coder.parse")
        || s.startsWith("cc.redberry.rings.io.Coder.popEvaluate")
        || s.startsWith("cc.redberry.rings.io.Tokenizer.checkChar")
        || s.startsWith("cc.redberry.rings.io.Tokenizer.nextToken")
        || s.startsWith("java.util.ArrayDeque.removeFirst");
  }

  private Polynomial(
      final VariableSet rawVariables, final MultivariatePolynomial<BigInteger> rawPoly) {
    assert rawVariables.size() == rawPoly.nVariables;
    variables = rawVariables;
    raw = rawPoly;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof Polynomial)) {
      return false;
    }

    final Polynomial aPoly = (Polynomial) other;
    if (variables.equals(aPoly.variables)) {
      return raw.equals(aPoly.raw);
    }

    final VariableSet newVariables = variables.union(aPoly.variables);
    return translate(newVariables).equals(aPoly.translate(newVariables));
  }

  @Override
  public int hashCode() {
    return Objects.hash(variables, raw);
  }

  @Override
  public String toString() {
    return raw.toString(variables.getTable());
  }

  /**
   * Returns the same polynomial in a different variable set.
   *
   * @throws IllegalArgumentException when any of used variables are not in {@code newVariables}.
   */
  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  public Polynomial translate(final VariableSet newVariables) {
    if (variables == newVariables) {
      return this;
    } else if (variables.equals(newVariables)) {
      return new Polynomial(newVariables, raw);
    } else {
      if (variables.size() == 0) {
        return new Polynomial(newVariables, raw.setNVariables(newVariables.size()));
      } else {
        return new Polynomial(
            newVariables,
            raw.mapVariables(variables.map(newVariables)).setNVariables(newVariables.size()));
      }
    }
    // Postcondition: `variables` of the returned-value is the given variable set.
  }

  private Polynomial performBinaryOperation(
      final Polynomial other, final BinaryOperator<MultivariatePolynomial<BigInteger>> operator) {
    // NOTE: MultivariatePolynomial is not immutable. We need a clone of one of the
    // operands.
    if (variables.equals(other.variables)) {
      return new Polynomial(variables, operator.apply(raw.clone(), other.raw));
    } else {
      final VariableSet newVariables = variables.union(other.variables);
      return new Polynomial(
          newVariables,
          operator.apply(translate(newVariables).raw.clone(), other.translate(newVariables).raw));
    }
  }

  /** Returns the sum of this polynomial and the other. */
  public Polynomial add(final Polynomial other) {
    return performBinaryOperation(other, MultivariatePolynomial<BigInteger>::add);
  }

  /** Returns the difference of this polynomial from the other. */
  public Polynomial subtract(final Polynomial other) {
    return performBinaryOperation(other, MultivariatePolynomial<BigInteger>::subtract);
  }

  /** Returns the product of this polynomial and the other. */
  public Polynomial multiply(final Polynomial other) {
    return performBinaryOperation(other, MultivariatePolynomial<BigInteger>::multiply);
  }

  /**
   * Returns the quotient of this polynomial divided by the given divisor.
   *
   * @throws ArithmeticException when exact division is impossible
   */
  public Polynomial divideExact(final Polynomial divisor) {
    return performBinaryOperation(divisor, MultivariateDivision::divideExact);
  }

  /**
   * Returns this polynomial to the power {@code exponent}.
   *
   * @throws IllegalArgumentException when {@code exponent} is negative
   */
  public Polynomial pow(final int exponent) {
    return new Polynomial(variables, PolynomialMethods.polyPow(raw, exponent));
  }

  /**
   * Returns this polynomial to the power {@code exponent}.
   *
   * @throws IllegalArgumentException when {@code exponent} is negative
   */
  public Polynomial pow(final BigInteger exponent) {
    return new Polynomial(variables, PolynomialMethods.polyPow(raw, exponent));
  }
}
