package com.github.tueda.donuts;

/** An object that contains several variables. */
public interface Multivariate {
  /** Returns the variable set in this object. */
  VariableSet getVariables();

  /** Returns the actually used variable set. */
  default VariableSet getMinimalVariables() {
    return VariableSet.EMPTY;
  }
}
