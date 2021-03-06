name := "sprongo"

version := "2.0.2-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation")

organization := "com.zipfworks"

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

//publishTo <<= (version) { version: String =>
//  val nexus = "https://oss.sonatype.org/"
//  if (version.trim.endsWith("SNAPSHOT")) {
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//   } else {
//    Some("releases" at nexus + "service/local/staging/deploy/maven2")
//  }
//}

publishTo := {
  val nexus = "https://nexus.zipfworks.com/content/repositories/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "snapshots")
  else
    Some("releases"  at nexus + "releases")
}

publishMavenStyle := true

publishArtifact in Test := true

pomIncludeRepository := { _ => false }

pomExtra := (
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
)

resolvers := Seq(
  "sonatype-releases"   at "https://oss.sonatype.org/content/repositories/releases/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype Snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots/",
  "spray"               at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo"   % "0.11.4",
  "io.spray"          %% "spray-json"      % "1.3.3",
  "joda-time"          % "joda-time"       % "2.3"
)
