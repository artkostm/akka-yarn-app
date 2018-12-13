name := "akka-yarn-app"

version := "0.1"

scalaVersion := "2.12.8"

val hadoop = "3.1.1"

libraryDependencies += "com.github.izeigerman" %% "akkeeper"     % "0.3.3" % Provided
libraryDependencies += "org.apache.hadoop"     % "hadoop-hdfs"   % hadoop % Provided
libraryDependencies += "org.apache.hadoop"     % "hadoop-common" % hadoop % Provided
libraryDependencies += "org.typelevel"         %% "cats-core"    % "1.5.0"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _ @_*) => MergeStrategy.discard
  case _                           => MergeStrategy.first
}

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
