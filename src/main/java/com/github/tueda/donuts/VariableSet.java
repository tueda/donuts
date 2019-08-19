package com.github.tueda.donuts;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** An immutable set of variables. */
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
   * @param variable the variable.
   */
  public VariableSet(final Variable variable) {
    super();
    table = new String[] {variable.getName()};
  }

  /**
   * Constructs a set of variables from the given variables.
   *
   * @param variables the set of variables.
   */
  public VariableSet(final Variable... variables) {
    super();
    final Stream<String> stream = Stream.of(variables).map(x -> x.toString());
    table = stream.sorted(Variable.NAME_COMPARATOR).distinct().toArray(String[]::new);
  }

  /**
   * Constructs a set of variables from the given variables.
   *
   * @param variables the set of variables.
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
   * @param variables the set of variables.
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

  private static VariableSet fromVariableNames(final Stream<String> stream) {
    return new VariableSet(
        stream.sorted(Variable.NAME_COMPARATOR).distinct().toArray(String[]::new));
  }

  /* default */ static VariableSet createVariableSetFromRawArray(final String... rawTable) {
    return new VariableSet(rawTable);
  }

  /**
   * Returns a set of variables from the given name.
   *
   * @param name the name.
   * @throws IllegalArgumentException when the given names is illegal for variables.
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static VariableSet of(final String name) {
    return new VariableSet(new Variable(name));
  }

  /**
   * Returns a set of variables from the given names.
   *
   * @param names the set of names.
   * @throws IllegalArgumentException when any of the given names are illegal for variables.
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
    return Stream.of(table).map(x -> Variable.createVariableWithoutCheck(x)).iterator();
  }

  @Override
  public VariableSet getVariables() {
    return this;
  }

  /**
   * Returns the union of this variable set and the other.
   *
   * @param other The set of variables to be united.
   */
  public VariableSet union(final VariableSet other) {
    if (this.equals(other)) {
      return this;
    }

    final VariableSet newVariables =
        VariableSet.fromVariableNames(Stream.concat(Stream.of(table), Stream.of(other.table)));

    if (this.equals(newVariables)) {
      return this;
    }
    if (other.equals(newVariables)) {
      return other;
    }

    return newVariables;
  }

  /** Returns the least common set that containing all the variables in the given objects. */
  public static VariableSet union(final Multivariate... objects) {
    return union(Stream.of(objects));
  }

  /** Returns the least common set that containing all the variables in the given objects. */
  public static VariableSet union(final Iterable<Multivariate> objects) {
    return union(StreamSupport.stream(objects.spliterator(), false));
  }

  /** Returns the least common set that containing all the variables in the given objects. */
  public static VariableSet union(final Stream<Multivariate> objects) {
    final Optional<VariableSet> result =
        objects.map(obj -> obj.getVariables()).reduce((n1, n2) -> n1.union(n2));
    if (result.isPresent()) {
      return result.get();
    } else {
      return EMPTY;
    }
  }

  /**
   * Returns the mapping of the variables to those in the other.
   *
   * @param other The target set of variables.
   * @throws IllegalArgumentException when the given variable set does not contain all variables in
   *     this set.
   */
  public int[] map(final VariableSet other) {
    final int[] mapping = new int[table.length];
    for (int i = 0; i < mapping.length; i++) {
      final int j = Arrays.binarySearch(other.table, table[i]);
      if (j < 0) {
        throw new IllegalArgumentException(
            String.format(
                "Failed to map %s from %s to %s", table[i], toString(), other.toString()));
      }
      mapping[i] = j;
    }
    return mapping;
  }

  /* default */ String[] getRawTable() {
    return table.clone();
  }

  /* default */ Iterable<String> getRawIterable() {
    return Arrays.asList(table);
  }

  /* default */ int indexOf(final String rawVariable) {
    return Arrays.binarySearch(table, rawVariable);
  }

  /* default */ int indexOf(final String rawVariable, final int fromIndex) {
    return Arrays.binarySearch(table, fromIndex, table.length, rawVariable);
  }
}
