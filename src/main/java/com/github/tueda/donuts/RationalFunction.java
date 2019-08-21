package com.github.tueda.donuts;

import cc.redberry.rings.Rational;
import cc.redberry.rings.Rationals;
import cc.redberry.rings.Rings;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.io.Coder;
import cc.redberry.rings.poly.MultivariateRing;
import cc.redberry.rings.poly.multivar.Monomial;
import cc.redberry.rings.poly.multivar.MultivariatePolynomial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/** An immutable multivariate rational function. */
public final class RationalFunction implements Serializable, Multivariate {
  private static final long serialVersionUID = 1L;

  /** The limitation on the number of variables. */
  private static final int MAX_VARIABLES = 300;

  /** The list of raw ring objects. */
  private static final List<MultivariateRing<MultivariatePolynomial<BigInteger>>> RAW_RINGS =
      new ArrayList<>();

  /** The list of raw fraction field objects. */
  private static final List<Rationals<MultivariatePolynomial<BigInteger>>> RAW_FIELDS =
      new ArrayList<>();

  /** Zero rational function. */
  /* default */ static final Rational<MultivariatePolynomial<BigInteger>> RAW_ZERO;

  static {
    assert MAX_VARIABLES >= 0;

    for (int i = 0; i <= MAX_VARIABLES; i++) {
      RAW_RINGS.add(Rings.MultivariateRing(i, Rings.Z));
      RAW_FIELDS.add(Rings.Frac(RAW_RINGS.get(i)));
    }

    RAW_ZERO = new Rational<>(RAW_RINGS.get(0), Polynomial.RAW_ZERO);
  }

  /** The set of variables. */
  private final VariableSet variables;

  /** The raw rational function object. */
  private final Rational<MultivariatePolynomial<BigInteger>> raw;

  /** Constructs a zero rational function. */
  public RationalFunction() {
    variables = VariableSet.EMPTY;
    raw = RAW_ZERO;
  }

  /** Constructs a rational function from the given integer. */
  public RationalFunction(final long value) {
    variables = VariableSet.EMPTY;
    raw = new Rational<>(getRings(0), Polynomial.RAW_ZERO.createConstant(value));
  }

  /** Constructs a rational function from the given integer. */
  public RationalFunction(final BigInteger value) {
    variables = VariableSet.EMPTY;
    raw = new Rational<>(getRings(0), Polynomial.RAW_ZERO.createConstant(value));
  }

  /** Constructs a rational function from the given polynomial. */
  public RationalFunction(final Polynomial poly) {
    variables = poly.getVariables();
    raw = new Rational<>(getRings(variables.size()), poly.getRawPolynomialWithoutCopy());
  }

