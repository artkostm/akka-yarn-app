package by.artsiom.bigdata201.yarn

import by.artsiom.bigdata201.yarn.cluster.{HdfsClusterSpec, YarnClusterSpec}
import org.scalatest.FlatSpec

class TestHdfsUtility extends FlatSpec with HdfsClusterSpec with YarnClusterSpec {
  "This test" should "start/stop mincluster" in {
    println("Running!!!!")
    Thread.sleep(5000)
    Info.name
  }
}
