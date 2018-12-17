package by.artsiom.bigdata201.yarn

import java.io.File

import akka.actor.{ActorRef, Props}
import akka.cluster.ClusterEvent.MemberUp
import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import akkeeper.master.service.MasterService
import by.artsiom.bigdata201.yarn.Constants._
import by.artsiom.bigdata201.yarn.Messages.RunTasks
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

class MultiNodeMasterSpecNode         extends MasterSpec
class MultiNodeWorkerSpecNode         extends MasterSpec
class MultiNodeAkkeeperMasterSpecNode extends MasterSpec

object MultiNodeAppConfig extends MultiNodeConfig {
  val masterNode     = role(MasterActorRole)
  val workerNode     = role(WorkerActorRole)
  val akkeeperMaster = role(MasterService.MasterServiceName)

  testTransport(on = true)

  nodeConfig(masterNode)(ConfigFactory.parseString(s"""
                                                      |akka.cluster.roles=[$MasterActorRole]
                                                      |hdfs.file=src/test/resources/test.csv
                                                      |hdfs.out.dir=/tmp
    """.stripMargin))

  nodeConfig(workerNode)(ConfigFactory.parseString(s"""
                                                      |akka.cluster.roles=[$WorkerActorRole]
    """.stripMargin))

  nodeConfig(akkeeperMaster)(ConfigFactory.parseString(s"""
                                                          |akka.cluster.roles=[${MasterService.MasterServiceName}]
    """.stripMargin))

  commonConfig(ConfigFactory.parseString("""
                                           |akka.loglevel=INFO
                                           |akka.actor.provider = cluster
                                           |akka.coordinated-shutdown.run-by-jvm-shutdown-hook = off
                                           |akka.coordinated-shutdown.terminate-actor-system = off
                                           |akka.cluster.run-coordinated-shutdown-when-down = off
    """.stripMargin))
}

class MasterSpec
    extends MultiNodeSpec(MultiNodeAppConfig)
    with MultiNodeAppSpec
    with ImplicitSender {
  import MultiNodeAppConfig._

  override def initialParticipants: Int = roles.size

  val masterAddress         = node(masterNode).address
  val workerAddress         = node(workerNode).address
  val akkeeperMasterAddress = node(akkeeperMaster).address

  var masterActor: Option[ActorRef] = None

  "Cluster" must {

    "start all nodes" in within(15 seconds) {
      runOn(akkeeperMaster) {
        system.actorOf(Props[AkkeeperMasterTestActor], MasterService.MasterServiceName)
        enterBarrier("deployed")
      }

      runOn(masterNode) {
        masterActor = Some(system.actorOf(Props[MasterActor], MasterActorRole))
        enterBarrier("deployed")
      }

      runOn(workerNode) {
        system.actorOf(Props[WorkerActor], WorkerActorRole)
        enterBarrier("deployed")
      }

      receiveN(3).collect { case MemberUp(m) => m.address }.toSet must be(
        Set(node(masterNode).address, node(workerNode).address, node(akkeeperMaster).address)
      )

      testConductor.enter("all-deployed")
    }

    "run tasks and proceed working" in within(15 seconds) {
      masterActor.foreach { master =>
        master ! RunTasks
      }
    }

    "generate task result" in within(5 seconds) {
      val files = new File("/tmp").listFiles { (_: File, name: String) =>
        name.startsWith("akka-yarn-0") && name.endsWith(".csv")
      }

      files.size must be(1)
    }
  }
}