  /**
   * Constructs a rational function from the given numerator and denominator.
   *
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction(final long numerator, final long denominator) {
    variables = VariableSet.EMPTY;
    raw =
        new Rational<>(
            getRings(0),
            Polynomial.RAW_ZERO.createConstant(numerator),
            Polynomial.RAW_ZERO.createConstant(denominator));
  }

  /**
   * Constructs a rational function from the given numerator and denominator.
   *
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction(final BigInteger numerator, final BigInteger denominator) {
    variables = VariableSet.EMPTY;
    raw =
        new Rational<>(
            getRings(0),
            Polynomial.RAW_ZERO.createConstant(numerator),
            Polynomial.RAW_ZERO.createConstant(denominator));
  }

  /**
   * Constructs a rational function from the given numerator and denominator.
   *
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction(final Polynomial numerator, final Polynomial denominator) {
    variables = numerator.getVariables().union(denominator.getVariables());
    raw =
        new Rational<>(
            getRings(variables.size()),
            numerator.translate(variables).getRawPolynomialWithoutCopy(),
            denominator.translate(variables).getRawPolynomialWithoutCopy());
  }

  /**
   * Constructs a rational function from the given string.
   *
   * @throws IllegalArgumentException when {@code string} does not represent a rational function
   */
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public RationalFunction(final String string) {
    final String[] names = Variable.guessVariableNames(string);
    variables = VariableSet.createFromRaw(names);
    try {
      raw = getCoder(variables).parse(string);
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
    return s.startsWith("cc.redberry.rings.bigint.BigInteger.pow")
        || s.startsWith("cc.redberry.rings.io.Coder.mkOperand")
        || s.startsWith("cc.redberry.rings.io.Coder.parse")
        || s.startsWith("cc.redberry.rings.io.Coder.popEvaluate")
        || s.startsWith("cc.redberry.rings.io.Tokenizer.checkChar")
        || s.startsWith("cc.redberry.rings.io.Tokenizer.nextToken")
        || s.startsWith("java.util.ArrayDeque.removeFirst");
  }

  private RationalFunction(
      final VariableSet rawVariables, final Rational<MultivariatePolynomial<BigInteger>> rawRat) {
    assert rawVariables.size() == ((MultivariateRing<?>) rawRat.ring).nVariables();
    checkNumberOfVariables(rawVariables.size());
    variables = rawVariables;
    raw = rawRat;
  }

  private static void checkNumberOfVariables(final int nvars) {
    if (nvars > MAX_VARIABLES) {
      throw new ArithmeticException(
          String.format("sorry, too many variables %s > %s", nvars, MAX_VARIABLES));
    }
  }

  /* default */ static MultivariateRing<MultivariatePolynomial<BigInteger>> getRings(
      final int nvars) {
    checkNumberOfVariables(nvars);
    return RAW_RINGS.get(nvars);
  }

  private static Rationals<MultivariatePolynomial<BigInteger>> getFields(final int nvars) {
    checkNumberOfVariables(nvars);
    return RAW_FIELDS.get(nvars);
  }

  private static Coder<Rational<MultivariatePolynomial<BigInteger>>, ?, ?> getCoder(
      final VariableSet variables) {
    return Coder.mkRationalsCoder(
        getFields(variables.size()),
        Coder.mkMultivariateCoder(getRings(variables.size()), variables.getRawTable()));
  }

  /**
   * Returns a rational function constructed from the given string.
   *
   * @param string the string representation
   * @throws IllegalArgumentException when {@code string} does not represent a rational function
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static RationalFunction of(final String string) {
    return new RationalFunction(string);
  }

  /**
   * Returns an array of rational functions constructed from the given strings.
   *
   * @param strings the string representations
   * @throws IllegalArgumentException when any of the given strings are invalid for rational
   *     functions.
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static RationalFunction[] of(final String... strings) {
    return Stream.of(strings).map(RationalFunction::new).toArray(RationalFunction[]::new);
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof RationalFunction)) {
      return false;
    }

    final RationalFunction aRat = (RationalFunction) other;

    if (variables.equals(aRat.variables)) {
      return raw.equals(aRat.raw);
    }

    final VariableSet newVariables = variables.union(aRat.variables);
    return translate(newVariables).raw.equals(aRat.translate(newVariables).raw);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variables, raw);
  }

  @Override
  public String toString() {
    return getCoder(variables).stringify(raw);
  }

  @Override
  public VariableSet getVariables() {
    return variables;
  }

  @Override
  public VariableSet getMinimalVariables() {
    return getNumerator().getMinimalVariables().union(getDenominator().getMinimalVariables());
  }

  /** Returns whether this rational function is zero. */
  public boolean isZero() {
    return raw.isZero();
  }

  /** Returns whether this rational function is one. */
  public boolean isOne() {
    return raw.isOne();
  }

  /** Returns whether this rational function is minus one. */
  public boolean isMinusOne() {
    return raw.isIntegral() && getNumerator().isMinusOne();
  }

  /** Returns whether this rational function is constant, i.e., a rational number. */
  public boolean isConstant() {
    return raw.numerator().isConstant() && raw.denominator().isConstant();
  }

  /** Returns whether this rational function is an integer. */
  public boolean isInteger() {
    return raw.isIntegral() && raw.numerator().isConstant();
  }

  /** Returns whether this rational function is a variable. */
  public boolean isVariable() {
    return raw.isIntegral() && getNumerator().isVariable();
  }

  /** Returns whether this rational function is an integer polynomial. */
  public boolean isPolynomial() {
    return raw.isIntegral();
  }

  /** Returns the numerator. */
  public Polynomial getNumerator() {
    return Polynomial.createFromRaw(variables, raw.numerator());
  }

  /** Returns the denominator. */
  public Polynomial getDenominator() {
    return Polynomial.createFromRaw(variables, raw.denominator());
  }

  /**
   * Returns the same rational function in a different variable set.
   *
   * @throws IllegalArgumentException when any of used variables are not in {@code newVariables}.
   */
  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  public RationalFunction translate(final VariableSet newVariables) {
    if (variables == newVariables) {
      return this;
    } else if (variables.equals(newVariables)) {
      return new RationalFunction(newVariables, raw);
    } else {
      final int nvars = newVariables.size();
      if (variables.isEmpty()) {
        return new RationalFunction(
            newVariables,
            new Rational<>(
                getRings(nvars),
                raw.numerator().setNVariables(nvars),
                raw.denominator().setNVariables(nvars)));
      } else {
        final int[] mapping = variables.map(newVariables);
        return new RationalFunction(
            newVariables,
            new Rational<>(
                getRings(nvars),
                raw.numerator().mapVariables(mapping).setNVariables(nvars),
                raw.denominator().mapVariables(mapping).setNVariables(nvars)));
      }
    }
    // Postcondition: `variables` of the returned-value is the given variable set.
  }

  /** Returns the negation of this rational function. */
  public RationalFunction negate() {
    if (isZero()) {
      return this;
    } else {
      return new RationalFunction(variables, raw.negate());
    }
  }

  /** Returns the reciprocal of this rational function. */
  public RationalFunction reciprocal() {
    return new RationalFunction(variables, raw.reciprocal());
  }

  private RationalFunction performBinaryOperation(
      final RationalFunction other,
      final BinaryOperator<Rational<MultivariatePolynomial<BigInteger>>> operator) {
    if (variables.equals(other.variables)) {
      return new RationalFunction(variables, operator.apply(raw, other.raw));
    } else {
      final VariableSet newVariables = variables.union(other.variables);
      return new RationalFunction(
          newVariables,
          operator.apply(translate(newVariables).raw, other.translate(newVariables).raw));
    }
  }

  /** Returns the sum of this rational function and the other. */
  public RationalFunction add(final RationalFunction other) {
    return performBinaryOperation(other, Rational<MultivariatePolynomial<BigInteger>>::add);
  }

  /** Returns the difference of this rational function from the other. */
  public RationalFunction subtract(final RationalFunction other) {
    return performBinaryOperation(other, Rational<MultivariatePolynomial<BigInteger>>::subtract);
  }

  /** Returns the product of this rational function and the other. */
  public RationalFunction multiply(final RationalFunction other) {
    return performBinaryOperation(other, Rational<MultivariatePolynomial<BigInteger>>::multiply);
  }

  /** Returns the quotient of this polynomial divided by the given divisor. */
  public RationalFunction divide(final RationalFunction divisor) {
    return performBinaryOperation(divisor, Rational<MultivariatePolynomial<BigInteger>>::divide);
  }

  /** Returns this rational function to the given power. */
  public RationalFunction pow(final int exponent) {
    return new RationalFunction(variables, raw.pow(exponent));
  }

  /** Returns this rational function to the given power. */
  public RationalFunction pow(final BigInteger exponent) {
    return new RationalFunction(variables, raw.pow(exponent));
  }

  /**
   * Returns the result of the given substitution.
   *
   * @throws IllegalArgumentException when {@code lhs} is invalid.
   */
  public RationalFunction substitute(final Polynomial lhs, final RationalFunction rhs) {
    SubstitutionUtils.checkLhs(lhs);

    if (!getMinimalVariables().intersects(lhs.getMinimalVariables())) {
      return this;
    }

    final VariableSet newVariables = variables.union(lhs.getVariables()).union(rhs.getVariables());

    final Rational<MultivariatePolynomial<BigInteger>> rawRat = translate(newVariables).raw;
    final Monomial<BigInteger> rawLhs =
        lhs.translate(newVariables).getRawPolynomialWithoutCopy().first();
    final Rational<MultivariatePolynomial<BigInteger>> rawRhs = rhs.translate(newVariables).raw;

    final Rational<MultivariatePolynomial<BigInteger>> rawNum =
        SubstitutionUtils.substitute(rawRat.numerator(), rawLhs, rawRhs);
    final Rational<MultivariatePolynomial<BigInteger>> rawDen =
        SubstitutionUtils.substitute(rawRat.denominator(), rawLhs, rawRhs);

    return new RationalFunction(newVariables, rawNum.divide(rawDen));
  }
}
