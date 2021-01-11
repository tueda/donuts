package com.github.tueda.donuts;

import cc.redberry.rings.Rational;
import cc.redberry.rings.Rationals;
import cc.redberry.rings.Rings;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.io.Coder;
import cc.redberry.rings.poly.MultivariateRing;
import cc.redberry.rings.poly.multivar.Monomial;
import cc.redberry.rings.poly.multivar.MultivariatePolynomial;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/** A multivariate rational function. Immutable. */
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

  /** Raw zero rational function. */
  /* default */ static final Rational<MultivariatePolynomial<BigInteger>> RAW_ZERO;

  /** The rational function that equals to zero. */
  public static final RationalFunction ZERO;

  /** The rational function that equals to unity. */
  public static final RationalFunction ONE;

  static {
    assert MAX_VARIABLES >= 0;

    for (int i = 0; i <= MAX_VARIABLES; i++) {
      RAW_RINGS.add(Rings.MultivariateRing(i, Rings.Z));
      RAW_FIELDS.add(Rings.Frac(RAW_RINGS.get(i)));
    }

    RAW_ZERO = new Rational<>(RAW_RINGS.get(0), Polynomial.RAW_ZERO);

    ZERO = new RationalFunction();
    ONE = new RationalFunction(1);
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

  /**
   * Constructs a rational function from the given integer.
   *
   * @param value the integer to be converted to a rational function
   */
  public RationalFunction(final long value) {
    variables = VariableSet.EMPTY;
    raw = new Rational<>(getRings(0), Polynomial.RAW_ZERO.createConstant(value));
  }

  /**
   * Constructs a rational function from the given integer.
   *
   * @param value the integer to be converted to a rational function
   */
  public RationalFunction(final BigInteger value) {
    variables = VariableSet.EMPTY;
    raw = new Rational<>(getRings(0), Polynomial.RAW_ZERO.createConstant(value));
  }

  /**
   * Constructs a rational function from the given polynomial.
   *
   * @param poly the polynomial to be converted to a rational function
   */
  public RationalFunction(final Polynomial poly) {
    variables = poly.getVariables();
    raw = new Rational<>(getRings(variables.size()), poly.getRawPolynomialWithoutCopy());
  }

  /**
   * Constructs a rational function from the given numerator and denominator.
   *
   * @param numerator the numerator
   * @param denominator the denominator
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
   * @param numerator the numerator
   * @param denominator the denominator
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
   * @param numerator the numerator
   * @param denominator the denominator
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction(final Polynomial numerator, final Polynomial denominator) {
    final VariableSet numeratorVariables = numerator.getVariables();
    final VariableSet denominatorVariables = denominator.getVariables();

    if (numeratorVariables.equals(denominatorVariables)) {
      variables = numeratorVariables;
      raw =
          new Rational<>(
              getRings(variables.size()),
              numerator.getRawPolynomialWithoutCopy(),
              denominator.getRawPolynomialWithoutCopy());
    } else {
      variables = numeratorVariables.union(denominatorVariables);
      raw =
          new Rational<>(
              getRings(variables.size()),
              numerator.translate(variables).getRawPolynomialWithoutCopy(),
              denominator.translate(variables).getRawPolynomialWithoutCopy());
    }
  }

  /**
   * Constructs a rational function from the given string.
   *
   * @param string the string to be parsed
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

  private RationalFunction(
      final VariableSet newVariables, final Rational<MultivariatePolynomial<BigInteger>> rawRat) {
    assert newVariables.size() == ((MultivariateRing<?>) rawRat.ring).nVariables();
    checkNumberOfVariables(newVariables.size());
    variables = newVariables;
    raw = rawRat;
  }

  private RationalFunction(
      final VariableSet newVariables,
      final MultivariatePolynomial<BigInteger> rawNum,
      final MultivariatePolynomial<BigInteger> rawDen) {
    final int nVariables = newVariables.size();
    assert nVariables == rawNum.nVariables;
    assert nVariables == rawDen.nVariables;
    if (rawDen.isZero()) {
      throw new ArithmeticException("division by zero");
    }
    variables = newVariables;
    raw = new Rational<>(getRings(nVariables), rawNum, rawDen);
  }

  /* default */ static RationalFunction createFromRaw(
      final VariableSet newVariables, final Rational<MultivariatePolynomial<BigInteger>> rawRat) {
    return new RationalFunction(newVariables, rawRat);
  }

  /**
   * Returns a rational function constructed from the given string.
   *
   * @param string the string to be parsed
   * @return the resultant rational function
   * @throws IllegalArgumentException when {@code string} does not represent a rational function
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static RationalFunction of(final String string) {
    return new RationalFunction(string);
  }

  /**
   * Returns an array of rational functions constructed from the given strings.
   *
   * @param strings the array of strings to be parsed
   * @return the resultant array of rational functions
   * @throws IllegalArgumentException when any of the given strings are invalid for rational
   *     functions
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static RationalFunction[] of(final String... strings) {
    return Stream.of(strings).map(RationalFunction::new).toArray(RationalFunction[]::new);
  }

  /**
   * Performs a replacement in serialization.
   *
   * @return a serialization proxy object
   * @throws ObjectStreamException never thrown
   */
  private Object writeReplace() throws ObjectStreamException {
    // Because Rational is not serializable (PoslavskySV/rings#61), the default serialization fails.
    // A solution is to serialize the numerator and denominator, separately.
    // We delegate the actual implementation to a serialization proxy class.
    return new SerializationProxy(this);
  }

  /**
   * Performs a replacement in deserialization (but always fails, must never called).
   *
   * @return never returns
   * @throws ObjectStreamException never thrown
   * @throws InvalidObjectException always thrown
   */
  private Object readResolve() throws ObjectStreamException {
    throw new InvalidObjectException("Proxy required.");
  }

  /** Serialization proxy class. */
  private static final class SerializationProxy implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The set of variables. */
    private final VariableSet variables;
    /** The numerator. */
    private final MultivariatePolynomial<BigInteger> numerator;
    /** The denominator. */
    private final MultivariatePolynomial<BigInteger> denominator;

    /**
     * Constructor.
     *
     * @param rat the rational function to be serialized
     */
    public SerializationProxy(final RationalFunction rat) {
      variables = rat.variables;
      numerator = rat.raw.numerator();
      denominator = rat.raw.denominator();
    }

    /**
     * Performs a replacement in deserialization.
     *
     * @return the result of the deserialization
     * @throws ObjectStreamException never thrown
     */
    private Object readResolve() throws ObjectStreamException {
      // We need to treat Rings.Z as a singleton. The easiest way to avoid similar singleton issues
      // of RationalFunction is to construct it via two Polynomial objects.
      return new RationalFunction(
          Polynomial.createFromRaw(variables, numerator.setRingUnsafe(Rings.Z)),
          Polynomial.createFromRaw(variables, denominator.setRingUnsafe(Rings.Z)));
    }
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
    final RationalFunction rat = translate(getMinimalVariables());
    return Objects.hash(rat.variables, rat.raw);
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

  /**
   * Returns the raw rational function object of the Rings library.
   *
   * @return the raw rational function object
   */
  public Rational<MultivariatePolynomial<BigInteger>> getRawRational() {
    return new Rational<>(raw.ring, raw.numerator().copy(), raw.denominator().copy());
  }

  /* default */ Rational<MultivariatePolynomial<BigInteger>> getRawRationalWithoutCopy() {
    // !!! Never modify it!!!
    return raw;
  }

  /**
   * Returns {@code true} if this rational function is equal to zero.
   *
   * @return {@code true} if this rational function is {@code 0}
   */
  public boolean isZero() {
    return raw.isZero();
  }

  /**
   * Returns {@code true} if this rational function is equal to unity.
   *
   * @return {@code true} if this rational function is {@code 1}.
   */
  public boolean isOne() {
    return raw.isOne();
  }

  /**
   * Returns {@code true} if this rational function is equal to minus one.
   *
   * @return {@code true} if this rational function is {@code -1}.
   */
  public boolean isMinusOne() {
    return raw.isIntegral() && getNumerator().isMinusOne();
  }

  /**
   * Returns {@code true} if this rational function is constant, i.e., a rational number (including
   * zero).
   *
   * @return {@code true} if this rational function is constant.
   */
  public boolean isConstant() {
    return raw.numerator().isConstant() && raw.denominator().isConstant();
  }

  /**
   * Returns {@code true} if this rational function is an integer.
   *
   * @return {@code true} if this rational function is an integer
   */
  public boolean isInteger() {
    return raw.isIntegral() && raw.numerator().isConstant();
  }

  /**
   * Returns {@code true} if this rational function is a plain variable (the coefficient is one).
   *
   * @return {@code true} if this rational function is a variable
   */
  public boolean isVariable() {
    return raw.isIntegral() && getNumerator().isVariable();
  }

  /**
   * Returns {@code true} if this rational function is a polynomial with integer coefficient.
   *
   * @return {@code true} if this rational function is a polynomial
   */
  public boolean isPolynomial() {
    return raw.isIntegral();
  }

  /**
   * Returns the numerator of this polynomial.
   *
   * @return the numerator
   */
  public Polynomial getNumerator() {
    return Polynomial.createFromRaw(variables, raw.numerator());
  }

  /**
   * Returns the denominator of this polynomial.
   *
   * @return the denominator
   */
  public Polynomial getDenominator() {
    return Polynomial.createFromRaw(variables, raw.denominator());
  }

  /**
   * Returns this rational function in a different variable set.
   *
   * @param newVariables the new variables to be used
   * @return the resultant rational function
   * @throws IllegalArgumentException when any of used variables are not in {@code newVariables}
   */
  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  public RationalFunction translate(final VariableSet newVariables) {
    if (variables == newVariables) {
      return this;
    } else if (variables.equals(newVariables)) {
      return new RationalFunction(newVariables, raw);
    } else {
      return new RationalFunction(
          newVariables,
          new Rational<>(
              getRings(newVariables.size()),
              getNumerator().translate(newVariables).getRawPolynomialWithoutCopy(),
              getDenominator().translate(newVariables).getRawPolynomialWithoutCopy()));
    }
    // Postcondition: `variables` of the returned-value is the given variable set.
  }

  /**
   * Returns the negation of this rational function.
   *
   * @return {@code -this}
   */
  public RationalFunction negate() {
    if (isZero()) {
      return this;
    } else {
      return new RationalFunction(variables, raw.negate());
    }
  }

  /**
   * Returns the reciprocal of this rational function.
   *
   * @return {@code 1/this}
   */
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

  /**
   * Returns the sum of this rational function and the other.
   *
   * @param other the other rational function to be added to this rational function
   * @return {@code this + other}
   */
  public RationalFunction add(final RationalFunction other) {
    return performBinaryOperation(other, Rational<MultivariatePolynomial<BigInteger>>::add);
  }

  /**
   * Returns the difference of this rational function from the other.
   *
   * @param other the other rational function to be subtracted from this rational function
   * @return {@code this - other}
   */
  public RationalFunction subtract(final RationalFunction other) {
    return performBinaryOperation(other, Rational<MultivariatePolynomial<BigInteger>>::subtract);
  }

  /**
   * Returns the product of this rational function and the other.
   *
   * @param other the other rational function to be multiplied by this rational function
   * @return {@code this * other}
   */
  public RationalFunction multiply(final RationalFunction other) {
    return performBinaryOperation(other, Rational<MultivariatePolynomial<BigInteger>>::multiply);
  }

  /**
   * Returns the quotient of this rational function divided by the given divisor.
   *
   * @param divisor the divisor
   * @return {@code this / divisor}
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction divide(final RationalFunction divisor) {
    return performBinaryOperation(divisor, Rational<MultivariatePolynomial<BigInteger>>::divide);
  }

  /**
   * Returns this rational function raised to the given power.
   *
   * @param exponent the exponent to which this rational function is to be raised
   * @return {@code this ^ exponent}
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction pow(final int exponent) {
    return new RationalFunction(variables, raw.pow(exponent));
  }

  /**
   * Returns this rational function raised to the given power.
   *
   * @param exponent the exponent to which this rational function is to be raised
   * @return {@code this ^ exponent}
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction pow(final BigInteger exponent) {
    return new RationalFunction(variables, raw.pow(exponent));
  }

  /**
   * Returns the result of the given substitution. The left-hand side must be a non-constant monic
   * monomial.
   *
   * @param lhs the left-hand side
   * @param rhs the right-hand side
   * @return the resultant rational function
   * @throws IllegalArgumentException when {@code lhs} is invalid
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

  /**
   * Returns a copy of this rational function with setting the given variable to the specified
   * value.
   *
   * @param variable the variable to be set
   * @param value the value
   * @return a copy of {@code this} with {@code variable -> value}
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction evaluate(final Variable variable, final int value) {
    final int i = variables.indexOf(variable);
    if (i < 0) {
      return this;
    }
    final BigInteger newValue = BigInteger.valueOf(value);
    return new RationalFunction(
        this.variables,
        raw.numerator().evaluate(i, newValue),
        raw.denominator().evaluate(i, newValue));
  }

  /**
   * Returns a copy of this rational function with setting the given variable to the specified
   * value.
   *
   * @param variable the variable to be set
   * @param value the value
   * @return a copy of {@code this} with {@code variable -> value}
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction evaluate(final Variable variable, final BigInteger value) {
    final int i = variables.indexOf(variable);
    if (i < 0) {
      return this;
    }
    return new RationalFunction(
        this.variables, raw.numerator().evaluate(i, value), raw.denominator().evaluate(i, value));
  }

  /**
   * Returns a copy of this rational function with setting the given variables to the specified
   * values.
   *
   * @param variables the variables to be set
   * @param values the values
   * @return a copy of {@code this} with {@code variables -> values}
   * @throws ArithmeticException when division by zero
   * @throws IllegalArgumentException when {@code variables} and {@code values} have different
   *     lengths
   */
  @SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.UseVarargs"})
  public RationalFunction evaluate(final Variable[] variables, final int[] values) {
    final Object[] result = this.variables.findIndicesForVariablesAndValues(variables, values);

    final int[] indices = (int[]) result[0];

    if (indices.length == 0) {
      return this;
    }

    final BigInteger[] newValues = (BigInteger[]) result[1];

    return new RationalFunction(
        this.variables,
        raw.numerator().evaluate(indices, newValues),
        raw.denominator().evaluate(indices, newValues));
  }

  /**
   * Returns a copy of this rational function with setting the given variables to the specified
   * values.
   *
   * @param variables the variables to be set
   * @param values the values
   * @return a copy of {@code this} with {@code variables -> values}
   * @throws ArithmeticException when division by zero
   * @throws IllegalArgumentException when {@code variables} and {@code values} have different
   *     lengths
   */
  @SuppressWarnings("PMD.UseVarargs")
  public RationalFunction evaluate(final Variable[] variables, final BigInteger[] values) {
    final Object[] result = this.variables.findIndicesForVariablesAndValues(variables, values);

    final int[] indices = (int[]) result[0];

    if (indices.length == 0) {
      return this;
    }

    final BigInteger[] newValues = (BigInteger[]) result[1];

    return new RationalFunction(
        this.variables,
        raw.numerator().evaluate(indices, newValues),
        raw.denominator().evaluate(indices, newValues));
  }

  /**
   * Returns the rational function with setting the given variable to zero.
   *
   * @param variable the variable to be set to zero
   * @return a copy of {@code this} with {@code variable -> 0}
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction evaluateAtZero(final Variable variable) {
    final int i = variables.indexOf(variable);
    if (i < 0) {
      return this;
    }
    return new RationalFunction(
        this.variables, raw.numerator().evaluateAtZero(i), raw.denominator().evaluateAtZero(i));
  }

  /**
   * Returns a copy of this rational function with setting all of the given variable to zero.
   *
   * @param variables the variables to be set to zero
   * @return a copy of {@code this} with {@code variables -> 0}
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction evaluateAtZero(final VariableSet variables) {
    final int[] indices = this.variables.findIndicesForVariableSet(variables);

    if (indices.length == 0) {
      return this;
    }

    return new RationalFunction(
        this.variables,
        raw.numerator().evaluateAtZero(indices),
        raw.denominator().evaluateAtZero(indices));
  }

  /**
   * Returns a copy of this rational function with setting the given variable to unity.
   *
   * @param variable the variable to be set to unity
   * @return a copy of {@code this} with {@code variable -> 1}
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction evaluateAtOne(final Variable variable) {
    final int i = variables.indexOf(variable);
    if (i < 0) {
      return this;
    }
    return new RationalFunction(
        this.variables,
        raw.numerator().evaluate(i, BigInteger.ONE),
        raw.denominator().evaluate(i, BigInteger.ONE));
  }

  /**
   * Returns a copy of this rational function with setting all of the given variables to unity.
   *
   * @param variables the variables to be set to unity
   * @return a copy of {@code this} with {@code variables -> 1}
   * @throws ArithmeticException when division by zero
   */
  public RationalFunction evaluateAtOne(final VariableSet variables) {
    final int[] indices = this.variables.findIndicesForVariableSet(variables);

    if (indices.length == 0) {
      return this;
    }

    final BigInteger[] values = new BigInteger[indices.length];
    Arrays.fill(values, BigInteger.ONE);

    return new RationalFunction(
        this.variables,
        raw.numerator().evaluate(indices, values),
        raw.denominator().evaluate(indices, values));
  }

  /**
   * Returns a copy of this rational function with the given variable shift.
   *
   * @param variable the variable to be shifted
   * @param shift the shift amount
   * @return a copy of {@code this} with {@code variable -> variable + shift}
   */
  public RationalFunction shift(final Variable variable, final int shift) {
    final int i = variables.indexOf(variable);
    if (i < 0) {
      return this;
    }
    final BigInteger newShift = BigInteger.valueOf(shift);
    return new RationalFunction(
        variables, raw.numerator().shift(i, newShift), raw.denominator().shift(i, newShift));
  }

  /**
   * Returns a copy of this rational function with the given variable shift.
   *
   * @param variable the variable to be shifted
   * @param shift the shift amount
   * @return a copy of {@code this} with {@code variable -> variable + shift}
   */
  public RationalFunction shift(final Variable variable, final BigInteger shift) {
    final int i = variables.indexOf(variable);
    if (i < 0) {
      return this;
    }
    return new RationalFunction(
        variables, raw.numerator().shift(i, shift), raw.denominator().shift(i, shift));
  }

  /**
   * Returns a copy of this rational function with the given variable shifts.
   *
   * @param variables the variables to be shifted
   * @param shifts the shift amounts
   * @return a copy of {@code this} with {@code variables -> variables + shifts}
   */
  @SuppressWarnings("PMD.UseVarargs")
  public RationalFunction shift(final Variable[] variables, final int[] shifts) {
    final Object[] result =
        this.variables.findIndicesForVariablesAndValues(variables, shifts, "shifts");

    final int[] indices = (int[]) result[0];

    if (indices.length == 0) {
      return this;
    }

    final BigInteger[] newShifts = (BigInteger[]) result[1];

    return new RationalFunction(
        this.variables,
        raw.numerator().shift(indices, newShifts),
        raw.denominator().shift(indices, newShifts));
  }

  /**
   * Returns a copy of this rational function with the given variable shifts.
   *
   * @param variables the variables to be shifted
   * @param shifts the shift amounts
   * @return a copy of {@code this} with {@code variables -> variables + shifts}
   */
  @SuppressWarnings("PMD.UseVarargs")
  public RationalFunction shift(final Variable[] variables, final BigInteger[] shifts) {
    final Object[] result =
        this.variables.findIndicesForVariablesAndValues(variables, shifts, "shifts");

    final int[] indices = (int[]) result[0];

    if (indices.length == 0) {
      return this;
    }

    final BigInteger[] newShifts = (BigInteger[]) result[1];

    return new RationalFunction(
        this.variables,
        raw.numerator().shift(indices, newShifts),
        raw.denominator().shift(indices, newShifts));
  }

  /**
   * Returns the partial derivative with respect to the given variable.
   *
   * @param variable the variable
   * @return the resultant rational function
   */
  public RationalFunction derivative(final Variable variable) {
    return derivative(variable, 1);
  }

  /**
   * Returns the partial derivative of the specified order with respect to the given variable.
   *
   * @param variable the variable
   * @param order the order
   * @return the resultant rational function
   * @throws IllegalArgumentException when {@code order} is negative
   */
  public RationalFunction derivative(final Variable variable, final int order) {
    if (order < 0) {
      throw new IllegalArgumentException(String.format("Negative order given: %s", order));
    }
    if (order == 0) {
      return this;
    }

    final int i = variables.indexOf(variable);
    if (i < 0) {
      return RationalFunction.ZERO;
    }

    Rational<MultivariatePolynomial<BigInteger>> r = raw;

    for (int j = 0; j < order; j++) {
      r = derivativeImpl(r, i);
      if (r == null) {
        return RationalFunction.ZERO;
      }
    }

    return new RationalFunction(variables, r);
  }

  private static Rational<MultivariatePolynomial<BigInteger>> derivativeImpl(
      final Rational<MultivariatePolynomial<BigInteger>> r, final int variable) {
    final MultivariatePolynomial<BigInteger> p = r.numerator();
    final MultivariatePolynomial<BigInteger> q = r.denominator();

    final MultivariatePolynomial<BigInteger> p1 = p.derivative(variable);
    final MultivariatePolynomial<BigInteger> q1 = q.derivative(variable);

    if (p1.isZero()) {
      if (q1.isZero()) {
        // p' == 0, q' == 0.
        return null;
      } else {
        // p' == 0, q' != 0.
        return new Rational<>(r.ring, q1.multiply(p).negate(), q.copy().multiply(q));
      }
    } else {
      if (q1.isZero()) {
        // p' != 0, q' == 0.
        return new Rational<>(r.ring, p1, q);
      } else {
        // p' != 0, q' != 0.
        final Rational<MultivariatePolynomial<BigInteger>> term1 = new Rational<>(r.ring, p1, q);
        final Rational<MultivariatePolynomial<BigInteger>> term2 =
            new Rational<>(r.ring, q1.multiply(p).negate(), q.copy().multiply(q));
        return term1.add(term2);
      }
    }
  }
}
