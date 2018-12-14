lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(Common.default)
  .settings(
    libraryDependencies ++= Dependencies.main,
    libraryDependencies ++= Dependencies.unit,
    libraryDependencies ++= Dependencies.it,
    dependencyOverrides ++= Dependencies.overrides
  )
