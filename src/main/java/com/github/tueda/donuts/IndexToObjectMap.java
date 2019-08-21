package com.github.tueda.donuts;

import java.util.ArrayList;

/** An object that maps indices to values. */
public final class IndexToObjectMap<T> {
  /** The table to store mappings. */
  @SuppressWarnings("PMD.LooseCoupling") // we want ArrayList.ensureCapacity().
  private final ArrayList<T> table = new ArrayList<>();

  /** Constructs a map. */
  public IndexToObjectMap() {
    // Do nothing.
  }

  /**
   * Returns the value specified by the given index, or {@code null} if the index has no mapping.
   *
   * @param index whose associated value is to be returned
   * @return the value to which the specified index is mapped, or null if no mapping is stored
   */
  public T get(final int index) {
    ensureIndex(index);
    return table.get(index);
  }

  /**
   * Associates the specified value with the specified index.
   *
   * @param index with which the specified value is to be associated
   * @param value to be associated to the index
   */
  public void put(final int index, final T value) {
    ensureIndex(index);
    table.set(index, value);
  }

  private void ensureIndex(final int minIndex) {
    if (minIndex >= table.size()) {
      table.ensureCapacity(minIndex + 1);
      for (int i = table.size(); i <= minIndex; i++) {
        table.add(null);
      }
    }
  }
}
