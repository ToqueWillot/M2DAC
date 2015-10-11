name := "RI_Project-evaluation"

organization := "com.fulldeep"

scalaVersion := "2.10.5"

version := "0.1"

libraryDependencies ++= Seq(
  "org.json4s"   %% "json4s-native" % "3.2.4",
  "org.json4s"   %% "json4s-jackson" % "3.2.4",
  "org.scalanlp" %% "breeze" % "0.10",
  "org.scalanlp" %% "breeze-natives" % "0.10",
  "com.fulldeep" %% "ri_project-indexation" % "0.1",
  "com.fulldeep" %% "ri_project-modeles" % "0.1"
)

resolvers ++= Seq(
            // other resolvers here
            "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)
