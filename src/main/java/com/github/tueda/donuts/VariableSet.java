package com.github.tueda.donuts;

import cc.redberry.rings.bigint.BigInteger;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** A set of variables. Immutable. */
public final class VariableSet extends AbstractSet<Variable> implements Serializable, Multivariate {
  private static final long serialVersionUID = 1L;

  /** Empty variable set. */
  /* default */ static final VariableSet EMPTY = new VariableSet();

  /** The table for variables, already sorted and distinct. */
  private final String[] table;

  /** Constructs an empty set of variables. */
  public VariableSet() {
    super();
    table = new String[0];
  }

  /**
   * Constructs a set of variables from the given variable.
   *
   * @param variable the variable
   */
  public VariableSet(final Variable variable) {
    super();
    table = new String[] {variable.getName()};
  }

  /**
   * Constructs a set of variables from the given variables.
   *
   * @param variables the set of variables
   */
  public VariableSet(final Variable... variables) {
    super();
    final Stream<String> stream = Stream.of(variables).map(x -> x.toString());
    table = stream.sorted(Variable.NAME_COMPARATOR).distinct().toArray(String[]::new);
  }

  /**
   * Constructs a set of variables from the given variables.
   *
   * @param variables the set of variables
   */
  public VariableSet(final Iterable<Variable> variables) {
    super();
    final Stream<String> stream =
        StreamSupport.stream(variables.spliterator(), false).map(x -> x.toString());
    table = stream.sorted(Variable.NAME_COMPARATOR).distinct().toArray(String[]::new);
  }

  /**
   * Constructs a set of variables from the given variables.
   *
   * @param variables the set of variables
   */
  public VariableSet(final Stream<Variable> variables) {
    super();
    final Stream<String> stream = variables.map(x -> x.toString());
    table = stream.sorted(Variable.NAME_COMPARATOR).distinct().toArray(String[]::new);
  }

  @SuppressWarnings("PMD.ArrayIsStoredDirectly")
  private VariableSet(final String... rawTable) {
    super();
    this.table = rawTable;
  }

  private static VariableSet createFromVariableNames(final Stream<String> stream) {
    return new VariableSet(
        stream.sorted(Variable.NAME_COMPARATOR).distinct().toArray(String[]::new));
  }

  /* default */ static VariableSet createFromRaw(final String... rawTable) {
    return new VariableSet(rawTable);
  }

  /**
   * Returns a set of variables from the given name.
   *
   * @param name the name
   * @return a set of variables constructed from the given name
   * @throws IllegalArgumentException when the given names is illegal for variables
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static VariableSet of(final String name) {
    return new VariableSet(new Variable(name));
  }

  /**
   * Returns a set of variables from the given names.
   *
   * @param names the set of names
   * @return a set of variables constructed from the given names
   * @throws IllegalArgumentException when any of the given names are illegal for variables
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static VariableSet of(final String... names) {
    return new VariableSet(Stream.of(names).map(Variable::new));
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof VariableSet)) {
      return false;
    }
    final VariableSet aVariableSet = (VariableSet) other;
    return Arrays.equals(table, aVariableSet.table);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(table);
  }

  @Override
  public String toString() {
    return "{" + Stream.of(table).collect(Collectors.joining(", ")) + "}";
  }

  @Override
  public int size() {
    return table.length;
  }

  @Override
  public Iterator<Variable> iterator() {
    return Stream.of(table).map(x -> Variable.createWithoutCheck(x)).iterator();
  }

  @Override
  public boolean contains(final Object o) {
    if (!(o instanceof Variable)) {
      return false;
    }
    final Variable x = (Variable) o;
    return Arrays.binarySearch(table, x.getName(), Variable.NAME_COMPARATOR) >= 0;
  }

  @Override
  public VariableSet getVariables() {
    return this;
  }

  /**
   * Returns {@code true} if this variable set and the other have any intersection.
   *
   * @param other The set of variables to be checked
   * @return {@code true} if there is an intersection for this set and the other
   */
  public boolean intersects(final VariableSet other) {
    return !Collections.disjoint(this, other);
  }

  /**
   * Returns the intersection of this variable set and the other.
   *
   * @param other The set of variables with which the intersection is taken
   * @return the intersection for this set and the other
   */
  public VariableSet intersection(final VariableSet other) {
    if (this.equals(other)) {
      return this;
    }

    if (isEmpty()) {
      return this;
    }

    if (other.isEmpty()) {
      return other;
    }

    final List<String> list = new ArrayList<>();

    for (final Variable x : other) {
      if (contains(x)) {
        list.add(x.getName());
      }
    }

    final VariableSet newVariables = new VariableSet(list.toArray(new String[0]));

    if (this.equals(newVariables)) {
      return this;
    }

    if (other.equals(newVariables)) {
      return other;
    }

    return newVariables;
  }

