Donuts
======

[![Test](https://github.com/tueda/donuts/workflows/Test/badge.svg?branch=master)](https://github.com/tueda/donuts/actions?query=branch:master)
[![Release](https://jitpack.io/v/tueda/donuts.svg)](https://jitpack.io/#tueda/donuts)
[![Javadoc](https://img.shields.io/badge/javadoc-latest-brightgreen.svg)](https://jitpack.io/com/github/tueda/donuts/latest/javadoc/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/tueda/donuts.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/tueda/donuts/context:java)

A wrapper library for [Rings](https://github.com/PoslavskySV/rings).

This library provides polynomial arithmetic operations based on the Rings
library by Stanislav Poslavsky. The main difference from Rings is that this library
allows operations between different polynomial rings; for example, addition of
two polynomials in Z[x,&nbsp;y] and Z[y,&nbsp;z], respectively, gives a
polynomial in Z[x,&nbsp;y,&nbsp;z].

Currently, the following wrapper classes are available:

| Donuts             | Rings                                          |
| ------------------ | ---------------------------------------------- |
| `Polynomial`       | `MultivariatePolynomial<BigInteger>`           |
| `RationalFunction` | `Rational<MultivariatePolynomial<BigInteger>>` |

All instances of the above classes of Donuts are *immutable* objects.

The [Python binding](https://github.com/tueda/donuts-python) is available.


Requirements
------------

- Java 8 or later


Example
-------

```shell
$ git clone https://github.com/tueda/donuts.git
$ cd donuts
$ ./gradlew shadowJar && jshell --class-path build/libs/donuts-*-all.jar # jshell is available in JDK9+
```
```java
jshell> import com.github.tueda.donuts.*

jshell> var a = new Polynomial("1 + x + y") // in Z[x, y]
a ==> 1+y+x

jshell> var b = new Polynomial("1 + y + z") // in Z[y, z]
b ==> 1+z+y

jshell> var g = a.add(b) // in Z[x, y, z]
g ==> 2+z+2*y+x

jshell> var ag = a.multiply(g)
ag ==> 2+z+4*y+3*x+y*z+x*z+2*y^2+3*x*y+x^2

jshell> var bg = b.multiply(g)
bg ==> 2+3*z+4*y+x+z^2+3*y*z+x*z+2*y^2+x*y

jshell> ag.gcd(bg) // must be equal to g
$7 ==> 2+z+2*y+x

jshell> ag.divide(bg) // same as new RationalFunction(ag, bg)
$8 ==> (1+y+x)/(1+z+y)

jshell> new Polynomial("-2*x^4*y^3 + 2*x^3*y^4 + 2*x^2*y^5 - 2*x*y^6").factors()
$9 ==> Polynomial[8] { -2, y, y, y, x, -y+x, -y+x, y+x }
```

Development
-----------

```shell
./gradlew spotlessApply      # code formatter
./gradlew check              # build and test
./gradlew jacocoTestReport   # code coverage
./gradlew javadoc            # build documents
./gradlew dependencyUpdates  # check dependency updates
./gradlew release            # release a new version

# Git hooks
pre-commit install
pre-commit install --hook-type commit-msg
```


Acknowledgements
----------------

This software was developed as part of the project supported by JSPS KAKENHI Grant Number 19K03831.


License
-------

MIT
