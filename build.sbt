addCommandAlias("dist", ";riffRaffArtifact")

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

lazy val root = (project in file(".")).enablePlugins(PlayScala, RiffRaffArtifact)
.settings(commonSettings: _*).
  settings(
    name := "blottr",
    playDefaultPort := 9681,
    packageName in Universal := normalizedName.value,
    riffRaffPackageType := (packageZipTarball in config("universal")).value,
    doc in Compile <<= target.map(_ / "none")
  )
 
