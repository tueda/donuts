package com.github.tueda.donuts;

import java.io.Serializable;
import java.util.Comparator;

/** Variable. */
public final class Variable implements Comparable<Variable>, Serializable {
  private static final long serialVersionUID = 1L;

  /** The variable name. */
  private final String name;

  /**
   * Constructs a variable with the given name.
   *
   * @param name the name of the variable.
   */
  public Variable(String name) {
    if (!isLegalName(name)) {
      throw new IllegalArgumentException(String.format("illegal variable name: %s", name));
    }
    this.name = name;
  }

  /**
   * Returns whether the given name is legal for variables.
   *
   * @param name the name to be tested.
   */
  public static boolean isLegalName(String name) {
    if (name.isEmpty()) {
      return false;
    }
    if (!Character.isLetter(name.charAt(0))) {
      return false;
    }
    for (int i = 1; i < name.length(); i++) {
      if (!Character.isLetterOrDigit(name.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Variable)) {
      return false;
    }
    Variable other = (Variable) obj;
    return name.equals(other.name);
  }

  @Override
  public int compareTo(Variable other) {
    if (this == other) {
      return 0;
    }
    return comparator.compare(name, other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }

  /** The comparator for variable names. */
  static final Comparator<String> comparator = new NameComparator();

  /** Comparator class for variable names. */
  public static class NameComparator implements Serializable, Comparator<String> {
    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
        justification = "Intend to compare two references",
        value = "ES_COMPARING_PARAMETER_STRING_WITH_EQ")
    public int compare(String s1, String s2) {
      // First, compare the references as a shortcut.
      if (s1 == s2) {
        return 0;
      }

      // This is more or less "natural order numerical sorting".

      int len1 = s1.length();
      int len2 = s2.length();
      int limit = Math.min(len1, len2);

      int i = 0;
      while (i < limit) {
        char c1 = s1.charAt(i);
        char c2 = s2.charAt(i);
        if (Character.isDigit(c1) && Character.isDigit(c2)) {
          // numeric order
          int result = compareNumbers(s1, s2, i);
          if (result != 0) {
            return result;
          }
          i = skipDigits(s1, i);
        } else {
          // lexicographical order
          if (c1 != c2) {
            return c1 - c2;
          }
          i++;
        }
      }

      // Now, compare the length. Shorter is smaller.
      return len1 - len2;
    }

    private static int compareNumbers(String s1, String s2, int first) {
      int j1 = skipZeros(s1, first);
      int j2 = skipZeros(s2, first);
      int k1 = skipDigits(s1, j1);
      int k2 = skipDigits(s2, j2);

      // Compare the numbers of non-zero digits. Shorter is smaller.
      if (k1 - j1 != k2 - j2) {
        return (k1 - j1) - (k2 - j2);
      }

      // Compare each non-zero digit.
      int len = k1 - j1;
      for (int j = 0; j < len; j++) {
        char c1 = s1.charAt(j1 + j);
        char c2 = s2.charAt(j2 + j);
        if (c1 != c2) {
          return c1 - c2;
        }
      }

      // Compare the numbers of leading zeros (as in lexicographical order).
      if (k1 != k2) {
        if (j1 == k1) {
          // Both are 0, e.g., "a00" < "a000".
          return k1 - k2;
        } else {
          // E.g., "a0001" < "a001".
          return k2 - k1;
        }
      }

      return 0;
    }

    private static int skipZeros(String s, int first) {
      int i = first;
      while (i < s.length() && s.charAt(i) == '0') {
        i++;
      }
      return i;
    }

    private static int skipDigits(String s, int first) {
      int i = first;
      while (i < s.length() && Character.isDigit(s.charAt(i))) {
        i++;
      }
      return i;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof NameComparator;
    }

    @Override
    public int hashCode() {
      return 1;
    }
  }
}
