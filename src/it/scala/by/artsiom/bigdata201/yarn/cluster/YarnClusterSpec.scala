package by.artsiom.bigdata201.yarn.cluster

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

object YarnClusterSpec extends Cluster {
  override protected val cluster = new YarnLocalCluster.Builder()
    .setNumNodeManagers(1)
    .setNumLocalDirs(1)
    .setNumLogDirs(1)
    .setResourceManagerAddress("0.0.0.0:8032")
    .setResourceManagerHostname("0.0.0.0")
    .setResourceManagerSchedulerAddress("0.0.0.0:37002")
    .setResourceManagerResourceTrackerAddress("0.0.0.0:37003")
    .setResourceManagerWebappAddress("0.0.0.0:37004")
    .setUseInJvmContainerExecutor(false)
    .setConfig(new Configuration())
    .build()
}
