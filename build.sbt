import Settings._

val kleisli: Project = (project in file(".")).
                       settings(commonSettings: _*).
                       settings(addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.6.3")).
                       settings(
                           name := "kleisli",
                             libraryDependencies ++= Seq(
                             "org.scalaz" %% "scalaz-core" % "7.2.2"
                             )
                         )




