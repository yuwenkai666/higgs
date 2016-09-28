import sbt.Keys._
import sbt.Resolver.{file => _, url => _}
import sbt._

name := "higgs"
scalaVersion := "2.11.7"
scmInfo := Some(ScmInfo(url("https://github.com/zcourts/higgs"), "scm:git:git@github.com:zcourts/higgs.git"))

val customResolvers = Seq(
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Twitter Repository" at "http://maven.twttr.com",
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases")
)

lazy val sharedSettings = Seq(
  scalaVersion := "2.11.7",
  organization := "io.higgs",
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  libraryDependencies ++= Seq(
    "io.netty" % "netty-all" % "4.0.33.Final",
    "org.slf4j" % "slf4j-api" % "1.7.21",
    "ch.qos.logback" % "logback-classic" % "1.1.7" % "test",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  )
)

val coreDeps = Seq()

val wsClientDeps = Seq(
)

lazy val core = project.in(file("core"))
  .settings(sharedSettings)
  .settings(libraryDependencies ++= coreDeps)

lazy val `ws-client` = project.in(file("ws-client"))
  .settings(libraryDependencies ++= wsClientDeps)
  .settings(sharedSettings)
  .dependsOn(core)


lazy val higgs = project.in(file("."))
  .settings(
    //disable publishing the empty root project
    publishLocal := {},
    publish := {}
  )
  .aggregate(core, `ws-client`)
