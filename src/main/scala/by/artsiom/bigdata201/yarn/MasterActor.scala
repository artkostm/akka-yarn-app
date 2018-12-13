package by.artsiom.bigdata201.yarn

import akka.actor.{Actor, Kill, RootActorPath}
import akka.actor.SupervisorStrategy.Stop
import akka.cluster.Cluster
import akka.pattern.ask
import akka.util.Timeout
import akkeeper.api._
import akkeeper.master.service.DeployService
import by.artsiom.bigdata201.yarn.Messages.{RunTasks, Task, TaskResult}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Master actor that deploys workers to yarn containers
 */
class MasterActor extends Actor with MasterBehaviour {
  implicit val timeout  = Timeout(5 seconds)
  implicit val executor = context.system.dispatcher

  val filePath = context.system.settings.config.getString("hdfs.file")

  val deployService = DeployService.createRemote(context.system)

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self)

  cluster.registerOnMemberUp {
    (deployService ? DeployContainer("worker", 5)).onComplete {
      case Success(SubmittedInstances(_, _, _)) => self ! RunTasks
      case Success(_) | Failure(_) =>
        context.become(failed)
    }
  }

  override def receive: Receive = running

  override def postStop(): Unit = cluster.unsubscribe(self)
}

trait MasterBehaviour { this: MasterActor =>

  val failed: Receive = {
    case Stop => self ! Kill
  }

  val waitingForResult: Receive = {
    case TaskResult(result: Map[(String, String), Int]) =>
      println(result)
  }

  val running: Receive = {
    case RunTasks =>
      cluster.state.members
        .filter(_.hasRole("tworker"))
        .map(m => RootActorPath(m.address) / "user" / "tworker")
        .map(context.system.actorSelection)
        .foreach(_ ! Task(self, filePath))

      context.become(waitingForResult)
  }
}
