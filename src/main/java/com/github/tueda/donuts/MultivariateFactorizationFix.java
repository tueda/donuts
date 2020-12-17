package com.github.tueda.donuts;

import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.poly.PolynomialFactorDecomposition;
import cc.redberry.rings.poly.multivar.MultivariateDivision;
import cc.redberry.rings.poly.multivar.MultivariateFactorization;
import cc.redberry.rings.poly.multivar.MultivariatePolynomial;
import cc.redberry.rings.poly.multivar.MultivariateSquareFreeFactorization;
import java.io.IOError;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.experimental.UtilityClass;

/**
 * A workaround for the factorization bug in Rings 2.5.7. We access the private methods of Rings and
 * we have to assume that there is no interference from the security manager.
 */
@UtilityClass
public class MultivariateFactorizationFix {

  /** Method object for "factorPrimitiveInZ". */
  private static /* final */ Method factorPrimitiveInZMethod;

  static {
    try {
      factorPrimitiveInZMethod =
          MultivariateFactorization.class.getDeclaredMethod(
              "factorPrimitiveInZ", MultivariatePolynomial.class);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new IOError(e);
    }
    makeAccessible();
  }

  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
      justification = "Temporary workaround until Rings 2.5.8 comes out",
      value = "DP_DO_INSIDE_DO_PRIVILEGED")
  private static void makeAccessible() {
    factorPrimitiveInZMethod.setAccessible(true);
  }

  /* default */ static PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>> factorInZ(
      final MultivariatePolynomial<BigInteger> poly) {
    if (poly.isEffectiveUnivariate()) {
      return MultivariateFactorization.FactorInZ(poly);
    }

    // square-free decomposition
    final PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>> sqf =
        MultivariateSquareFreeFactorization.SquareFreeFactorization(poly);

    // the result
    final PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>> res =
        PolynomialFactorDecomposition.unit(sqf.unit);

    for (int i = 0; i < sqf.size(); i++) {
      final MultivariatePolynomial<BigInteger> factor = sqf.get(i);
      // factor into primitive polynomials
      final PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>> primitiveFactors =
          factorToPrimitive(factor);
      res.addUnit(primitiveFactors.unit, sqf.getExponent(i));
      for (final MultivariatePolynomial<BigInteger> primitiveFactor : primitiveFactors) {
        // factor each primitive polynomial
        final PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>> prFactors =
            factorPrimitiveInZ(primitiveFactor);
        res.addUnit(prFactors.unit, sqf.getExponent(i));
        for (final MultivariatePolynomial<BigInteger> prFactor : prFactors) {
          res.addFactor(prFactor, sqf.getExponent(i));
        }
      }
    }
    return res;
  }

  // Broken in Rings 2.5.7.
  // Fixed in https://github.com/PoslavskySV/rings/commit/59ee8dc90a9798fca2f31a5e092ddcb7f7198a18.
  private static PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>>
      factorToPrimitive(final MultivariatePolynomial<BigInteger> poly) {
    if (poly.isEffectiveUnivariate()) {
      return PolynomialFactorDecomposition.of(poly);
    }
    final PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>> result =
        PolynomialFactorDecomposition.empty(poly);

    MultivariatePolynomial<BigInteger> currentPoly = poly;

    for (int i = 0; i < currentPoly.nVariables; i++) {
      if (currentPoly.degree(i) == 0) {
        continue;
      }
      final MultivariatePolynomial<BigInteger> factor = currentPoly.asUnivariate(i).content();
      result.addAll(factorToPrimitive(factor));
      currentPoly = MultivariateDivision.divideExact(currentPoly, factor);
    }
    result.addFactor(currentPoly, 1);
    return result;
  }

  @SuppressWarnings("unchecked")
  private static PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>>
      factorPrimitiveInZ(final MultivariatePolynomial<BigInteger> polynomial) {
    try {
      return (PolynomialFactorDecomposition<MultivariatePolynomial<BigInteger>>)
          factorPrimitiveInZMethod.invoke(null, polynomial);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new IOError(e);
    }
  }
}
