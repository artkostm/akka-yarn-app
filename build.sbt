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
  .settings(sourceGenerators in Compile += Def.task {
    val file = (sourceManaged in Compile).value / "info.scala"
    IO.write(file, """package by.artsiom.bigdata201.yarn
                     |object BuildInfo {
                     |  val Version = "%s"
                     |  val Name = "%s"
                     |  val JarName = "%s"
                     |}
                     |""".stripMargin.format(version.value, name.value, (assemblyJarName in assembly).value))
    Seq(file)
  }.taskValue)
