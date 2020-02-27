package com.github.tueda.donuts;

/** An object that contains several variables. */
public interface Multivariate {
  /**
   * Returns the variable set in this object.
   *
   * @return the variables in this object
   */
  VariableSet getVariables();

  /**
   * Returns the actually used variable set.
   *
   * @return the variables actually used in this object
   */
  default VariableSet getMinimalVariables() {
    return VariableSet.EMPTY;
  }
}
