import play.PlayImport.PlayKeys._

lazy val commonLibraryDependencies = Seq(
  ws,
  "com.amazonaws" % "aws-java-sdk" % "1.10.0",
  "com.gu" %% "pan-domain-auth-play_2-4-0" % "0.2.7"
)

lazy val commonSettings = Seq(
  scalaVersion := "2.11.6",
  libraryDependencies ++= commonLibraryDependencies
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
.settings(commonSettings: _*).
  settings(
    name := "blottr",
    playDefaultPort := 9681
  )
 
