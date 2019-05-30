package com.github.tueda.donuts;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Immutable set of variables. */
public final class VariableSet extends AbstractSet<Variable> implements Serializable {
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
   * Constructs a set of variables.
   *
   * @param variables the set of variables.
   */
  public VariableSet(final Variable... variables) {
    super();
    final Stream<String> stream = Stream.of(variables).map(x -> x.toString());
    table = stream.sorted(Variable.NAME_COMPARATOR).distinct().toArray(String[]::new);
  }

  /**
   * Constructs a set of variables.
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
   * Constructs a set of variables.
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
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static VariableSet of(final String name) {
    return new VariableSet(Stream.of(name).map(Variable::new));
  }

  /**
   * Returns a set of variables from the given names.
   *
   * @param names the set of names.
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

  /* default */ String[] getTable() {
    return table.clone();
  }
}
