package by.artsiom.bigdata201.yarn.cluster

import java.util.concurrent.atomic.AtomicBoolean

import com.github.sakserv.minicluster.MiniCluster

trait ClusterSpec {
  private val started = new AtomicBoolean(false)
  private val stopped = new AtomicBoolean(false)

  protected val cluster: MiniCluster

  def start() = {
    if (started.compareAndSet(false, true)) {
      cluster.start()
    }
  }

  def stop() = {
    if (stopped.compareAndSet(false, true)) {
      cluster.stop()
    }
  }
}
