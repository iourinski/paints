import   scheduler._
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}


/**
  * Created by dmitri on 13/02/2017.
  */

// the testing is pretty straightforward-- we consider possible/impossible orders and data that is formatted wrongly
class PaintShopTest extends TestKit(ActorSystem("PaintOrderSystem")) with ImplicitSender
    with WordSpecLike with Matchers with BeforeAndAfterAll {
    override def afterAll {
      TestKit.shutdownActorSystem(system)
    }
    val worker = TestActorRef[Worker]
    val listener = TestActorRef[Listener]
    listener ! InitialSetup(4)
    val goodOrder = Order(5, 3, List(List(1,1,1), List(2,1,0,2,0), List(1,5,0) ))
    val badOrder = Order(1, 2, List(List(1,1,0), List(1,1,1)))
    val wrongFormat = Order(3, 2, List(List(1,1,0), List(1,7,0))) // there is no paint #7

    "A worker actor" must {
      s"""create an order from data that is consistent""" in {
        worker !  InputCase(1, goodOrder.customers, goodOrder.numPaints, listener)
        assert(
          worker.underlyingActor.processCase(goodOrder.customers, goodOrder.numPaints).deep == Array(1,0,0,0,0).deep
        )
      }
      s"""fail to create an order where customers have conflicting demands""" in {
        worker ! InputCase(2, badOrder.customers, badOrder.numPaints, listener)
        assert(
          worker.underlyingActor.processCase(badOrder.customers, badOrder.numPaints).deep == Array().deep
        )
      }
      s"""fail to create order from wrongly formatted data""" in {
        worker ! InputCase(3, wrongFormat.customers, wrongFormat.numPaints, listener)
        assert(
          worker.underlyingActor.processCase(wrongFormat.customers, wrongFormat.numPaints).deep == Array().deep
        )
      }
      s"""fail to understand anything else, but the order request""" in {
        worker ! InitialSetup(10)
        expectMsg(Error())
      }
    }
}
