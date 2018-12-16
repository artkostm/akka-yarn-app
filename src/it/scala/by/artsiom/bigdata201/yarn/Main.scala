package by.artsiom.bigdata201.yarn

import akkeeper.launcher.LauncherMain
import by.artsiom.bigdata201.yarn.cluster.{HdfsClusterSpec, YarnClusterSpec, ZookeeperClusterSpec}
import org.apache.hadoop.fs.Path
import org.scalatest.FlatSpec

class TestHdfsUtility extends FlatSpec with HdfsClusterSpec with YarnClusterSpec with ZookeeperClusterSpec {
  "This test" should "start/stop mincluster" in {
    println("Running!!!!")
    fs.listStatus(new Path("/")).foreach { fileStatus =>
      println(fileStatus.getPath)
    }
    println("Summary")
    println(fs.getContentSummary(new Path("/")))
    fs.copyFromLocalFile(new Path("src/it/resources/test.conf"), new Path("/test.conf"))

    println(fs.getContentSummary(new Path("/test.conf")))
    println(s"traget/scala-2.12/${BuildInfo.JarName}")

    LauncherMain.main(Array(
      "--akkeeperJar", "src/it/resources/akkeeper-assembly-0.3.3.jar",
      "--config", "src/it/resources/test.conf",
      "--jars", "src/it/resources/hadoop-common-2.7.3.2.6.3.0-235.jar,src/it/resources/hadoop-hdfs-2.7.3.2.6.3.0-235.jar,src/it/resources/hadoop-core-1.2.1.jar",
      s"target/scala-2.12/${BuildInfo.JarName}"
    ))

    println("??????????????Launcher completed!!!!!!!!!!!!!!")
    Thread.sleep(60000 * 4)
    // --config ./config.conf /path/to/my.jar
  }
}
