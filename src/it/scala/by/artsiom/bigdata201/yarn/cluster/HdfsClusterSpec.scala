package by.artsiom.bigdata201.yarn.cluster

import com.github.sakserv.minicluster.impl.HdfsLocalCluster
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.scalatest.{BeforeAndAfterAll, Suite}

trait HdfsClusterSpec extends BeforeAndAfterAll { this: Suite =>
  def fs: FileSystem = HdfsClusterSpec.cluster.getHdfsFileSystemHandle

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    HdfsClusterSpec.start()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    HdfsClusterSpec.stop()
  }
}

object HdfsClusterSpec extends Cluster {

  override protected val cluster = new HdfsLocalCluster.Builder()
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
