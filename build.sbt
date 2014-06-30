name := "sprongo"

version := "1.0.4"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature", "-deprecation")

organization := "com.zipfworks"

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

publishTo := {
  val nexus = "http://nexus.zipfworks.com/content/repositories/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "snapshots")
  else
    Some("releases" at nexus + "releases")
}

resolvers := Seq(
  "sonatype-releases"   at "https://oss.sonatype.org/content/repositories/releases/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype Snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/",
  "spray"               at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo"   % "0.10.0",
  "io.spray"          %%  "spray-json"     % "1.2.5",
  "joda-time"          % "joda-time"       % "2.3",
  "org.joda"           % "joda-convert"    % "1.5"
)

org.scalastyle.sbt.ScalastylePlugin.Settings
