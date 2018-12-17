package by.artsiom.bigdata201.yarn

import akka.remote.testkit.{MultiNodeConfig, MultiNodeSpec}
import akka.testkit.ImplicitSender
import akkeeper.master.service.MasterService
import Constants._
import akka.actor.{Actor, Props}
import akkeeper.api.{DeployContainer, OperationFailed, SubmittedInstances}
import com.typesafe.config.ConfigFactory

class MultiNodeMasterSpecNode extends MasterSpec
class MultiNodeWorkerSpecNode extends MasterSpec
class MultiNodeAkkeeperMasterSpecNode extends MasterSpec

object MultiNodeAppConfig extends MultiNodeConfig {
  val masterNode = role(MasterActorRole)
  val workerNode = role(WorkerActorRole)
  val akkeeperMaster = role(MasterService.MasterServiceName)

  nodeConfig(masterNode)(ConfigFactory.parseString(
    s"""
      |akka.cluster.roles=[$MasterActorRole]
    """.stripMargin))

  nodeConfig(workerNode)(ConfigFactory.parseString(
    s"""
      |akka.cluster.roles=[$WorkerActorRole]
    """.stripMargin))

  nodeConfig(akkeeperMaster)(ConfigFactory.parseString(
    s"""
      |akka.cluster.roles=[${MasterService.MasterServiceName}]
    """.stripMargin))
}

class MasterSpec extends MultiNodeSpec(MultiNodeAppConfig) with MultiNodeAppSpec with ImplicitSender {
  import MultiNodeAppConfig._

  override def initialParticipants: Int = roles.size

  val masterAddress = node(masterNode).address
  val workerAddress = node(workerNode).address
  val akkeeperMasterAddress = node(akkeeperMaster).address

  it should "" in {
    runOn(akkeeperMaster) {
      system.actorOf(Props[AkkeeperMasterTestActor], MasterService.MasterServiceName)
      enterBarrier("deployed")
    }

    runOn(masterNode) {
      system.actorOf(Props[MasterActor], MasterActorRole)
      enterBarrier("deployed")
    }

    runOn(workerNode) {
      system.actorOf(Props[WorkerActor], WorkerActorRole)
      enterBarrier("deployed")
    }

  }
}

class AkkeeperMasterTestActor extends Actor {
  override def receive: Receive = {
    case DeployContainer(name, _, _, _, reqId) if name == "wrongName" =>
      sender ! OperationFailed(reqId, new RuntimeException("no containers with name " + name))
    case DeployContainer(name, _, _, _, reqId) =>
      sender ! SubmittedInstances(reqId, name, Seq.empty)
  }
}