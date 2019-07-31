package observatory

import akka.actor.{Actor, Props}
import akka.event.LoggingReceive
import org.spark_project.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener

class TransferMain extends Actor  {
  val alice = context.actorOf(Props[BankAccount], "Alice")
  val bob = context.actorOf(Props[BankAccount], "Bob")

  alice ! BankAccount.Deposit(100)

  def receive = LoggingReceive {
    case BankAccount.Done => transfer(50)
  }

  def transfer(amount: BigInt): Unit = {
    val transaction = context.actorOf(Props[WireTransfer], "transfer")
    transaction ! WireTransfer.Transfer(alice, bob, amount)
    context.become(LoggingReceive {
      case WireTransfer.Done =>
        println("success")
        context.stop(self)
      case WireTransfer.Failed =>
        println("failed")
        context.stop(self)
    })
  }
}