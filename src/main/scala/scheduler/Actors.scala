package scheduler

import akka.actor.{Props, _}
import akka.event.Logging
import akka.routing.RoundRobinRouter

// definition of actors that process paint orders

// main actor, that routes and accumulates queries
class Master (nrOfWorkers: Int, listener: ActorRef) extends Actor {
  val workerRouter = context.actorOf(
    Props(new Worker()).withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter"
  )
  val log = Logging(context.system, this)
  // it can only send things to processing or complain about wrong format
  def receive = {
    case ProcessOrders(numCases, orders) =>
      if (numCases != orders.length) {
        log.error("The data is inconsistent, shutting down")
        context.system.shutdown()
      }
      listener ! InitialSetup(numCases)
      for (idx <- orders.indices)
        workerRouter ! InputCase(idx, orders(idx).customers, orders(idx).numPaints, listener)
    case _ =>
      log.error("Unknown message should not be here")
      context.system.shutdown()
  }
}

// actor that processes data-- one actor for one set of customers
class Worker extends Actor {
  // nothing wrongly formatted can not be here-- we presumably sanitized the data!
  def receive = {
    case InputCase(caseNumber, customers, numPaints, listener) =>
      listener ! ReportCase(caseNumber, processCase(customers, numPaints))
    case InputCaseHash(caseNumber, customers, numPaints, listener) =>
      listener ! ReportCase(caseNumber, processCaseWithHash(customers, numPaints))
    case _ =>
      sender ! Error()
  }

  def processCaseWithHash(customers: List[List[Int]], numPaints: Int): Array[Int] = {
    val result = Array.fill(numPaints)(0)
    var next = true
    val mattes = scala.collection.mutable.Map[Int, Boolean]()
    // go thru customers, order matte if needed, if matte is added to list need go back and check
    // the need for further checks lapses if no changes were made during a pass
    while (next) {
      next = false
      for (idx <- customers.indices) {
        val customer = customers(idx)
        var ok = false
        var counter = 1
        // minimal data sanity check, return empty if data is inconsistent
        if (customer.length != customer(0) * 2 + 1)
          return Array()
        for (i <- 0 to customer(0) - 1) {
          val paintNumber = customer(i * 2 + 1) - 1
          // one more check
          if (paintNumber > result.length - 1)
            return Array()
          val paintType = customer(i * 2 + 2)
          if (paintType == 1) {
            if (mattes.getOrElse(paintNumber, false)){
              ok = true
            }
          } else {
            if (! mattes.contains(paintNumber)){
              ok = true
            }
          }
        }
        // if the customer is not satisfied, we can try to order matte for him (if he wants it)
        if (!ok) {
          for (j <- 1 to customer.length - 2 by 2) {
            // this is changing something in the result, we will need to go back and check again
            if (customer(j + 1) == 1) {
              mattes.+=((customer(j) -1, true))
              next = true
              ok = true
            }
          }
        }
        // if a customer is not satisfied, no need to go further, order is impossible
        if (!ok)
          return Array()
        // if everything is ok, we now need to check if we broke something if me made changes
      }
    }
    // now assign values to resulting array
    for (kv <- mattes) {
      result(kv._1) = 1
    }
    // this satisfies everybody, return
    return result
  }


  // the same as above except that we don't track matte separately, we just check the batch directly
  def processCase(customers: List[List[Int]], numPaints: Int): Array[Int] = {
    val result = Array.fill(numPaints)(0)
    var next = true
    // go thru customers, order matte if needed, if matte is added to list need go back and check
    // the need for further checks lapses if no changes were made during a pass
    while (next) {
      next = false
      for (idx <- customers.indices) {
        val customer = customers(idx)
        var ok = false
        var counter = 1
        // minimal data sanity check, return empty if data is inconsistent
        if (customer.length != customer(0) * 2 + 1)
          return Array()
        for (i <- 0 to customer(0) - 1) {
          val paintNumber = customer(i * 2 + 1) - 1
          // one more check
          if (paintNumber > result.length - 1)
            return Array()
          val paintType = customer(i * 2 + 2)
          if (result(paintNumber) == paintType) {
            ok = true
          }
        }
        // if the customer is not satisfied, we can try to order matte for him (if he wants it)
        if (!ok) {
          for (j <- 1 to customer.length - 2 by 2) {
            // this is changing something in the result, we will need to go back and check again
            if (customer(j + 1) == 1) {
              result(customer(j) - 1) = 1
              next = true
              ok = true
            }
          }
        }
        // if a customer is not satisfied, no need to go further, order is impossible
        if (!ok)
          return Array()
        // if everything is ok, we now need to check if we broke something if me made changes
      }
    }
    // this satisfies everybody, return result
    return result
  }
}

// actor for communicating with outside world and stopping the whole system
class Listener extends Actor {
  val log = Logging(context.system, this)
  var answers: Array[Array[Int]] = Array()
  var counter = 0
  def receive = {
    // set the initial array, that receives the results
    case InitialSetup(numCases) =>
      answers = Array.fill(numCases)(Array[Int]())
    // process answers
    case ReportCase(caseNumber,result) =>
      answers(caseNumber) = result
      counter = counter + 1
      // when all actors have reported, print the result out, and shut the whole thing down
      if (counter == answers.length) {
        for (idx <- answers.indices){
          if (answers(idx).length == 0){
            println("#" + (idx + 1) + ": IMPOSSIBLE")
          } else {
            println("#" + (idx + 1) + ": " + answers(idx).mkString(" ") )
          }
        }
        context.system.shutdown()
      }
    //complain if something meaningless arrives
    case  _ =>
      log.error("meaningless message, shutting down")
      context.system.shutdown()
  }
}

