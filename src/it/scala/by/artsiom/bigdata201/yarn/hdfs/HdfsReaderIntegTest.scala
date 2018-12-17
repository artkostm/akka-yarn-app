package by.artsiom.bigdata201.yarn.hdfs

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.hdfs._
import akka.stream.alpakka.hdfs.scaladsl.HdfsFlow
import akka.stream.scaladsl.{Sink, Source}
import by.artsiom.bigdata201.yarn.cluster.HdfsClusterSpec
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.compress.DefaultCodec
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class HdfsReaderIntegTest extends WordSpecLike with HdfsClusterSpec with Matchers with BeforeAndAfterEach {
  import util.ScalaTestUtils._

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  private val destination = "/tmp/akka-yarn/"

  val settings = HdfsWritingSettings()

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  "HdfsSource" should {
    "read data file" in {
      val flow = HdfsFlow.data(
        fs,
        SyncStrategy.count(500),
        RotationStrategy.size(0.5, FileUnit.KB),
        HdfsWritingSettings()
      )

      val content = generateFakeContent(1, FileUnit.KB.byteCount)

      val resF1 = Source
        .fromIterator(() => content.toIterator)
        .map(HdfsWriteMessage(_))
        .via(flow)
        .runWith(Sink.seq)

      val resF = resF1.flatMap { logs =>
        Future
          .sequence(
            logs.map { log =>
              val path = new Path("/tmp/akka-yarn", log.path)
              //#define-data-source
              val source = HdfsSource.data(fs, path)
              //#define-data-source
              source.runWith(Sink.seq)
            }
          )
          .map(_.flatten)
      }

      val result = Await.result(resF, Duration.Inf)
      content.flatMap(_.utf8String) shouldBe result.flatMap(_.utf8String)
    }

    "read compressed data file" in {
      val codec = new DefaultCodec()
      codec.setConf(fs.getConf)

      val flow = HdfsFlow.compressed(
        fs,
        SyncStrategy.count(1),
        RotationStrategy.size(0.1, FileUnit.MB),
        codec,
        settings
      )

      val content = generateFakeContentWithPartitions(1, FileUnit.MB.byteCount, 30)

      val resF1 = Source
        .fromIterator(() => content.toIterator)
        .map(HdfsWriteMessage(_))
        .via(flow)
        .runWith(Sink.seq)

      val resF = resF1.flatMap { logs =>
        Future
          .sequence(
            logs.map { log =>
              val path = new Path("/tmp/akka-yarn", log.path)
              val source = HdfsSource.compressed(fs, path, codec)
              source.runWith(Sink.seq)
            }
          )
          .map(_.flatten)
      }

      val result = Await.result(resF, Duration.Inf)
      content.flatMap(_.utf8String) shouldBe result.flatMap(_.utf8String)
    }
  }

  override protected def afterEach(): Unit = {
    fs.delete(new Path(destination), true)
    fs.delete(settings.pathGenerator(0, 0).getParent, true)
    ()
  }
}
