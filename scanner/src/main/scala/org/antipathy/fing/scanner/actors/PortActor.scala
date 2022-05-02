package org.antipathy.fing.scanner.actors

import akka.actor.{ActorRef, Props}
import org.antipathy.fing.scanner.actors.traits.{FingCommandActor, LoggingActor}
import org.antipathy.fing.scanner.messages.{PingResult, PortResult, PortValues}
import org.antipathy.fing.scanner.actors.traits.FingCommandActor
import org.antipathy.fing.scanner.messages.PortValues

import scala.util.Try

class PortActor(pointActor: ActorRef) extends LoggingActor with FingCommandActor[Seq[PortValues]]  {

  private def conversionFunction: Array[String] => Seq[PortValues] =
    (items: Array[String]) => items.toSeq.map{ item =>
      val values = item.split(";")
      PortValues(
        name = Try(values(3)).getOrElse("Unknown"),
        number = Try(values(1).toInt).getOrElse(-1),
        protocol = Try(values(2)).getOrElse("Unknown")
      )
    }

  override def receive: Receive = {
    case ping: PingResult =>
      logger.info(s"${self.path.name}: Beginning port mapping")
      val command: SystemCommand = s"$prefix --servicescan ${ping.ipAddress}/32 --silent -o csv"
      val result = PortResult(
        ipAddress = ping.ipAddress,
        hostName = ping.hostName,
        hardwareAddress = ping.hardwareAddress,
        hardwareVendor = ping.hardwareVendor,
        time = ping.time,
        dnsName = ping.dnsName,
        pingResults = ping.pingResults,
        portValues = run(command, conversionFunction)
      )
      pointActor ! result
      logger.info(s"${self.path.name}: Port mapping complete")
  }
}

object PortActor {

  def props(pointActor: ActorRef): Props = Props(new PortActor(pointActor))
}
