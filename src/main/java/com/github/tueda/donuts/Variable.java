package com.github.tueda.donuts;

import java.io.Serializable;
import java.util.Comparator;

/** Variables. */
public final class Variable implements Comparable<Variable>, Serializable {
  private static final long serialVersionUID = 1L;

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

  static final Comparator<String> comparator = new NameComparator();

  public static class NameComparator implements Comparator<String> {
    @Override
    public int compare(String s1, String s2) {
      if (s1 == s2) {
        return 0;
      }

      // more or less "natural order numerical sorting"

      int len1 = s1.length();
      int len2 = s2.length();
      int limit = Math.min(len1, len2);

      int i = 0;
      while (i < limit) {
        char c1 = s1.charAt(i);
        char c2 = s2.charAt(i);
        if (Character.isDigit(c1) && Character.isDigit(c2)) {
          // numeric order: firstly skip zeros and find the end
          int j1 = i;
          while (c1 == '0' && j1 + 1 < len1) {
            c1 = s1.charAt(j1 + 1);
            if (!Character.isDigit(c1)) {
              break;
            }
            j1++;
          }
          int k1 = j1;
          while (k1 < len1 && Character.isDigit(s1.charAt(k1))) {
            k1++;
          }

          int j2 = i;
          while (c2 == '0' && j2 + 1 < len2) {
            c2 = s2.charAt(j2 + 1);
            if (!Character.isDigit(c2)) {
              break;
            }
            j2++;
          }
          int k2 = j2;
          while (k2 < len2 && Character.isDigit(s2.charAt(k2))) {
            k2++;
          }

          // compare the numbers of digits
          if (k1 - j1 != k2 - j2) {
            return (k1 - j1) - (k2 - j2);
          }

          // compare the digits
          int numlen = k1 - j1;
          for (int j = 0; j < numlen; j++) {
            c1 = s1.charAt(j1 + j);
            c2 = s2.charAt(j2 + j);
            if (c1 != c2) {
              return c1 - c2;
            }
          }

          // compare the numbers of zeros
          if (k1 != k2) {
            if (numlen == 1 && c1 == '0') {
              // both are 0
              return k1 - k2;
            } else {
              return k2 - k1;
            }
          }

          i = k1;
        } else {
          // lexicographical order
          if (c1 != c2) {
            return c1 - c2;
          }

          i++;
        }
      }

      return len1 - len2;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof NameComparator;
    }
  }
}
