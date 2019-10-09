Donuts
======

A wrapper library for [Rings](https://github.com/PoslavskySV/rings).

This library provides polynomial arithmetic operations based on the Rings
library by Stanislav Poslavsky. The main difference from Rings is that it
allows operations between different polynomial rings; for example, addition of
two polynomials in Z[x,&nbsp;y] and Z[y,&nbsp;z], respectively, gives a
polynomial in Z[x,&nbsp;y,&nbsp;z].

Currently, the following wrapper classes are available:

| Donuts             | Rings                                          |
| ------------------ | ---------------------------------------------- |
| `Polynomial`       | `MultivariatePolynomial<BigInteger>`           |
| `RationalFunction` | `Rational<MultivariatePolynomial<BigInteger>>` |


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

jshell> new Polynomial("-2*x^4*y^3 + 2*x^3*y^4 + 2*x^2*y^5 - 2*x*y^6").factorize()
$9 ==> Polynomial[4] { -2*x*y^2, -1*y+x, -1*y+x, y+x }
```

License
-------

MIT
