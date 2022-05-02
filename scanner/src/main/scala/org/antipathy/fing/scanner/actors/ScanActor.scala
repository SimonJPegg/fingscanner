package org.antipathy.fing.scanner.actors

import akka.actor.{ActorRef, Props}
import org.antipathy.fing.scanner.actors.traits.{FingCommandActor, LoggingActor}
import org.antipathy.fing.scanner.messages.{BeginCollecting, BeginScan, ScanResult}
import org.antipathy.fing.scanner.actors.traits.FingCommandActor
import org.antipathy.fing.scanner.messages.BeginScan

import scala.util.Try

class ScanActor(dnsActors: ActorRef, collectionActor: ActorRef) extends LoggingActor with FingCommandActor[Seq[ScanResult]]  {

  override def receive: Receive = {
    case BeginScan(subnet) =>
      logger.info(s"${self.path.name}: Beginning scan event")
      val command : SystemCommand = s"$prefix -r 1 -n $subnet -d on --silent -o table,csv"
      val results = run(command, conversionFunction)

      collectionActor ! BeginCollecting(results.length)

      results.foreach{ result =>
        dnsActors ! result
      }

      logger.info(s"${self.path.name}: Scan event complete")
  }

  private def conversionFunction: Array[String] => Seq[ScanResult] =
    (items: Array[String]) => items.map{ item =>
      val values = item.split(";")
      ScanResult(
        ipAddress = Try(values(0)).getOrElse("Unknown"),
        hostName = Try(values(4)).getOrElse("Unknown"),
        hardwareAddress = Try(values(5)).getOrElse("Unknown"),
        hardwareVendor = Try(values(6)).getOrElse("Unknown")
      )
    }.toSeq
}

object ScanActor {

  def props(dnsActors: ActorRef, collectionActor: ActorRef): Props = Props(new ScanActor(dnsActors, collectionActor))
}
