package by.artsiom.bigdata201.yarn
import akka.actor.Actor
import akkeeper.api.{DeployContainer, OperationFailed, SubmittedInstances}

class AkkeeperMasterTestActor extends Actor {
  override def receive: Receive = {
    case DeployContainer(name, _, _, _, reqId) if name == "wrongName" =>
      sender ! OperationFailed(reqId, new RuntimeException("no containers with name " + name))
    case DeployContainer(name, _, _, _, reqId) =>
      sender ! SubmittedInstances(reqId, name, Seq.empty)
  }
}
