### General

Calculates medcouple for Python 3 and Scala (native or Spark) using the naive algorithm (does not scale well to large n).

Implemented based on
- https://wis.kuleuven.be/stat/robust/papers/2004/medcouple.pdf
- https://en.wikipedia.org/wiki/Medcouple
- https://github.com/statsmodels/statsmodels/issues/5395

### Python

Recommend using [statsmodels](https://github.com/statsmodels/statsmodels) instead for small n, but you can use this code for reference to understand the logic of the medcouple algorithm. See the bottom of `python/test.py` for an example of how it's used. Tests show that the results match up but statsmodels is slightly faster.

As for scalable libraries using the fast algorithm, haven't found any Python libraries that are 100% accurate yet, existing ones have a problem with handling ties to the median like in the example in the GitHub issue above, and the results do not match those of the naive algorithms and R's fast algorithm.

### Scala

Can use as a library project/jar, or just copy the relevant files over to your project.

To run without Spark:
```
cd scala
# ensure `scalaVersion` in `build.sbt` matches your version or you might get a runtime error like: Exception in thread "main" java.lang.NoSuchMethodError: scala.Predef$.doubleArrayOps([D)[D
sbt assembly
scala -classpath target/medcouple.jar stats.medcouple.TestAlgos
```
To run the Spark code with UDF, do the above but instead of the last line, spark-submit that jar with class `stats.medcouple.TestSpark`.

#### Advanced, safe to ignore

If you see these lines during runtime, it means that the Medcouple implementations using Breeze matrices will be unoptimised and slower than it should be:
```
WARN BLAS: Failed to load implementation from: com.github.fommil.netlib.NativeSystemBLAS
WARN BLAS: Failed to load implementation from: com.github.fommil.netlib.NativeRefBLAS
```
To fix that, ensure these libraries are included in `build.sbt`:
```
  "org.scalanlp" %% "breeze-natives" % "1.0",
  "com.github.fommil.netlib" % "all" % "1.1.2" pomOnly()
```
Shade so they don't conflict with spark's version of breeze:
```
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("breeze.**" -> "shaded.breeze.@1").inAll
)
```
And get libraries like BLAS natively installed on your system.
If you manage to do this, you can check if `Medcouple.calcMatrix` is faster. Otherwise, just stick with the default `Medcouple.calcLoop`.
