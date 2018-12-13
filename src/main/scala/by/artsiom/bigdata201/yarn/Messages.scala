package by.artsiom.bigdata201.yarn

import akka.actor.ActorRef

object Messages {
  case object RunTasks

  final case class Task(master: ActorRef, file: String, limit: Int = 3)

  final case class TaskResult[T](result: T)
}
