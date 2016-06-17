name := """Veritask"""

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.7"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

pipelineStages := Seq(uglify, digest, gzip)

pipelineStages in Assets := Seq()

pipelineStages := Seq(uglify, digest, gzip)

routesGenerator := InjectedRoutesGenerator

DigestKeys.algorithms += "sha1"

UglifyKeys.uglifyOps := { js =>
  Seq((js.sortBy(_._2), "concat.min.js"))
}

resolvers += "bblfish-snapshots" at "http://bblfish.net/work/repo/snapshots/"
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.0",
  "javax.inject" % "javax.inject" % "1",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.11-play24",
  "com.adrianhurt" %% "play-bootstrap" % "1.0-P24-B3",
  "org.mockito" % "mockito-core" % "1.10.19" % "test",
  "org.webjars.bower" % "jsrender" % "1.0.0-rc.70",
  "org.webjars" % "node-uuid" % "1.4.7"
)

libraryDependencies += filters
libraryDependencies += ws
libraryDependencies += specs2 % Test
libraryDependencies += "org.w3" %% "banana-jena" % "0.8.1" //"0.8.1"
libraryDependencies += "org.w3" %% "banana-sesame" % "0.8.1" //"0.8.1"
libraryDependencies += "io.megl" %% "play-json-extra" % "2.4.3"
//updateOptions := updateOptions.value.withCachedResolution(true)

//fork in run := true
