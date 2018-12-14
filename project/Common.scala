import sbt._
import Keys._
import sbtassembly._
import AssemblyKeys._

object Common {
  lazy val default = Seq(
    name := "akka-yarn-app",
    version := "0.1",
    scalaVersion := "2.12.8",
    resolvers ++= Seq(
      "Hortonworks public" at "http://repo.hortonworks.com/content/groups/public/",
      "Hortonworks release" at "http://repo.hortonworks.com/content/repositories/releases/",
      "Sonatype release" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/",
      "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
      Resolver.sonatypeRepo("public")
    ),
    testOptions in IntegrationTest ++= Seq(
      Tests.Argument(TestFrameworks.ScalaTest, "-oD"),
      Tests.Argument(TestFrameworks.ScalaTest, "-W", "120", "60")
    ),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", _ @_*) => MergeStrategy.discard
      case _                           => MergeStrategy.first
    },
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )
}