  /**
   * Returns the union of this variable set and the other.
   *
   * @param other The set of variables to be united
   * @return the union of this set and the other
   */
  public VariableSet union(final VariableSet other) {
    if (this.equals(other)) {
      return this;
    }

    if (this.isEmpty()) {
      return other;
    }

    if (other.isEmpty()) {
      return this;
    }

    final VariableSet newVariables =
        VariableSet.createFromVariableNames(
            Stream.concat(Stream.of(table), Stream.of(other.table)));

    if (this.equals(newVariables)) {
      return this;
    }

    if (other.equals(newVariables)) {
      return other;
    }

    return newVariables;
  }

  /**
   * Returns the least common set that containing all the variables in the given objects.
   *
   * @param objects the objects to be examined
   * @return the least common set of the variables in the objects
   */
  public static VariableSet unionOf(final Multivariate... objects) {
    return unionOf(Stream.of(objects));
  }

  /**
   * Returns the least common set that containing all the variables in the given objects.
   *
   * @param objects the objects to be examined
   * @return the least common set of the variables in the objects
   */
  public static VariableSet unionOf(final Iterable<Multivariate> objects) {
    return unionOf(StreamSupport.stream(objects.spliterator(), false));
  }

  /**
   * Returns the least common set that containing all the variables in the given objects.
   *
   * @param objects the objects to be examined
   * @return the least common set of the variables in the objects
   */
  public static VariableSet unionOf(final Stream<Multivariate> objects) {
    final Optional<VariableSet> result =
        objects.map(obj -> obj.getVariables()).reduce((n1, n2) -> n1.union(n2));
    if (result.isPresent()) {
      return result.get();
    } else {
      return EMPTY;
    }
  }

  /**
   * Returns the mapping of the variables to those in the other, or {@code null} when no mapping
   * exists. The returned non-null array contains the mapping in such a way that {@code a[i] = j}
   * (for {@code 0 <= i < this.size()}) indicating the i-th variable of the current set is mapped to
   * the j-th variable of the other. The mapping is injective but may be not surjective.
   *
   * @param other The target set of variables
   * @return the mapping of the variables, or {@code null} when the given variable set does not
   *     contain all variables in the current set
   */
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
      justification = "Returning null indicates no mapping exists",
      value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS")
  public int[] map(final VariableSet other) {
    final int[] mapping = new int[table.length];
    int j = 0;
    for (int i = 0; i < mapping.length; i++) {
      j =
          Arrays.binarySearch(
              other.table, j, other.table.length, table[i], Variable.NAME_COMPARATOR);
      if (j < 0) {
        return null;
      }
      mapping[i] = j++;
    }
    return mapping;
  }

  /**
   * Returns the mapping of the variables to those in the other. The returned array contains the
   * mapping in such a way that {@code a[i] = j} (for {@code 0 <= i < this.size()}) indicating the
   * i-th variable of the current set is mapped to the j-th variable of the other. When any variable
   * in the current set is not contained in the other, then the variable is mapped to {@code
   * defaultIndex}, so the mapping may be neither injective nor surjective.
   *
   * @param other The target set of variables
   * @param defaultIndex is used as the default image of any variables that are not contained in the
   *     other set
   * @return the mapping of the variables
   */
  public int[] map(final VariableSet other, final int defaultIndex) {
    final int[] mapping = new int[table.length];
    int j = 0;
    for (int i = 0; i < mapping.length; i++) {
      final int k =
          Arrays.binarySearch(
              other.table, j, other.table.length, table[i], Variable.NAME_COMPARATOR);
      if (k < 0) {
        mapping[i] = defaultIndex;
      } else {
        mapping[i] = k;
        j = k + 1;
      }
    }
    return mapping;
  }

  @SuppressWarnings("PMD.MethodReturnsInternalArray")
  /* default */ String[] getRawTable() {
    // !!! Never modify it!!!
    return table;
  }

  /* default */ String getRawName(final int index) {
    return table[index];
  }

  /* default */ int indexOf(final String rawVariable) {
    return Arrays.binarySearch(table, rawVariable, Variable.NAME_COMPARATOR);
  }

  /* default */ int indexOf(final String rawVariable, final int fromIndex) {
    return Arrays.binarySearch(
        table, fromIndex, table.length, rawVariable, Variable.NAME_COMPARATOR);
  }

  /* default */ int indexOf(final Variable variable) {
    return indexOf(variable.getName());
  }

