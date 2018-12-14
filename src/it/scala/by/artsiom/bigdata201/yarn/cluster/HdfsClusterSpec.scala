package by.artsiom.bigdata201.yarn.cluster

import com.github.sakserv.minicluster.MiniCluster
import com.github.sakserv.minicluster.impl.HdfsLocalCluster
import org.apache.hadoop.conf.Configuration
import org.scalatest.{BeforeAndAfterAll, Suite}

trait HdfsClusterSpec extends BeforeAndAfterAll { this: Suite =>
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    HdfsClusterSpec.start()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    HdfsClusterSpec.stop()
  }
}

object HdfsClusterSpec extends ClusterSpec {

  override protected val cluster: MiniCluster = new HdfsLocalCluster.Builder()
    .setHdfsNamenodePort(12345)
    .setHdfsNamenodeHttpPort(12341)
    .setHdfsTempDir("embedded_hdfs")
    .setHdfsNumDatanodes(1)
    .setHdfsEnablePermissions(false)
    .setHdfsFormat(true)
    .setHdfsEnableRunningUserAsProxyUser(true)
    .setHdfsConfig(new Configuration())
    .build()
}