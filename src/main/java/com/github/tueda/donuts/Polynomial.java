package com.github.tueda.donuts;

import cc.redberry.rings.Rings;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.poly.PolynomialFactorDecomposition;
import cc.redberry.rings.poly.PolynomialMethods;
import cc.redberry.rings.poly.multivar.Monomial;
import cc.redberry.rings.poly.multivar.MonomialOrder;
import cc.redberry.rings.poly.multivar.MultivariateDivision;
import cc.redberry.rings.poly.multivar.MultivariatePolynomial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** A multivariate polynomial with integer coefficients. */
public final class Polynomial implements Serializable, Iterable<Polynomial>, Multivariate {
  private static final long serialVersionUID = 1L;

  /** Raw zero polynomial. */
  /* default */ static final MultivariatePolynomial<BigInteger> RAW_ZERO =
      MultivariatePolynomial.zero(0, Rings.Z, MonomialOrder.DEFAULT);

  /** The minimum value of short. */
  private static final BigInteger SHORT_MIN_VALUE = BigInteger.valueOf(Short.MIN_VALUE);

  /** The maximum value of short. */
  private static final BigInteger SHORT_MAX_VALUE = BigInteger.valueOf(Short.MAX_VALUE);

  /** The minimum value of int. */
  private static final BigInteger INT_MIN_VALUE = BigInteger.valueOf(Integer.MIN_VALUE);

  /** The maximum value of int. */
  private static final BigInteger INT_MAX_VALUE = BigInteger.valueOf(Integer.MAX_VALUE);

  /** The minimum value of long. */
  private static final BigInteger LONG_MIN_VALUE = BigInteger.valueOf(Long.MIN_VALUE);

