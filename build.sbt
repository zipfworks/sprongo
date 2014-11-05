import SonatypeKeys._

sonatypeSettings

name := "sprongo"

version := "1.1.2.akka23-SNAPSHOT"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

scalacOptions ++= Seq("-feature", "-deprecation")

organization := "com.zipfworks"

publishTo <<= (version) { version: String =>
  val nexus = "https://oss.sonatype.org/"
  if (version.trim.endsWith("SNAPSHOT")) {
    Some("snapshots" at nexus + "content/repositories/snapshots")
   } else {
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }
}

publishMavenStyle := true

publishArtifact in Test := true

pomIncludeRepository := { _ => false }

pomExtra :=
  <url>https://github.com/zipfworks/sprongo</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/MIT</url>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:zipfworks/sprongo.git</url>
    <connection>scm:git:git@github.com:zipfworks/sprongo.git</connection>
  </scm>
  <developers>
    <developer>
      <id>kfang</id>
      <name>Kevin Fang</name>
      <url>https://github.com/kfang</url>
    </developer>
    <developer>
      <id>Stanback</id>
      <name>Brian Stanback</name>
      <url>https://github.com/stanback</url>
    </developer>
    <developer>
      <id>dvliman</id>
      <name>David Liman</name>
      <url>https://github.com/dvliman</url>
     </developer>
  </developers>


resolvers := Seq(
  "sonatype-releases"   at "https://oss.sonatype.org/content/repositories/releases/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype Snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/",
  "spray"               at "http://repo.spray.io/"
)


libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo"   % "0.10.5.0.akka23",
  "io.spray"          %%  "spray-json"     % "1.3.1",
  "joda-time"          % "joda-time"       % "2.3",
  "org.joda"           % "joda-convert"    % "1.5",
  "org.specs2"        %% "specs2-core"     % "2.4.9" % "test"
)

org.scalastyle.sbt.ScalastylePlugin.Settings
