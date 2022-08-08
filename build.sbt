lazy val root = project.in(file(".")).settings(
  name := "hwtest",
  version := "1.0.0",
  scalaVersion := "3.1.3",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  ThisBuild / versionScheme := Some("semver-spec"),
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12",
)
