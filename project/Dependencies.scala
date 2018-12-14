import sbt._

object Dependencies {
  val tests = "tests"

  val versions = new {
    val hadoop     = "2.7.3.2.6.3.0-235"
    val hadoopCore = "1.2.1"
    val akkeeper   = "0.3.3"
    val cats       = "1.5.0"

    val minicluster = "0.1.15"
    val scalatest   = "3.0.5"
  }

  lazy val main = Seq(
    "com.github.izeigerman" %% "akkeeper"   % versions.akkeeper % Provided,
    "org.apache.hadoop"     % "hadoop-core" % versions.hadoopCore % Provided,
    "org.typelevel"         %% "cats-core"  % versions.cats
  )

  lazy val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5"
  )

  lazy val unit = test.map(_ % Test) ++ Seq(
    )

  lazy val it = test.map(_ % IntegrationTest) ++ (for {
    (t, vh, vm) <- Seq((IntegrationTest, versions.hadoop, versions.minicluster))
    d <- Seq(
          "org.apache.hadoop"  % "hadoop-client"                     % vh % t,
          "org.apache.hadoop"  % "hadoop-minicluster"                % vh % t,
          "org.apache.hadoop"  % "hadoop-yarn-server-tests"          % vh % t,
          "org.apache.hadoop"  % "hadoop-yarn-api"                   % vh % t,
          "org.apache.hadoop"  % "hadoop-mapreduce-client-app"       % vh % t,
          "org.apache.hadoop"  % "hadoop-mapreduce-client-core"      % vh % t,
          "org.apache.hadoop"  % "hadoop-mapreduce-client-hs"        % vh % t,
          "org.apache.hadoop"  % "hadoop-mapreduce-client-jobclient" % vh % t,
          "com.github.sakserv" % "hadoop-mini-clusters"              % vm % t,
          "com.github.sakserv" % "hadoop-mini-clusters-hdfs"         % vm % t,
          "com.github.sakserv" % "hadoop-mini-clusters-yarn"         % vm % t,
          "com.github.sakserv" % "hadoop-mini-clusters-zookeeper"    % vm % t,
          "com.github.sakserv" % "hadoop-mini-clusters-common"       % vm % t,
          "org.apache.hadoop"  % "hadoop-hdfs"                       % vh % t classifier tests,
          "org.apache.hadoop"  % "hadoop-common"                     % vh % t classifier tests
        )
  } yield d)

  lazy val overrides = Seq(
    "org.codehaus.jackson" % "jackson-core-asl" % "1.9.13" % Test,
    "commons-daemon"       % "commons-daemon"   % "1.0.13" % Provided,
    "tomcat"               % "jasper-runtime"   % "5.5.12" % Test
  )
}
