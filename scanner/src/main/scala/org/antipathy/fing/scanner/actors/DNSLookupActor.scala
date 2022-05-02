package org.antipathy.fing.scanner.actors

import akka.actor.{ActorRef, Props}
import org.antipathy.fing.scanner.actors.traits.LoggingActor
import org.antipathy.fing.scanner.messages.{DNSResult, ScanResult}
import org.xbill.DNS.{Lookup, PTRRecord, Resolver, SimpleResolver, Type}

import scala.util.Try

class DNSLookupActor(pingActors: ActorRef, resolver: Resolver) extends LoggingActor {

  override def receive: Receive = {
    case scan:  ScanResult =>
      logger.info(s"${self.path.name}: Beginning DNS Lookup")
      val result = DNSResult(
        ipAddress = scan.ipAddress,
        hostName = scan.hostName,
        hardwareVendor = scan.hardwareVendor,
        hardwareAddress = scan.hardwareAddress,
        time = scan.time,
        dnsName = Try {
          val hostName = s"${scan.ipAddress.split('.').reverse.mkString(".")}.in-addr.arpa"
          val lookup = new Lookup(hostName, Type.PTR)
          lookup.setResolver(resolver)
          val result = lookup.run()
          result(0).asInstanceOf[PTRRecord].getTarget.toString
        }.toOption
      )
      pingActors ! result
      logger.info(s"${self.path.name}: DNS Lookup complete")
  }
}

object DNSLookupActor {

  def props(pingActors: ActorRef, dnsServer: Option[String]): Props = {
    val resolver = new SimpleResolver(dnsServer.getOrElse("8.8.8.8"))
    Props(new DNSLookupActor(pingActors, resolver))
  }
}
