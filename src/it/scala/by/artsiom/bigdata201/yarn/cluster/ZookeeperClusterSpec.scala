package by.artsiom.bigdata201.yarn.cluster

import com.github.sakserv.minicluster.impl.ZookeeperLocalCluster
import org.scalatest.{BeforeAndAfterAll, Suite}

trait ZookeeperClusterSpec extends BeforeAndAfterAll { this: Suite =>
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    ZookeeperClusterSpec.start()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    ZookeeperClusterSpec.stop()
  }
}

object ZookeeperClusterSpec extends Cluster {
  override protected val cluster = new ZookeeperLocalCluster.Builder()
    .setPort(12745)
    .setTempDir("embedded_zookeeper")
    .setZookeeperConnectionString("localhost:12745")
    .setMaxClientCnxns(60)
    .setElectionPort(20001)
    .setQuorumPort(20002)
    .setDeleteDataDirectoryOnClose(true)
    .setServerId(1)
    .setTickTime(2000)
    .build()
}
