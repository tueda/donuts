package com.github.tueda.donuts;

import cc.redberry.rings.Rational;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.poly.MultivariateRing;
import cc.redberry.rings.poly.PolynomialMethods;
import cc.redberry.rings.poly.multivar.DegreeVector;
import cc.redberry.rings.poly.multivar.Monomial;
import cc.redberry.rings.poly.multivar.MultivariatePolynomial;
import com.github.tueda.donuts.util.IndexToObjectMap;
import lombok.experimental.UtilityClass;

/** This class consists of static utility methods for substitutions. */
@SuppressWarnings("doclint:missing") // workaround for JDK-8271159
@UtilityClass
public class SubstitutionUtils {
  /**
   * Checks if the given polynomial is valid for a LHS of a substitution.
   *
   * @param lhs the polynomial to be checked
   * @throws IllegalArgumentException when the polynomial is invalid
   */
  public static void checkLhs(final Polynomial lhs) {
    if (!lhs.isMonomial() || lhs.isConstant() || !lhs.isMonic()) {
      throw new IllegalArgumentException("illegal lhs for substitution");
    }
  }

  /**
   * Returns the result of the given substitution.
   *
   * @param poly the input polynomial
   * @param lhs the left-hand side
   * @param rhs the right-hand side
   * @return the resultant polynomial
   */
  public static MultivariatePolynomial<BigInteger> substitute(
      final MultivariatePolynomial<BigInteger> poly,
      final Monomial<BigInteger> lhs,
      final MultivariatePolynomial<BigInteger> rhs) {
    final MultivariatePolynomial<BigInteger> result = poly.createZero();
    final PolynomialPowers powers = new PolynomialPowers(rhs);
    for (final Monomial<BigInteger> term : poly) {
      int n = 0;
      DegreeVector dv = term;
      while (dv.dvDivisibleBy(lhs)) {
        dv = dv.dvDivideExact(lhs);
        n++;
      }
      if (n == 0) {
        result.add(term);
      } else {
        result.add(powers.pow(n).multiply(new Monomial<>(dv, term.coefficient)));
      }
    }
    return result;
  }

  /**
   * Returns the result of the given substitution.
   *
   * @param poly the input polynomial
   * @param lhs the left-hand side
   * @param rhs the right-hand side
   * @return the resultant polynomial
   */
  public static Rational<MultivariatePolynomial<BigInteger>> substitute(
      final MultivariatePolynomial<BigInteger> poly,
      final Monomial<BigInteger> lhs,
      final Rational<MultivariatePolynomial<BigInteger>> rhs) {

    final MultivariateRing<MultivariatePolynomial<BigInteger>> ring =
        RationalFunction.getRings(poly.nVariables);
    Rational<MultivariatePolynomial<BigInteger>> result =
        Rational.<MultivariatePolynomial<BigInteger>>zero(ring);
    final MultivariatePolynomial<BigInteger> spectators = poly.createZero();
    final RationalPowers powers = new RationalPowers(rhs);
    for (final Monomial<BigInteger> term : poly) {
      int n = 0;
      DegreeVector dv = term;
      while (dv.dvDivisibleBy(lhs)) {
        dv = dv.dvDivideExact(lhs);
        n++;
      }
      if (n == 0) {
        spectators.add(term);
      } else {
        result =
            result.add(
                powers
                    .pow(n)
                    .multiply(poly.createOne().multiply(new Monomial<>(dv, term.coefficient))));
      }
    }
    if (spectators.isZero()) {
      return result;
    } else {
      return result.add(spectators);
    }
  }

  /** Cache powers of a raw polynomial object. */
  private static class PolynomialPowers {
    /** The base. */
    private final MultivariatePolynomial<BigInteger> base;

    /** The cache for powers. */
    private final IndexToObjectMap<MultivariatePolynomial<BigInteger>> cache;

    /**
     * Constructs a precomputed cache for powers of the given polynomial.
     *
     * @param base the base polynomial
     */
    public PolynomialPowers(final MultivariatePolynomial<BigInteger> base) {
      this.base = base;
      this.cache = new IndexToObjectMap<>();
    }

    /**
     * Returns the power of the polynomial.
     *
     * @param exponent the exponent
     * @return the result
     */
    public MultivariatePolynomial<BigInteger> pow(final int exponent) {
      MultivariatePolynomial<BigInteger> result = cache.get(exponent);
      if (result == null) {
        result = PolynomialMethods.polyPow(base, exponent, true);
        cache.put(exponent, result);
      }
      return result.copy();
    }
  }

  /** Cache powers of a raw rational function object. */
  private static class RationalPowers {
    /** The base. */
    private final Rational<MultivariatePolynomial<BigInteger>> base;

    /** The cache for powers. */
    private final IndexToObjectMap<Rational<MultivariatePolynomial<BigInteger>>> cache;

    /**
     * Constructs a precomputed cache for powers of the given rational function.
     *
     * @param base the base rational function
     */
    public RationalPowers(final Rational<MultivariatePolynomial<BigInteger>> base) {
      this.base = base;
      this.cache = new IndexToObjectMap<>();
    }

    /**
     * Returns the power of the rational function.
     *
     * @param exponent the exponent
     * @return the result
     */
    public Rational<MultivariatePolynomial<BigInteger>> pow(final int exponent) {
      Rational<MultivariatePolynomial<BigInteger>> result = cache.get(exponent);
      if (result == null) {
        result = base.pow(exponent);
        cache.put(exponent, result);
      }
      return result;
    }
  }
}
