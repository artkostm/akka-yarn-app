package by.artsiom.bigdata201.yarn

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, RootActorPath}
import akka.actor.SupervisorStrategy.Stop
import akka.cluster.Cluster
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.alpakka.hdfs._
import akka.stream.alpakka.hdfs.scaladsl.HdfsFlow
import akka.stream.scaladsl.{Sink, Source}
import akka.util.{ByteString, Timeout}
import akkeeper.api._
import akkeeper.master.service.DeployService
import by.artsiom.bigdata201.yarn.Messages.{RunTasks, Task, TaskResult}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem

import scala.concurrent.duration._
import scala.util.Success
import Constants._

/**
 * Master actor that deploys workers to yarn containers and writes result to HDFS
 */
class MasterActor extends Actor with MasterBehaviour with ActorLogging {
  implicit val timeout  = Timeout(5 seconds)
  implicit val executor = context.system.dispatcher

  val filePath = context.system.settings.config.getString(HdfsFileSettingName)
  val outDir   = context.system.settings.config.getString(HdfsOutputDirectorySettingName)

  val deployService = DeployService.createRemote(context.system)

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self)

  cluster.registerOnMemberUp {
    (deployService ? DeployContainer(WorkerActorRole, 5)).onComplete {
      case Success(SubmittedInstances(_, _, _)) => self ! RunTasks
      case Success(_) | scala.util.Failure(_) =>
        context.become(failed)
    }
  }

  override def receive: Receive = running

  override def postStop(): Unit = cluster.unsubscribe(self)
}

trait MasterBehaviour { this: MasterActor =>
  implicit val mat = ActorMaterializer()(context.system)

  val failed: Receive = {
    case Stop => context stop self
  }

  val waitingForResult: Receive = {
    case Failure(exception) =>
      context stop sender
      log.error(exception, "error while executing task")
    case TaskResult(result: Map[(String, String), Int]) =>
      val fs = FileSystem.get(new Configuration())
      Source(result)
        .map(m => HdfsWriteMessage(ByteString(s"${m._1._1}\t${m._1._2}\t${m._2}")))
        .via(
          HdfsFlow.data(
            fs,
            SyncStrategy.count(3),
            RotationStrategy.none,
            HdfsWritingSettings(
              overwrite = false,
              newLine = true,
              lineSeparator = System.lineSeparator(),
              pathGenerator = FilePathGenerator(
                (rotationCount, timestamp) => s"$outDir/akka-yarn-$rotationCount-$timestamp.csv"
              )
            )
          )
        )
        .runWith(Sink.ignore)
  }

  val running: Receive = {
    case RunTasks =>
      cluster.state.members
        .filter(_.hasRole(WorkerActorRole))
        .map(m => RootActorPath(m.address) / RootActorName / WorkerActorRole)
        .map(context.system.actorSelection)
        .foreach(_ ! Task(self, filePath))

      context.become(waitingForResult)
  }
}