  /** The maximum value of long. */
  private static final BigInteger LONG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);

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
    variables = VariableSet.createFromRaw(names);
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
      final VariableSet newVariables, final MultivariatePolynomial<BigInteger> rawPoly) {
    assert newVariables.size() == rawPoly.nVariables;
    variables = newVariables;
    raw = rawPoly;
  }

  /* default */ static Polynomial createFromRaw(
      final VariableSet newVariables, final MultivariatePolynomial<BigInteger> rawPoly) {
    return new Polynomial(newVariables, rawPoly);
  }

  /**
   * Returns a polynomial constructed from the given string.
   *
   * @param string the string representation
   * @throws IllegalArgumentException when {@code string} does not represent a polynomial
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static Polynomial of(final String string) {
    return new Polynomial(string);
  }

  /**
   * Returns an array of polynomials constructed from the given strings.
   *
   * @param strings the string representations
   * @throws IllegalArgumentException when any of the given strings are invalid for polynomials.
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static Polynomial[] of(final String... strings) {
    return Stream.of(strings).map(Polynomial::new).toArray(Polynomial[]::new);
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

    if (size() != aPoly.size()) {
      return false;
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

  @Override
  public VariableSet getVariables() {
    return variables;
  }

  @Override
  public VariableSet getMinimalVariables() {
    if (variables.isEmpty()) {
      return variables;
    }

    final String[] newTable =
        IntStream.range(0, variables.size())
            .filter(i -> isVariableUsed(raw, i))
            .mapToObj(i -> variables.getRawName(i))
            .toArray(String[]::new);
    return VariableSet.createFromRaw(newTable);
  }

  /**
   * Returns whether the polynomial actually uses the specified variable, i.e., has any terms
   * involving the variable.
   */
  private static boolean isVariableUsed(
      final MultivariatePolynomial<BigInteger> rawPolynomial, final int variable) {
    for (final Monomial<BigInteger> term : rawPolynomial) {
      if (term.exponents[variable] > 0) {
        return true;
      }
    }
    return false;
  }

  /** Returns the raw polynomial object. */
  public MultivariatePolynomial<BigInteger> getRawPolynomial() {
    return raw.copy();
  }

  /* default */ MultivariatePolynomial<BigInteger> getRawPolynomialWithoutCopy() {
    // !!! Never modify it!!!
    return raw;
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

  /** Returns whether this polynomial is constant, i.e., an integer. */
  public boolean isConstant() {
    return raw.isConstant();
  }

  /** Returns whether this polynomial is constant fitting in a short. */
  public boolean isShortValue() {
    if (isZero()) {
      return true;
    }
    if (!isConstant()) {
      return false;
    }
    final BigInteger c = raw.lc();
    return c.compareTo(SHORT_MIN_VALUE) >= 0 && c.compareTo(SHORT_MAX_VALUE) <= 0;
  }

  /** Returns whether this polynomial is constant fitting in a int. */
  public boolean isIntValue() {
    if (isZero()) {
      return true;
    }
    if (!isConstant()) {
      return false;
    }
    final BigInteger c = raw.lc();
    return c.compareTo(INT_MIN_VALUE) >= 0 && c.compareTo(INT_MAX_VALUE) <= 0;
  }

  /** Returns whether this polynomial is constant fitting in a long. */
  public boolean isLongValue() {
    if (isZero()) {
      return true;
    }
    if (!isConstant()) {
      return false;
    }
    final BigInteger c = raw.lc();
    return c.compareTo(LONG_MIN_VALUE) >= 0 && c.compareTo(LONG_MAX_VALUE) <= 0;
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

  /**
   * Returns this polynomial as {@code short}.
   *
   * @throws IllegalStateException when the polynomial is not a {@code short}.
   */
  @SuppressWarnings("PMD.AvoidUsingShortType")
  public short asShortValue() {
    if (isZero()) {
      return 0;
    }
    if (!isShortValue()) {
      throw new IllegalStateException("not a short");
    }
    return raw.lc().shortValue();
  }

  /**
   * Returns this polynomial as a {@code int}.
   *
   * @throws IllegalStateException when the polynomial is not a {@code int}.
   */
  public int asIntValue() {
    if (isZero()) {
      return 0;
    }
    if (!isIntValue()) {
      throw new IllegalStateException("not a int");
    }
    return raw.lc().intValue();
  }

  /**
   * Returns this polynomial as a {@code long}.
   *
   * @throws IllegalStateException when the polynomial is not a {@code long}.
   */
  public long asLongValue() {
    if (isZero()) {
      return 0;
    }
    if (!isLongValue()) {
      throw new IllegalStateException("not a long");
    }
    return raw.lc().longValue();
  }

  /**
   * Returns this polynomial as a variable.
   *
   * @throws IllegalStateException when the polynomial is not a variable.
   */
  public Variable asVariable() {
    if (!isVariable()) {
      throw new IllegalStateException("not a variable");
    }
    final Monomial<BigInteger> term = raw.lt();
    for (int i = 0; i < term.exponents.length; i++) {
      assert term.exponents[i] == 0 || term.exponents[i] == 1;
      if (term.exponents[i] != 0) {
        return Variable.createWithoutCheck(variables.getRawName(i));
      }
    }
    assert false;
    return null;
  }

  /** Returns the number of terms in this polynomial. */
  public int size() {
    return raw.size();
  }

  /** Returns signum of the leading coefficient. */
  public int signum() {
    return raw.signumOfLC();
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
    for (final String x : variables.getRawTable()) {
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
      if (variables.isEmpty()) {
        // 0 -> N variables (N >= 1).
        return new Polynomial(newVariables, raw.setNVariables(newVariables.size()));
      } else {
        final int[] mapping = variables.map(newVariables);
        if (mapping == null) {
          // No direct mapping from the current set of variables to the new one.
          // Then consider a composition of 2 mappings: first, shrinking the current set to
          // the minimum one, and secondly to the new one.
          final VariableSet minVariables = getMinimalVariables();
          if (variables.equals(minVariables)) {
            // Already the current set is minimal. No way to proceed.
            throw new IllegalArgumentException(
                String.format("Variables %s does not fit in %s", variables, newVariables));
          }
          final int[] minMapping = variables.map(minVariables, variables.size() - 1);
          return new Polynomial(
                  minVariables, raw.mapVariables(minMapping).setNVariables(minVariables.size()))
              .translate(newVariables);
        }
        return new Polynomial(
            newVariables, raw.mapVariables(mapping).setNVariables(newVariables.size()));
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

  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  private Polynomial performBinaryOperation(
      final Polynomial other,
      final BinaryOperator<MultivariatePolynomial<BigInteger>> operator,
      final boolean doClone) {
    // NOTE: For many methods, MultivariatePolynomial is not immutable. In such cases, we need a
    // clone of one of the operands.
    if (variables.equals(other.variables)) {
      return new Polynomial(variables, operator.apply(doClone ? raw.copy() : raw, other.raw));
    } else {
      final VariableSet newVariables = variables.union(other.variables);
      final MultivariatePolynomial<BigInteger> raw1 = translate(newVariables).raw;
      return new Polynomial(
          newVariables,
          operator.apply(
              doClone && raw1 == raw1 ? raw1.copy() : raw1, other.translate(newVariables).raw));
    }
  }

  /** Returns the sum of this polynomial and the other. */
  public Polynomial add(final Polynomial other) {
    return performBinaryOperation(other, MultivariatePolynomial<BigInteger>::add, true);
  }

  /** Returns the difference of this polynomial from the other. */
  public Polynomial subtract(final Polynomial other) {
    return performBinaryOperation(other, MultivariatePolynomial<BigInteger>::subtract, true);
  }

  /** Returns the product of this polynomial and the other. */
  public Polynomial multiply(final Polynomial other) {
    return performBinaryOperation(other, MultivariatePolynomial<BigInteger>::multiply, true);
  }

  /**
   * Returns the quotient of this polynomial divided by the given divisor.
   *
   * @throws ArithmeticException when exact division is impossible
   */
  public Polynomial divideExact(final Polynomial divisor) {
    return performBinaryOperation(divisor, MultivariateDivision::divideExact, false);
  }

  /**
   * Returns a rational function whose value is (this / divisor).
   *
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction divide(final Polynomial divisor) {
    return new RationalFunction(this, divisor);
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

  /** Returns the sum of this polynomial and the other. */
  public Polynomial gcd(final Polynomial other) {
    return performBinaryOperation(other, PolynomialMethods::PolynomialGCD, false);
  }

  /** Returns the greatest common divisor of the given polynomials. */
  public static Polynomial gcd(final Polynomial... polynomials) {
    if (polynomials.length == 0) {
      // gcd() -> 0
      return new Polynomial();
    }
    if (polynomials.length == 1) {
      // gcd(x) -> x
      return polynomials[0];
    }

    final VariableSet newVariables = VariableSet.union(polynomials);

    final List<MultivariatePolynomial<BigInteger>> polys =
        Stream.of(polynomials).map(p -> p.translate(newVariables).raw).collect(Collectors.toList());

    return new Polynomial(newVariables, PolynomialMethods.PolynomialGCD(polys));
  }

  /** Returns the greatest common divisor of the given polynomials. */
  public static Polynomial gcd(final Iterable<Polynomial> polynomials) {
    return gcd(StreamSupport.stream(polynomials.spliterator(), false).toArray(Polynomial[]::new));
  }

  /** Returns the greatest common divisor of the given polynomials. */
  public static Polynomial gcd(final Stream<Polynomial> polynomials) {
    return gcd(polynomials.toArray(Polynomial[]::new));
  }

  /** Returns the factors of the polynomial. */
  public Polynomial[] factorize() {
    // Trivial cases, in which the polynomial is a monomial.
    if (isMonomial()) {
      return new Polynomial[] {this};
    }

    // Perform the factorization.

    final PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>> factors =
        PolynomialMethods.Factor(raw);

    factors.canonical();

    final List<Polynomial> list = new ArrayList<>(factors.sumExponents() + 1);

    // First, add the trivial factor (e.g., a monomial).

    final MultivariatePolynomial<BigInteger> trivialFactor = factors.unit;

    for (int i = 0; i < factors.size(); i++) {
      final MultivariatePolynomial<BigInteger> p = factors.get(i);
      if (p.isMonomial()) {
        trivialFactor.multiply(p);
      }
    }

    if (!trivialFactor.isOne()) {
      list.add(new Polynomial(variables, trivialFactor));
    }

    // Then, the actual factors.

    for (int i = 0; i < factors.size(); i++) {
      final MultivariatePolynomial<BigInteger> p = factors.get(i);
      if (!p.isMonomial()) {
        final Polynomial aPoly = new Polynomial(variables, p);
        for (int j = 0; j < factors.getExponent(i); j++) {
          list.add(aPoly);
        }
      }
    }

    return list.toArray(new Polynomial[0]);
  }

  /**
   * Returns the result of the given substitution.
   *
   * @throws IllegalArgumentException when {@code lhs} is invalid.
   */
  public Polynomial substitute(final Polynomial lhs, final Polynomial rhs) {
    SubstitutionUtils.checkLhs(lhs);

    if (!getMinimalVariables().intersects(lhs.getMinimalVariables())) {
      return this;
    }

    final VariableSet newVariables = variables.union(lhs.getVariables()).union(rhs.getVariables());
    final MultivariatePolynomial<BigInteger> newRawPoly =
        SubstitutionUtils.substitute(
            translate(newVariables).raw,
            lhs.translate(newVariables).raw.first(),
            rhs.translate(newVariables).raw);
    return new Polynomial(newVariables, newRawPoly);
  }
}
