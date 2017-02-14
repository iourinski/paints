/**
  * Created by dmitri on 12/02/2017.
  */

import java.io.File

import akka.actor.{Props, _}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scheduler._

object PaintShop extends App {
  override def main(args: Array[String]): Unit = {
    // read simple config from resources/application.conf (slightly excessive, there is not much to record there)
    val config = ConfigFactory.load()
    val numWorkers = config.getInt("master.numWorkers")
    var inputFilePath = ""
    try {
      inputFilePath = args(0)
    } catch {
      case e: IndexOutOfBoundsException => println("You need to pass a file with urls in command line")
    }
    if (inputFilePath != "") {
      val system = ActorSystem("PaintOrderSystem")
      // create the result listener, which will print the result and shutdown the system
      val listener = system.actorOf(Props(new Listener))
      // create main actor that will distribute requests to a poll of workers
      val master = system.actorOf(Props(new Master(numWorkers, listener)), name = "master")
      // read orders from given file, prepare and minimally sanitize data
      val (numOrders, orders) = readFile(inputFilePath)
      // send data for processing
      master ! ProcessOrders(numOrders, orders)
    }
  }

  // read data according to described format
  def readFile(inputFilePath: String): (Int, List[Order]) = {
    val file  = new File(inputFilePath)
    val lines = ListBuffer[String]()
    var numOrders = 0
    val orders = ListBuffer[Order]()
    if (file.exists()){
      for (line <- Source.fromFile(file).getLines()){
        lines.+=(line)
      }
      var offset = 1
      numOrders = lines(0).toInt
      for (x <- 1 to lines(0).toInt ){
        val numPaints = lines(offset).toInt
        offset += 1
        val numCustomers = lines(offset).toInt
        val customers = ListBuffer[List[Int]]()
        offset += 1
        for (i <- 0 to numCustomers - 1 ) {
          val customer = lines(offset).split(" ").map(_.toInt).toList
          customers.+=(customer)
          offset += 1
        }
        orders.+=(Order(numPaints,numCustomers,customers.toList))
      }
    } else {
      println("The file with order list doesn't exist")
    }
    return (numOrders, orders.toList)
  }
}
