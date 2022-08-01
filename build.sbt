lazy val root = project.in(file(".")).settings(
  name := "hwtest",
  version := "0.3.0",
  scalaVersion := "3.1.3",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  ThisBuild / versionScheme := Some("semver-spec"),
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12",
  Compile / packageDoc / publishArtifact := false, // don't publish javadocs
  Compile / packageSrc / publishArtifact := false // don't publish source
)
