package by.artsiom.bigdata201.yarn

import akka.actor.Actor
import akka.pattern.pipe
import akka.stream.Supervision.Decider
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.{Flow, Framing, Sink}
import akka.util.ByteString
import by.artsiom.bigdata201.yarn.Messages.{Task, TaskResult}
import by.artsiom.bigdata201.yarn.hdfs.HdfsSource
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import cats.implicits._

import scala.collection.immutable.ListMap

/**
 * Worker actor reads file partitions from HDFS and returns top 3 hotels among couples
 */
class WorkerActor extends Actor with WorkerBehaviour {
  override def receive: Receive = working
}

object WorkerActor {
  val RecordSize      = 24
  val AdultCount      = "2"
  val AdultCountIdx   = 13
  val HotelCountryIdx = 21
  val HotelMarketIdx  = 22
  val FieldSeparator  = ","
}

trait WorkerBehaviour { this: WorkerActor =>
  import WorkerActor._
  import context.dispatcher

  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(context.system).withSupervisionStrategy({
      case e: Throwable =>
        e.printStackTrace()
        context stop self
        Supervision.Stop
    }: Decider)
  )(context.system)

  val working: Receive = {
    case Task(master, file, limit) =>
      val fs = FileSystem.get(new Configuration())

      val result = HdfsSource
        .data(fs, new Path(file))
        .via(
          Framing.delimiter(ByteString(System.lineSeparator),
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

      result
        .map(
          m =>
            TaskResult(
              ListMap(m.toSeq.sortBy(-_._2).take(limit): _*)
          )
        )
        .pipeTo(master)(self)
  }
}
