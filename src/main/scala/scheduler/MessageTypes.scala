package scheduler

import akka.actor.ActorRef
// this not quite a message type, but a data structure that describes a set of customer orders
case class Order(numPaints: Int, numCustomers: Int, customers: List[List[Int]])

//messages for information exchange between actors
case class InitialSetup(numCases: Int)
case class ProcessOrders(numCases: Int, orders: List[Order])
case class InputCase(caseNumber: Int, customers: List[List[Int]], numPaints: Int, listener: ActorRef)
case class InputCaseHash(caseNumber: Int, customers: List[List[Int]], numPaints: Int, listener: ActorRef)
case class ReportCase(caseNumber: Int, result: Array[Int])
case class Error()

