package by.artsiom.bigdata201.yarn

import akka.actor.Status.Failure
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import by.artsiom.bigdata201.yarn.Messages.{Task, TaskResult}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

class WorkerSpes
    extends TestKit(ActorSystem("WorkerSpec"))
    with ImplicitSender
    with FlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  implicit val ex = system.dispatcher

  "Worker actor" should "return top 3 hotels among couples" in {

    val receiver = TestProbe()

    val worker = system.actorOf(Props[WorkerActor], "worker1")

    worker ! Task(receiver.ref, "src/test/resources/test.csv", 3)

    receiver.expectMsg(5 seconds,
                       TaskResult(
                         Map(
                           ("105", "29") -> 42,
                           ("151", "69") -> 37,
                           ("50", "675") -> 12
                         )
                       ))
  }

  "Worker actor" should "be terminated for wrong task setting" in {
    val supervisor = TestProbe()

    val worker = system.actorOf(Props[WorkerActor], "worker2")

    val wrongFilePath = "wrong/file/path.csv"

    worker ! Task(supervisor.ref, wrongFilePath, 3)

    val message = supervisor.expectMsgPF(5 seconds) {
      case Failure(exception) => exception.getMessage
    }

    assert(message.contains(wrongFilePath))
  }

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)
}
