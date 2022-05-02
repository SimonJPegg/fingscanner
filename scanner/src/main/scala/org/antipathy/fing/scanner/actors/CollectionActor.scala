package org.antipathy.fing.scanner.actors

import akka.actor._
import org.antipathy.fing.scanner.actors.traits.LoggingActor
import org.antipathy.fing.scanner.messages.{BeginCollecting, PortResult, ScanComplete}
import org.antipathy.fing.scanner.messages.ScanComplete


class CollectionActor(aggregationActor: ActorRef) extends LoggingActor {
  override def receive: Receive = {
    case BeginCollecting(expected) =>
      logger.info(s"Awaiting $expected results")
      context.become(collecting(expected, List.empty[PortResult]))
  }

  def collecting(expected: Int, events: Seq[PortResult]) : Receive ={
    case p : PortResult =>
      if (expected == events.length + 1) {
        aggregationActor ! ScanComplete(events :+ p)
        logger.warn(s"Got $expected, waiting for next round")
        context.become(receive)
      } else {
        logger.info(s"${(events :+ p).length} of $expected received")
        context.become(collecting(expected, events :+ p))
      }
  }
}

object CollectionActor {

  def props(aggregationActor: ActorRef): Props = Props(new CollectionActor(aggregationActor))
}
