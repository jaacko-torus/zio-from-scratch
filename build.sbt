ThisBuild / version      := "0.1.0"
ThisBuild / scalaVersion := "3.1.3"
ThisBuild / organization := "jaackotorus"

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
//    "-explain",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Yexplicit-nulls", // experimental (I've seen it cause issues with circe)
    "-Ykind-projector",
    "-Ysafe-init", // experimental (I've seen it cause issues with circe)
  ) ++ Seq("-rewrite", "-indent") ++ Seq("-source", "future-migration")

lazy val root = project
  .in(file("."))
  .settings(
    name              := "zio-from-scratch",
    idePackagePrefix  := Some(organization.value),
    semanticdbEnabled := true,
  )
  .settings(commonSettings)
  .settings(dependencies)

lazy val commonSettings = commonScalacOptions ++ Seq(
  update / evictionWarningOptions := EvictionWarningOptions.empty,
)

lazy val commonScalacOptions = Seq(
  Compile / console / scalacOptions --= Seq(
    "-Wunused:_",
    "-Xfatal-warnings",
  ),
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value,
)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio" % "1.0.16",
  ),
//  libraryDependencies ++= Seq(
//  ).map(_ % Test),
)
