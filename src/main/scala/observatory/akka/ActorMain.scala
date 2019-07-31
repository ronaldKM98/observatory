package observatory

import akka.actor.Actor
import akka.actor.Props

class ActorMain extends Actor {
  val counter = context.actorOf(Props[Counter], "counter")

  counter ! "incr"
  counter ! "incr"
  counter ! "incr"
  counter ! "get"

  override def receive: Receive = {
    case count: Int =>
      println(s"count was $count")
      context.stop(self)
  }
}