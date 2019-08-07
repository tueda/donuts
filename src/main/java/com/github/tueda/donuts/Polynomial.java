package com.github.tueda.donuts;

import cc.redberry.rings.Rings;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.poly.PolynomialMethods;
import cc.redberry.rings.poly.multivar.Monomial;
import cc.redberry.rings.poly.multivar.MonomialOrder;
import cc.redberry.rings.poly.multivar.MultivariateDivision;
import cc.redberry.rings.poly.multivar.MultivariatePolynomial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BinaryOperator;

/** An immutable multivariate polynomial with integer coefficients. */
public final class Polynomial implements Serializable, Iterable<Polynomial> {
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
    return raw.toString(variables.getRawTable());
  }

  /** Returns an iterator for all terms (as polynomials) in this polynomial. */
  @Override
  public Iterator<Polynomial> iterator() {
    return new TermIterator(raw.iterator());
  }

  /** Iterates terms in a polynomial. */
  private class TermIterator implements Iterator<Polynomial> {
    /** The raw iterator. */
    private final Iterator<Monomial<BigInteger>> rawIterator;

    /** Constructs an iterator. */
    public TermIterator(final Iterator<Monomial<BigInteger>> iterator) {
      rawIterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return rawIterator.hasNext();
    }

    @Override
    public Polynomial next() {
      return new Polynomial(variables, raw.createZero().add(rawIterator.next()));
    }
  }

  /** Returns the raw polynomial object. */
  public MultivariatePolynomial<BigInteger> getRawPolynomial() {
    return raw.copy();
  }

  /** Returns whether this polynomial is zero. */
  public boolean isZero() {
    return raw.isZero();
  }

  /** Returns whether this polynomial is one. */
  public boolean isOne() {
    return raw.isOne();
  }

  /** Returns whether this polynomial is minus one. */
  public boolean isMinusOne() {
    if (raw.size() != 1) {
      return false;
    }
    final Monomial<BigInteger> lt = raw.first();
    return lt.isZeroVector() && Rings.Z.isMinusOne(lt.coefficient);
  }

  /** Returns whether this polynomial is constant. */
  public boolean isConstant() {
    return raw.isConstant();
  }

  /** Returns whether this polynomial is a monomial (including zero). */
  public boolean isMonomial() {
    return raw.isMonomial();
  }

  /** Returns whether this polynomial is monic, i.e., the leading coefficient is one. */
  public boolean isMonic() {
    return raw.isMonic();
  }

  /** Returns whether this polynomial is a plain variable (the coefficient is one). */
  public boolean isVariable() {
    return raw.size() == 1 && raw.degree() == 1 && raw.isMonic();
  }

  /** Returns the number of terms in this polynomial. */
  public int size() {
    return raw.size();
  }

  /** Returns the total degree of this polynomial. */
  public int degree() {
    return raw.degree();
  }

  /** Returns the degree in the given variable. */
  public int degree(final Variable variable) {
    final int j = variables.indexOf(variable.getName());
    if (j < 0) {
      return 0;
    }
    return raw.degree(j);
  }

  /** Returns the degree in the given variables. */
  public int degree(final VariableSet variables) {
    final int[] variableIndices = new int[variables.size()];
    int n = 0;

    int i = 0;
    for (final String x : variables.getRawIterable()) {
      final int j = this.variables.indexOf(x, i);
      if (j >= 0) {
        variableIndices[n++] = j;
        i = j + 1;
        if (i >= this.variables.size()) {
          break;
        }
      }
    }

    if (n == 0) {
      return 0;
    }

    return raw.degree(Arrays.copyOfRange(variableIndices, 0, n));
  }

  /** Returns the coefficient of the given variable with the specified exponent. */
  public Polynomial coefficientOf(final Variable variable, final int exponent) {
    final int j = variables.indexOf(variable.getName());
    if (j < 0) {
      return new Polynomial(variables, raw.createZero());
    }
    return new Polynomial(variables, raw.coefficientOf(j, exponent));
  }

  /** Returns the coefficient of the given variables with the specified exponents. */
  @SuppressWarnings("PMD.UseVarargs")
  public Polynomial coefficientOf(final Variable[] variables, final int[] exponents) {
    if (variables.length != exponents.length) {
      throw new IllegalArgumentException("sizes of variables and exponents unmatch");
    }

    final int length = variables.length;

    final int[] variableIndices = new int[length];
    final int[] newExponents = new int[length];
    int n = 0;

    int i = 0;
    int k = 0;
    for (final Variable v : variables) {
      final String x = v.getName();
      final int j = this.variables.indexOf(x, i);
      if (j >= 0) {
        variableIndices[n] = j;
        newExponents[n++] = exponents[k];
        i = j + 1;
        if (i >= this.variables.size()) {
          break;
        }
      } else if (exponents[k] != 0) {
        n = 0;
        break;
      }
      k++;
    }

    if (n == 0) {
      return new Polynomial(this.variables, raw.createZero());
    }

    return new Polynomial(
        this.variables,
        raw.coefficientOf(
            Arrays.copyOfRange(variableIndices, 0, n), Arrays.copyOfRange(newExponents, 0, n)));
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

  /** Returns the negation of this polynomial. */
  public Polynomial negate() {
    if (isZero()) {
      return this;
    } else {
      return new Polynomial(variables, raw.copy().negate());
    }
  }

  private Polynomial performBinaryOperation(
      final Polynomial other, final BinaryOperator<MultivariatePolynomial<BigInteger>> operator) {
    // NOTE: MultivariatePolynomial is not immutable. We need a clone of one of the
    // operands.
    if (variables.equals(other.variables)) {
      return new Polynomial(variables, operator.apply(raw.copy(), other.raw));
    } else {
      final VariableSet newVariables = variables.union(other.variables);
      return new Polynomial(
          newVariables,
          operator.apply(translate(newVariables).raw.copy(), other.translate(newVariables).raw));
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
