libraryDependencies ++= Seq(
  "net.java.dev.jna" % "jna-platform" % "5.5.0",
  "com.typesafe" % "config" % "1.4.1",
)

lazy val commonSettings = Seq(
  resourceDirectory in Compile := baseDirectory.value / "src" / "assembly" / "resources"
)