  /**
   * Return an array for indices corresponding to the given set of variables. Variables that do not
   * exist are just ignored in the resultant index array.
   *
   * @param variables the set of variables to be examined
   * @return an array storing the indices
   */
  /* default */ int[] findIndicesForVariableSet(final VariableSet variables) {
    final int[] indices = new int[variables.size()];
    int n = 0;

    int i = 0;
    for (final String x : variables.getRawTable()) {
      final int j = indexOf(x, i);
      if (j >= 0) {
        indices[n++] = j;
        i = j + 1;
        if (i >= size()) {
          break;
        }
      }
    }

    return Arrays.copyOfRange(indices, 0, n);
  }

  /**
   * Returns indices and the corresponding values for the given variables and values. Variables that
   * do not exist are just ignored in the return value.
   *
   * @param variables the variables to be examined
   * @param values the values paired with {@code variables}
   * @return an object array consisting of the indices ({@code (int[]) returnValue[0]}) and the
   *     corresponding values ({@code (BigInteger[]) returnValue[1]})
   * @throws IllegalArgumentException when {@code variables} and {@code values} have different
   *     lengths
   */
  @SuppressWarnings("PMD.UseVarargs")
  /* default */ Object[] findIndicesForVariablesAndValues(
      final Variable[] variables, final int[] values) {
    return findIndicesForVariablesAndValues(variables, values, "values");
  }

  /**
   * Returns indices and the corresponding values for the given variables and values. Variables that
   * do not exist are just ignored in the return value.
   *
   * @param variables the variables to be examined
   * @param values the values paired with {@code variables}
   * @param valuesName the string to be used when an exception occurs
   * @return an object array consisting of the indices ({@code (int[]) returnValue[0]}) and the
   *     corresponding values ({@code (BigInteger[]) returnValue[1]})
   * @throws IllegalArgumentException when {@code variables} and {@code values} have different
   *     lengths
   */
  /* default */ Object[] findIndicesForVariablesAndValues(
      final Variable[] variables, final int[] values, final String valuesName) {
    if (variables.length != values.length) {
      throw new IllegalArgumentException("sizes of variables and " + valuesName + " unmatch");
    }

    final int len = variables.length;
    final int[] indices = new int[len];
    final BigInteger[] newValues = new BigInteger[len];
    int n = 0;

    for (int i = 0; i < len; i++) {
      final int j = indexOf(variables[i]);
      if (j >= 0) {
        indices[n] = j;
        newValues[n++] = BigInteger.valueOf(values[i]);
      }
    }

    final Object[] result = new Object[2];
    result[0] = Arrays.copyOfRange(indices, 0, n);
    result[1] = Arrays.copyOfRange(newValues, 0, n);
    return result;
  }

  /**
   * Returns indices and the corresponding values for the given variables and values. Variables that
   * do not exist are just ignored in the return value.
   *
   * @param variables the variables to be examined
   * @param values the values paired with {@code variables}
   * @return an object array consisting of the indices ({@code (int[]) returnValue[0]}) and the
   *     corresponding values ({@code (BigInteger[]) returnValue[1]})
   * @throws IllegalArgumentException when {@code variables} and {@code values} have different
   *     lengths
   */
  @SuppressWarnings("PMD.UseVarargs")
  /* default */ Object[] findIndicesForVariablesAndValues(
      final Variable[] variables, final BigInteger[] values) {
    return findIndicesForVariablesAndValues(variables, values, "values");
  }

  /**
   * Returns indices and the corresponding values for the given variables and values. Variables that
   * do not exist are just ignored in the return value.
   *
   * @param variables the variables to be examined
   * @param values the values paired with {@code variables}
   * @param valuesName the string to be used when an exception occurs
   * @return an object array consisting of the indices ({@code (int[]) returnValue[0]}) and the
   *     corresponding values ({@code (BigInteger[]) returnValue[1]})
   * @throws IllegalArgumentException when {@code variables} and {@code values} have different
   *     lengths
   */
  /* default */ Object[] findIndicesForVariablesAndValues(
      final Variable[] variables, final BigInteger[] values, final String valuesName) {
    if (variables.length != values.length) {
      throw new IllegalArgumentException("sizes of variables and " + valuesName + " unmatch");
    }

    final int len = variables.length;
    final int[] indices = new int[len];
    final BigInteger[] newValues = new BigInteger[len];
    int n = 0;

    for (int i = 0; i < len; i++) {
      final int j = indexOf(variables[i]);
      if (j >= 0) {
        indices[n] = j;
        newValues[n++] = values[i];
      }
    }

    final Object[] result = new Object[2];
    result[0] = Arrays.copyOfRange(indices, 0, n);
    result[1] = Arrays.copyOfRange(newValues, 0, n);
    return result;
  }
}
