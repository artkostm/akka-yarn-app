package by.artsiom.bigdata201.yarn

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{BeforeAndAfterAll, Matchers, MustMatchers, WordSpecLike}

trait MultiNodeAppSpec
    extends MultiNodeSpecCallbacks
    with WordSpecLike
    with MustMatchers
    with BeforeAndAfterAll {
  override def beforeAll() = multiNodeSpecBeforeAll()

  override def afterAll() = multiNodeSpecAfterAll()
}
