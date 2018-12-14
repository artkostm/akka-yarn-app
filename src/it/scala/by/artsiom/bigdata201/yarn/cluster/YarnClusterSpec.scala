package by.artsiom.bigdata201.yarn.cluster

import com.github.sakserv.minicluster.MiniCluster
import com.github.sakserv.minicluster.impl.YarnLocalCluster
import org.apache.hadoop.conf.Configuration
import org.scalatest.{BeforeAndAfterAll, Suite}

trait YarnClusterSpec extends BeforeAndAfterAll { this: Suite =>
  override protected def beforeAll(): Unit = {
    super.beforeAll()
    YarnClusterSpec.start()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    YarnClusterSpec.stop()
  }
}

object YarnClusterSpec extends ClusterSpec {
  override protected val cluster: MiniCluster = new YarnLocalCluster.Builder()
    .setNumNodeManagers(1)
    .setNumLocalDirs(1)
    .setNumLogDirs(1)
    .setResourceManagerAddress("localhost:37001")
    .setResourceManagerHostname("localhost")
    .setResourceManagerSchedulerAddress("localhost:37002")
    .setResourceManagerResourceTrackerAddress("localhost:37003")
    .setResourceManagerWebappAddress("localhost:37004")
    .setUseInJvmContainerExecutor(false)
    .setConfig(new Configuration())
    .build()
}
