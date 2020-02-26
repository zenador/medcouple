name := "medcouple"
version := "0.0.1"
organization := "stats"
scalaVersion := "2.11.8"

val sparkVersion = "2.4.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
  "org.scalanlp" %% "breeze" % "1.0"
  // "org.scalanlp" %% "breeze-natives" % "1.0",
  // "com.github.fommil.netlib" % "all" % "1.1.2" pomOnly()
)

crossPaths := false
assemblyJarName in assembly := "medcouple.jar"
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("breeze.**" -> "shaded.breeze.@1").inAll // necessary to avoid clashing with spark's
)
