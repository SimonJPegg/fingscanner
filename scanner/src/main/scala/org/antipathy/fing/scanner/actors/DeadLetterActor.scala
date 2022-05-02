package org.antipathy.fing.scanner.actors

import akka.actor.{DeadLetter, Props}
import org.antipathy.fing.scanner.actors.traits.LoggingActor

class DeadLetterActor extends LoggingActor {


  def receive: Receive = {
    case deadLetter: DeadLetter =>
      logger.error(
        s"saw dead letter ${deadLetter.message}, from ${deadLetter.sender.path}"
      )
  }
}

object DeadLetterActor {
  def props(): Props = Props(new DeadLetterActor())
}
