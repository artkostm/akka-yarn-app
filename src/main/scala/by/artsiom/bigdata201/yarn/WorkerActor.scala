package by.artsiom.bigdata201.yarn

import java.net.URI

import akka.actor.{Actor, ActorSystem, Kill, PoisonPill, Props}
import akka.pattern.pipe
import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.Supervision.Decider
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.{Flow, Framing, Sink}
import akka.util.{ByteString, Timeout}
import by.artsiom.bigdata201.yarn.Messages.{Task, TaskResult}
import by.artsiom.bigdata201.yarn.hdfs.HdfsSource
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import cats.implicits._

import scala.collection.immutable.ListMap
import scala.concurrent.Await
import scala.util.{Failure, Success}

class WorkerActor extends Actor with WorkerBehaviour {

  override def receive: Receive = working
}

object WorkerActor {
  val RecordSize      = 24 //22
  val AdultCount      = "2"
  val AdultCountIdx   = 13 //14
  val HotelCountryIdx = 21 //20
  val HotelMarketIdx  = 22 //21
  val FieldSeparator  = ","
}

trait WorkerBehaviour { this: WorkerActor =>
  import WorkerActor._
  import context.dispatcher

  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(context.system).withSupervisionStrategy({
      case e: Throwable =>
        e.printStackTrace()
        Supervision.Stop
    }: Decider)
  )(context.system)

  val working: Receive = {
    case Task(master, file, limit) =>
      val fs = FileSystem.get(new Configuration())

      val result = HdfsSource
        .data(fs, new Path(file))
        .via(
          Framing.delimiter(ByteString("\n"), //sys.props("line.separator")), System.lineSeparator
                            maximumFrameLength = 450,
                            allowTruncation = true)
        )
        .map(_.utf8String)
        .via(
          Flow[String]
            .map(_.split(FieldSeparator))
            .filter(r => r.size >= RecordSize && r(AdultCountIdx).trim == AdultCount) // only pass correct records with adult count = 2
            .map(arr => (arr(HotelCountryIdx), arr(HotelMarketIdx))) // hotel country and market
            .scan(Map.empty[(String, String), Int]) { (acc, entry) =>
              acc |+| Map(entry -> 1)
            }
        )
        .runWith(Sink.last)

      result.map(
        m =>
          TaskResult(
            ListMap(m.toSeq.sortBy(-_._2).take(limit): _*)
        )
      ) pipeTo master
  }
}

object tt extends App {
  val system = ActorSystem("test")

  implicit val d = system.dispatcher

  class TestActor extends Actor {
    override def receive: Receive = {
      case TaskResult(result: Map[(String, String), Int]) =>
        println(result.mkString("===========\n", "\n", "\n==========="))
        context stop self
        context.system.terminate()
    }
  }

  val testActor = system.actorOf(Props[TestActor], "test_actor")

  val worker = system.actorOf(Props[WorkerActor], "worker")

  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(50 minutes)

  val f = worker ? Task(testActor, "D:\\homework\\Hadoop.Intro\\train.csv", 3)

  //akka.pattern.gracefulStop(worker, 20 seconds)

  f.onComplete {
    case Success(_) =>
      println("Good!")
      system.terminate()
    case Failure(exception) =>
      exception.printStackTrace()
      system.terminate()
  }

  Await.ready(system.whenTerminated, Duration.Inf)
}
