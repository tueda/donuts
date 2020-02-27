package com.github.tueda.donuts;

import cc.redberry.rings.Rings;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.poly.PolynomialFactorDecomposition;
import cc.redberry.rings.poly.PolynomialMethods;
import cc.redberry.rings.poly.multivar.Monomial;
import cc.redberry.rings.poly.multivar.MonomialOrder;
import cc.redberry.rings.poly.multivar.MultivariateDivision;
import cc.redberry.rings.poly.multivar.MultivariateFactorization;
import cc.redberry.rings.poly.multivar.MultivariateGCD;
import cc.redberry.rings.poly.multivar.MultivariatePolynomial;
import java.io.ObjectStreamException;
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

  /**
   * Constructs a polynomial from the given integer.
   *
   * @param value the integer to be converted to a polynomial
   */
  public Polynomial(final long value) {
    variables = VariableSet.EMPTY;
    raw = RAW_ZERO.createConstant(value);
  }

  /**
   * Constructs a polynomial from the given integer.
   *
   * @param value the integer to be converted to a polynomial
   */
  public Polynomial(final BigInteger value) {
    variables = VariableSet.EMPTY;
    raw = RAW_ZERO.createConstant(value);
  }

  /**
   * Constructs a polynomial from the given string.
   *
   * @param string the string to be parsed
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
   * @param string the string to be parsed
   * @return the resultant polynomial
   * @throws IllegalArgumentException when {@code string} does not represent a polynomial
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static Polynomial of(final String string) {
    return new Polynomial(string);
  }

  /**
   * Returns an array of polynomials constructed from the given array of strings.
   *
   * @param strings the array of strings to be parsed
   * @return the resultant array of polynomials
   * @throws IllegalArgumentException when any of the given strings are invalid for polynomials
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static Polynomial[] of(final String... strings) {
    return Stream.of(strings).map(Polynomial::new).toArray(Polynomial[]::new);
  }

  private Object readResolve() throws ObjectStreamException {
    // Note that Rings.Z (Integers) doesn't override equals(), as of Rings v.2.5.2.
    // In deserialization, we need to keep Rings.Z being singleton, otherwise we easily get
    // "Mixing polynomials over different coefficient rings: Z and Z" errors.
    return Polynomial.createFromRaw(variables, raw.setRingUnsafe(Rings.Z));
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
    final Polynomial poly = translate(getMinimalVariables());
    return Objects.hash(poly.variables, poly.raw);
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
   * Returns {@code true} if the polynomial actually uses the specified variable, i.e., has any
   * terms involving the variable.
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

  /**
   * Returns the raw polynomial object of the Rings library.
   *
   * @return the raw polynomial object
   */
  public MultivariatePolynomial<BigInteger> getRawPolynomial() {
    return raw.copy();
  }

  /* default */ MultivariatePolynomial<BigInteger> getRawPolynomialWithoutCopy() {
    // !!! Never modify it!!!
    return raw;
  }

  /**
   * Returns {@code true} if this polynomial is equal to zero, i.e., has no terms.
   *
   * @return {@code true} if this polynomial is {@code 0}
   */
  public boolean isZero() {
    return raw.isZero();
  }

  /**
   * Returns {@code true} if this polynomial is equal to unity.
   *
   * @return {@code true} if this polynomial is {@code 1}
   */
  public boolean isOne() {
    return raw.isOne();
  }

  /**
   * Returns {@code true} if this polynomial is equal to minus one.
   *
   * @return {@code true} if this polynomial is {@code 0}
   */
  public boolean isMinusOne() {
    if (raw.size() != 1) {
      return false;
    }
    final Monomial<BigInteger> lt = raw.first();
    return lt.isZeroVector() && Rings.Z.isMinusOne(lt.coefficient);
  }

  /**
   * Returns {@code true} if this polynomial is constant, i.e., an integer (including zero).
   *
   * @return {@code true} if this polynomial is constant
   */
  public boolean isConstant() {
    return raw.isConstant();
  }

  /**
   * Returns {@code true} if this polynomial is constant that fits in {@code short}.
   *
   * @return {@code true} if this polynomial fits in {@code short}
   */
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

  /**
   * Returns {@code true} if this polynomial is constant that fits in {@code int}.
   *
   * @return {@code true} if this polynomial fits in {@code int}
   */
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

  /**
   * Returns {@code true} if this polynomial is constant that fits in {@code long}.
   *
   * @return {@code true} if this polynomial fits in {@code long}
   */
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

  /**
   * Returns {@code true} if this polynomial is a monomial (including zero).
   *
   * @return {@code true} if this polynomial is a monomial
   */
  public boolean isMonomial() {
    return raw.isMonomial();
  }

  /**
   * Returns {@code true} if this polynomial is monic, i.e., the leading coefficient is one.
   *
   * @return {@code true} if this polynomial is monic
   */
  public boolean isMonic() {
    return raw.isMonic();
  }

  /**
   * Returns {@code true} if this polynomial is a plain variable (the coefficient is one).
   *
   * @return {@code true} if this polynomial is a variable
   */
  public boolean isVariable() {
    return raw.size() == 1 && raw.degree() == 1 && raw.isMonic();
  }

  /**
   * Returns this polynomial as {@code short}.
   *
   * @return the {@code short} value
   * @throws IllegalStateException when the polynomial is not a {@code short}
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
   * @return the {@code int} value
   * @throws IllegalStateException when the polynomial is not a {@code int}
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
   * @return the {@code long} value
   * @throws IllegalStateException when the polynomial is not a {@code long}
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
   * @return the variable
   * @throws IllegalStateException when the polynomial is not a variable
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

  /**
   * Returns the number of terms in this polynomial.
   *
   * @return the number of terms
   */
  public int size() {
    return raw.size();
  }

  /**
   * Returns the signum function of polynomial, defined by the sign of the coefficient of the
   * leading term.
   *
   * @return {@code -1} or {@code 1} as the coefficient of the leading term of this polynomial is
   *     negative or positive, or zero if it has no terms.
   */
  public int signum() {
    return raw.signumOfLC();
  }

  /**
   * Returns the total degree of this polynomial.
   *
   * @return the total degree
   */
  public int degree() {
    return raw.degree();
  }

  /**
   * Returns the degree in the given variable.
   *
   * @param variable the variable
   * @return the degree in the variable
   */
  public int degree(final Variable variable) {
    final int j = variables.indexOf(variable.getName());
    if (j < 0) {
      return 0;
    }
    return raw.degree(j);
  }

  /**
   * Returns the degree in the given variables.
   *
   * @param variables the variables
   * @return the degree in the variables
   */
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

  /**
   * Returns the coefficient of the given variable with the specified exponent.
   *
   * @param variable the variable
   * @param exponent the exponent
   * @return the coefficient
   */
  public Polynomial coefficientOf(final Variable variable, final int exponent) {
    final int j = variables.indexOf(variable.getName());
    if (j < 0) {
      if (exponent == 0) {
        // Example: coeff of (x + y) wrt z^0 => (x + y)
        return this;
      }
      return new Polynomial(variables, raw.createZero());
    }
    return new Polynomial(variables, raw.coefficientOf(j, exponent));
  }

  /**
   * Returns the coefficient of the given variables with the specified exponents.
   *
   * @param variables the variables
   * @param exponents the exponents
   * @return the coefficient
   * @throws IllegalArgumentException when {@code variables} and {@code exponents} have different
   *     lengths
   */
  @SuppressWarnings("PMD.UseVarargs")
  public Polynomial coefficientOf(final Variable[] variables, final int[] exponents) {
    if (variables.length != exponents.length) {
      throw new IllegalArgumentException("sizes of variables and exponents unmatch");
    }

    final int length = variables.length;

    final int[] variableIndices = new int[length];
    final int[] newExponents = new int[length];
    int n = 0;

    for (int i = 0; i < variables.length; i++) {
      final Variable v = variables[i];
      final int p = exponents[i];
      final String x = v.getName();
      final int j = this.variables.indexOf(x);
      if (j >= 0) {
        variableIndices[n] = j;
        newExponents[n++] = exponents[i];
      } else if (p != 0) {
        // The polynomial doesn't contain x^p (p != 0), immediately 0.
        return new Polynomial(this.variables, raw.createZero());
      }
    }

    if (n == 0) {
      // Example: coeff of (x + y) wrt a^0 b^0 => (x + y)
      // Also the case with empty variables and exponents.
      return this;
    }

    return new Polynomial(
        this.variables,
        raw.coefficientOf(
            Arrays.copyOfRange(variableIndices, 0, n), Arrays.copyOfRange(newExponents, 0, n)));
  }

  /**
   * Returns this polynomial in a different variable set.
   *
   * @param newVariables the new variables to be used
   * @return the resultant polynomial
   * @throws IllegalArgumentException when any of used variables are not in {@code newVariables}
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

  /**
   * Returns the negation of this polynomial.
   *
   * @return {@code -this}
   */
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
      final boolean doCopy) {
    // NOTE: For many methods, MultivariatePolynomial is not immutable. In such cases, we need a
    // clone of one of the operands.
    if (variables.equals(other.variables)) {
      return new Polynomial(variables, operator.apply(doCopy ? raw.copy() : raw, other.raw));
    } else {
      final VariableSet newVariables = variables.union(other.variables);
      final MultivariatePolynomial<BigInteger> raw1 = translate(newVariables).raw;
      return new Polynomial(
          newVariables,
          operator.apply(
              doCopy && raw1 == raw ? raw1.copy() : raw1, other.translate(newVariables).raw));
    }
  }

  /**
   * Returns the sum of this polynomial and the other.
   *
   * @param other the other polynomial to be added to this polynomial
   * @return {@code this + other}
   */
  public Polynomial add(final Polynomial other) {
    return performBinaryOperation(other, MultivariatePolynomial<BigInteger>::add, true);
  }

  /**
   * Returns the difference of this polynomial from the other.
   *
   * @param other the other polynomial to be subtracted from this polynomial
   * @return {@code this - other}
   */
  public Polynomial subtract(final Polynomial other) {
    return performBinaryOperation(other, MultivariatePolynomial<BigInteger>::subtract, true);
  }

  /**
   * Returns the product of this polynomial and the other.
   *
   * @param other the other polynomial to be multiplied by this polynomial
   * @return {@code this * other}
   */
  public Polynomial multiply(final Polynomial other) {
    return performBinaryOperation(other, MultivariatePolynomial<BigInteger>::multiply, true);
  }

  /**
   * Returns the quotient of this polynomial divided by the given divisor.
   *
   * @param divisor the divisor
   * @return {@code this / divisor} as a polynomial
   * @throws ArithmeticException when exact division is impossible
   */
  public Polynomial divideExact(final Polynomial divisor) {
    return performBinaryOperation(divisor, MultivariateDivision::divideExact, false);
  }

  /**
   * Returns a rational function whose value is (this / divisor).
   *
   * @param divisor the divisor
   * @return {@code this / divisor} as a rational function
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction divide(final Polynomial divisor) {
    return new RationalFunction(this, divisor);
  }

  /**
   * Returns this polynomial raised to the power {@code exponent}.
   *
   * @param exponent the exponent to which this polynomial is to be raised
   * @return {@code this ^ exponent}
   * @throws IllegalArgumentException when {@code exponent} is negative
   */
  public Polynomial pow(final int exponent) {
    return new Polynomial(variables, PolynomialMethods.polyPow(raw, exponent));
  }

  /**
   * Returns this polynomial raised to the power {@code exponent}.
   *
   * @param exponent the exponent to which this polynomial is to be raised
   * @return {@code this ^ exponent}
   * @throws IllegalArgumentException when {@code exponent} is negative
   */
  public Polynomial pow(final BigInteger exponent) {
    return new Polynomial(variables, PolynomialMethods.polyPow(raw, exponent));
  }

  /**
   * Returns the greatest common divisor of this polynomial and the other.
   *
   * @param other the polynomial with which the GCD is to be computed
   * @return {@code GCD(this, other)}
   */
  public Polynomial gcd(final Polynomial other) {
    return performBinaryOperation(other, MultivariateGCD::PolynomialGCD, false);
  }

  /**
   * Returns the greatest common divisor of the given polynomials.
   *
   * @param polynomials the polynomials for which the GCD is to be computed
   * @return {@code GCD(polynomial1, ..., polynomialN)}
   */
  public static Polynomial gcdOf(final Polynomial... polynomials) {
    if (polynomials.length == 0) {
      // gcd() -> 0
      return new Polynomial();
    }
    if (polynomials.length == 1) {
      // gcd(x) -> x
      return polynomials[0];
    }

    final VariableSet newVariables = VariableSet.unionOf(polynomials);

    final List<MultivariatePolynomial<BigInteger>> polys =
        Stream.of(polynomials).map(p -> p.translate(newVariables).raw).collect(Collectors.toList());

    return new Polynomial(newVariables, MultivariateGCD.PolynomialGCD(polys));
  }

  /**
   * Returns the greatest common divisor of the given polynomials.
   *
   * @param polynomials the polynomials for which the GCD is to be computed
   * @return {@code GCD(polynomial1, ..., polynomialN)}
   */
  public static Polynomial gcdOf(final Iterable<Polynomial> polynomials) {
    return gcdOf(StreamSupport.stream(polynomials.spliterator(), false).toArray(Polynomial[]::new));
  }

  /**
   * Returns the greatest common divisor of the given polynomials.
   *
   * @param polynomials the polynomials for which the GCD is to be computed
   * @return {@code GCD(polynomial1, ..., polynomialN)}
   */
  public static Polynomial gcdOf(final Stream<Polynomial> polynomials) {
    return gcdOf(polynomials.toArray(Polynomial[]::new));
  }

  /**
   * Returns the least common multiple of this polynomial and the other.
   *
   * @param other the polynomial with which the LCM is to be computed
   * @return {@code LCM(this, other)}
   */
  public Polynomial lcm(final Polynomial other) {
    return performBinaryOperation(other, Polynomial::polynomialLcm, true);
  }

  /**
   * Returns the least common multiple of the given polynomials.
   *
   * @param polynomials the polynomials for which the LCM is to be computed
   * @return {@code LCM(polynomial1, ..., polynomialN)}
   * @throws IllegalArgumentException when no polynomial is given
   */
  public static Polynomial lcmOf(final Polynomial... polynomials) {
    if (polynomials.length == 0) {
      // lcm() -> undefined
      throw new IllegalArgumentException("lcm with 0 arguments");
    }
    if (polynomials.length == 1) {
      // lcm(x) -> x
      return polynomials[0];
    }

    final VariableSet newVariables = VariableSet.unionOf(polynomials);

    final List<MultivariatePolynomial<BigInteger>> polys =
        Stream.of(polynomials).map(p -> p.translate(newVariables).raw).collect(Collectors.toList());

    return new Polynomial(newVariables, Polynomial.polynomialLcm(polys));
  }

  /**
   * Returns the least common multiple of the given polynomials.
   *
   * @param polynomials the polynomials for which the LCM is to be computed
   * @return {@code LCM(polynomial1, ..., polynomialN)}
   * @throws IllegalArgumentException when no polynomial is given
   */
  public static Polynomial lcmOf(final Iterable<Polynomial> polynomials) {
    return lcmOf(StreamSupport.stream(polynomials.spliterator(), false).toArray(Polynomial[]::new));
  }

  /**
   * Returns the least common multiple of the given polynomials.
   *
   * @param polynomials the polynomials for which the LCM is to be computed
   * @return {@code LCM(polynomial1, ..., polynomialN)}
   * @throws IllegalArgumentException when no polynomial is given
   */
  public static Polynomial lcmOf(final Stream<Polynomial> polynomials) {
    return lcmOf(polynomials.toArray(Polynomial[]::new));
  }

  private static MultivariatePolynomial<BigInteger> polynomialLcm(
      final MultivariatePolynomial<BigInteger> a, final MultivariatePolynomial<BigInteger> b) {
    if (a.isZero()) {
      // LCM(0, b) -> 0, including LCM(0, 0) -> 0
      return a;
    }
    if (b.isZero()) {
      // LCM(a, 0) -> 0
      return b;
    }
    if (a.isOne()) {
      // LCM(1, b) -> b
      return b;
    }
    if (b.isOne()) {
      // LCM(a, 1) -> a
      return a;
    }
    final MultivariatePolynomial<BigInteger> gcd = MultivariateGCD.PolynomialGCD(a, b);
    // CATION: the following line changes `a`.
    return MultivariateDivision.divideExact(a.multiply(b), gcd);
  }

  private static MultivariatePolynomial<BigInteger> polynomialLcm(
      final Iterable<MultivariatePolynomial<BigInteger>> array) {
    boolean first = true;
    MultivariatePolynomial<BigInteger> lcm = null;
    for (final MultivariatePolynomial<BigInteger> poly : array) {
      if (first) {
        lcm = poly.copy();
        first = false;
      } else {
        lcm = polynomialLcm(lcm, poly);
      }
    }
    return lcm;
  }

  /**
   * Perform the factorization of this polynomial.
   *
   * @return the factors of this polynomial
   */
  public Polynomial[] factors() {
    if (isConstant()) {
      return new Polynomial[] {this};
    }

    // Perform the factorization.

    final PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>> decomposition =
        MultivariateFactorization.Factor(raw);

    decomposition.canonical();

    // Construct the result.

    final List<Polynomial> factors = new ArrayList<>();

    if (!decomposition.unit.isOne()) {
      factors.add(new Polynomial(variables, decomposition.unit));
    }

    for (int i = 0; i < decomposition.size(); i++) {
      final MultivariatePolynomial<BigInteger> factor = decomposition.get(i);
      Polynomial poly;
      int exponent = decomposition.getExponent(i);
      if (exponent == 1 && factor.isMonomial() && factor.isEffectiveUnivariate()) {
        final int variable = factor.univariateVariable();
        exponent = factor.degree(variable);
        poly = new Polynomial(variables, factor.createMonomial(variable, 1));
      } else {
        poly = new Polynomial(variables, factor);
      }
      for (int j = 0; j < exponent; j++) {
        factors.add(poly);
      }
    }

    return factors.toArray(new Polynomial[0]);
  }

  /**
   * Returns the result of the given substitution. The left-hand side must be a non-constant monic
   * monomial.
   *
   * @param lhs the left-hand side
   * @param rhs the right-hand side
   * @return the resultant polynomial
   * @throws IllegalArgumentException when {@code lhs} is invalid
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

  /**
   * Returns the partial derivative with respect to the given variable.
   *
   * @param variable the variable
   * @return the resultant polynomial
   */
  public Polynomial derivative(final Variable variable) {
    return derivative(variable, 1);
  }

  /**
   * Returns the partial derivative of the specified order with respect to the given variable.
   *
   * @param variable the variable
   * @param order the order
   * @return the resultant polynomial
   * @throws IllegalArgumentException when {@code order} is negative
   */
  public Polynomial derivative(final Variable variable, final int order) {
    if (order < 0) {
      throw new IllegalArgumentException(String.format("Negative order given: %s", order));
    }
    if (order == 0) {
      return this;
    }

    final int i = variables.indexOf(variable.getName());
    if (i < 0) {
      return new Polynomial();
    }

    return new Polynomial(variables, raw.derivative(i, order));
  }
}
