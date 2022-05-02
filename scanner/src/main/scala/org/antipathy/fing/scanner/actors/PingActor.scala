package org.antipathy.fing.scanner.actors

import akka.actor.{ActorRef, Props}
import org.antipathy.fing.scanner.actors.traits.{FingCommandActor, LoggingActor}
import org.antipathy.fing.scanner.messages.{DNSResult, PingResult, PingValues}
import org.antipathy.fing.scanner.actors.traits.FingCommandActor
import org.antipathy.fing.scanner.messages.PingValues

import scala.util.Try

class PingActor(portActors: ActorRef) extends LoggingActor with FingCommandActor[Seq[PingValues]]  {

  private def conversionFunction: Array[String] => Seq[PingValues] =
    (items: Array[String]) => items.toSeq.map{ item =>
      val values = item.split(";")
      PingValues(
        avg = Try(values(1).toLong).toOption.getOrElse(-1),
        loss = Try(values(2).toLong).toOption.getOrElse(0),
        min = Try(values(3).toLong).toOption.getOrElse(-1),
        max = Try(values(4).toLong).toOption.getOrElse(-1)
      )
    }

  override def receive: Receive = {
    case dns: DNSResult =>
      logger.info(s"${self.path.name}: Beginning ping")
      val command: SystemCommand = s"$prefix -p ${dns.ipAddress} --silent -o csv"
      val result  = PingResult(
        ipAddress = dns.ipAddress,
        hostName = dns.hostName,
        hardwareAddress = dns.hardwareAddress,
        hardwareVendor = dns.hardwareVendor,
        dnsName = dns.dnsName,
        time = dns.time,
        pingResults = run(command, conversionFunction)
      )
      portActors ! result
      logger.info(s"${self.path.name}: ping complete")
  }
}

object PingActor {
  def props(portActors: ActorRef): Props = Props(new PingActor(portActors))
}
