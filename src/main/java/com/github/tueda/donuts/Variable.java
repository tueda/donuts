package com.github.tueda.donuts;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.EqualsAndHashCode;

/** A variable. Immutable. */
public final class Variable implements Comparable<Variable>, Serializable, Multivariate {
  private static final long serialVersionUID = 1L;

  /** The pattern for legal variable names. */
  private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");

  /** The comparator for variable names. */
  /* default */ static final Comparator<String> NAME_COMPARATOR = new NameComparator();

  /** The variable name. */
  private final String name;

  /**
   * Constructs a variable with the given name.
   *
   * @param name the name of the variable
   * @throws IllegalArgumentException when the given name is illegal for variables
   */
  public Variable(final String name) {
    this(name, true);
  }

  private Variable(final String name, final boolean check) {
    if (check && !isVariableName(name)) {
      throw new IllegalArgumentException(String.format("illegal variable name: \"%s\"", name));
    }
    this.name = name;
  }

  /* default */ static Variable createWithoutCheck(final String name) {
    return new Variable(name, false);
  }

  /**
   * Returns a variable with the given name.
   *
   * @param name the name of the variable
   * @return a variable with the given name
   * @throws IllegalArgumentException when the given name is illegal for variables
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static Variable of(final String name) {
    return new Variable(name);
  }

  /**
   * Returns an array of variables with the given names.
   *
   * @param names the names
   * @return an array of variables with the given names.
   * @throws IllegalArgumentException when any of the given names are illegal for variables
   */
  @SuppressWarnings("PMD.ShortMethodName")
  public static Variable[] of(final String... names) {
    return Stream.of(names).map(Variable::new).toArray(Variable[]::new);
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Variable)) {
      return false;
    }
    final Variable aVariable = (Variable) other;
    return name.equals(aVariable.name);
  }

  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  @Override
  public int compareTo(final Variable other) {
    if (this == other) {
      return 0;
    }
    return NAME_COMPARATOR.compare(name, other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public VariableSet getVariables() {
    return new VariableSet(this);
  }

  /**
   * Returns the name of this variable.
   *
   * @return the name of variable
   */
  public String getName() {
    return name;
  }

  /**
   * Returns {@code true} if the given name is legal for variables.
   *
   * @param name the name to be tested
   * @return {@code true} if the name is legal
   */
  public static boolean isVariableName(final String name) {
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

  /**
   * Returns strings that look like variable names.
   *
   * @param string the string to be examined
   * @return variable names found in the string, sorted and distinct
   */
  public static String[] guessVariableNames(final String string) {
    final Matcher matcher = IDENTIFIER_PATTERN.matcher(string);
    final Set<String> seen = new HashSet<>();
    while (matcher.find()) {
      seen.add(matcher.group());
    }
    final Stream<String> stream = StreamSupport.stream(seen.spliterator(), false);
    return stream.sorted(NAME_COMPARATOR).toArray(String[]::new);
  }

  /** Comparator class for variable names. */
  @EqualsAndHashCode
  /* default */ static class NameComparator implements Comparator<String>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "PMD.UseEqualsToCompareStrings"})
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
        justification = "Intend to compare two references",
        value = "ES_COMPARING_PARAMETER_STRING_WITH_EQ")
    public int compare(final String s1, final String s2) {
      // First, compare the references as a shortcut.
      if (s1 == s2) {
        return 0;
      }

      // This is more or less "natural order numerical sorting".

      final int len1 = s1.length();
      final int len2 = s2.length();
      final int limit = Math.min(len1, len2);

      int i = 0;
      while (i < limit) {
        final char c1 = s1.charAt(i);
        final char c2 = s2.charAt(i);
        if (Character.isDigit(c1) && Character.isDigit(c2)) {
          // numeric order
          final int result = compareNumbers(s1, s2, i);
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

    private static int compareNumbers(final String s1, final String s2, final int first) {
      final int j1 = skipZeros(s1, first);
      final int j2 = skipZeros(s2, first);
      final int k1 = skipDigits(s1, j1);
      final int k2 = skipDigits(s2, j2);

      // Compare the numbers of non-zero digits. Shorter is smaller.
      if (k1 - j1 != k2 - j2) {
        return (k1 - j1) - (k2 - j2);
      }

      // Compare each non-zero digit.
      final int len = k1 - j1;
      for (int j = 0; j < len; j++) {
        final char c1 = s1.charAt(j1 + j);
        final char c2 = s2.charAt(j2 + j);
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

    private static int skipZeros(final String s, final int first) {
      int i = first;
      while (i < s.length() && s.charAt(i) == '0') {
        i++;
      }
      return i;
    }

    private static int skipDigits(final String s, final int first) {
      int i = first;
      while (i < s.length() && Character.isDigit(s.charAt(i))) {
        i++;
      }
      return i;
    }
  }
}
