package observatory

import akka.actor.Actor
import akka.event.LoggingReceive

object BankAccount {
  case class Deposit(amount: BigInt) {
    require(amount > 0)
  }

  case class Withdraw(amount: BigInt) {
    require(amount > 0)
  }

  case object Done
  case object Failed
}


// Actors are serialized inside. So no problem with Withdraw and Deposit
class BankAccount extends Actor {
  import BankAccount._

  var balance = BigInt(0)

  override def receive: Receive = LoggingReceive {
    case Deposit(amount) => balance += amount
                            sender ! Done
    case Withdraw(amount) => balance -= amount
                              sender ! Done
    case _ => sender ! Failed
  }
}