package com.github.tueda.donuts;

import cc.redberry.rings.Rings;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.poly.multivar.MonomialOrder;
import cc.redberry.rings.poly.multivar.MultivariatePolynomial;
import java.io.Serializable;
import java.util.Objects;

/** An immutable multivariate polynomial. */
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

  /** Constructs a polynomial from the given string. */
  public Polynomial(final String string) {
    final String[] names = Variable.guessVariableNames(string);
    variables = VariableSet.createVariableSetFromRawArray(names);
    raw = MultivariatePolynomial.parse(string, names);
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
    // TODO: variables
    return raw.equals(aPoly.raw);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variables, raw);
  }

  @Override
  public String toString() {
    return raw.toString(variables.getTable());
  }
}
