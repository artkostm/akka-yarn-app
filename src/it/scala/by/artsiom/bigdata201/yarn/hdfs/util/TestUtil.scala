package by.artsiom.bigdata201.yarn.hdfs.util

import java.nio.ByteBuffer

import akka.util.ByteString
import org.scalatest.Matchers

import scala.language.higherKinds
import scala.util.Random

sealed trait TestUtils {

  protected type Sequence[_]
  protected type Pair[_, _]
  protected type Assertion

  def destination = "/tmp/akka-yarn/"

  def generateFakeContent(count: Double, bytes: Long): Sequence[ByteString]

  def generateFakeContentWithPartitions(count: Double,
                                        bytes: Long,
                                        partition: Int): Sequence[ByteString]
}

object ScalaTestUtils extends TestUtils with Matchers {
  type Sequence[A] = Seq[A]
  type Pair[A, B]  = (A, B)
  type Assertion   = org.scalatest.Assertion

  def generateFakeContent(count: Double, bytes: Long): Sequence[ByteString] =
    ByteBuffer
      .allocate((count * bytes).toInt)
      .array()
      .toList
      .map(_ => Random.nextPrintableChar)
      .map(ByteString(_))

  def generateFakeContentWithPartitions(count: Double,
                                        bytes: Long,
                                        partition: Int): Sequence[ByteString] = {
    val fakeData  = generateFakeContent(count, bytes)
    val groupSize = Math.ceil(fakeData.size / partition.toDouble).toInt
    fakeData.grouped(groupSize).map(list => ByteString(list.map(_.utf8String).mkString)).toList
  }
}
