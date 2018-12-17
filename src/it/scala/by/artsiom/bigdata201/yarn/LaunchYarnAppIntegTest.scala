package by.artsiom.bigdata201.yarn

import akkeeper.launcher.LauncherMain
import by.artsiom.bigdata201.yarn.cluster.{HdfsClusterSpec, YarnClusterSpec, ZookeeperClusterSpec}
import org.apache.hadoop.fs.Path
import org.scalatest.FlatSpec

class LaunchYarnAppIntegTest
    extends FlatSpec
    with HdfsClusterSpec
    with YarnClusterSpec
    with ZookeeperClusterSpec {

  "LauncherMain" should "start the yarn app" in {

    fs.copyFromLocalFile(new Path("src/it/resources/test.csv"), new Path("/hotels.csv"))

    println(fs.getContentSummary(new Path("/hotels.csv")))

    LauncherMain.main(
      Array(
        "--akkeeperJar",
        "src/it/resources/akkeeper-assembly-0.3.3.jar",
        "--config",
        "src/it/resources/test.conf",
        "--jars",
        "src/it/resources/hadoop-common-2.7.3.2.6.3.0-235.jar," +
        "src/it/resources/hadoop-hdfs-2.7.3.2.6.3.0-235.jar," +
        "src/it/resources/hadoop-core-1.2.1.jar",
        s"target/scala-2.12/${BuildInfo.JarName}"
      )
    )

    Thread.sleep(60000 * 1)

    val fileStatuses =
      fs.globStatus(new Path("/tmp/akka-yarn-0-*"), (path: Path) => path.getName.endsWith(".csv"))
    assert(fileStatuses.size == 1)
  }
}
