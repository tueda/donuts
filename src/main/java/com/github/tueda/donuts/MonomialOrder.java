package com.github.tueda.donuts;

import cc.redberry.rings.poly.multivar.DegreeVector;
import java.util.Comparator;

/** A monomial order. */
public final class MonomialOrder {
  /** Lexicographic monomial order. */
  public static final MonomialOrder LEX =
      new MonomialOrder(cc.redberry.rings.poly.multivar.MonomialOrder.LEX);

  /** Antilexicographic monomial order. */
  public static final MonomialOrder ALEX =
      new MonomialOrder(cc.redberry.rings.poly.multivar.MonomialOrder.ALEX);

  /** Graded lexicographic monomial order. */
  public static final MonomialOrder GRLEX =
      new MonomialOrder(cc.redberry.rings.poly.multivar.MonomialOrder.GRLEX);

  /** Graded reverse lexicographic monomial order (default). */
  public static final MonomialOrder GREVLEX =
      new MonomialOrder(cc.redberry.rings.poly.multivar.MonomialOrder.GREVLEX);

  /** Raw monomial order. */
  /* default */ final Comparator<DegreeVector> raw;

  /**
   * Constructs a monomial order.
   *
   * @param raw the raw monomial order
   */
  /* default */ MonomialOrder(final Comparator<DegreeVector> raw) {
    this.raw = raw;
  }
}
