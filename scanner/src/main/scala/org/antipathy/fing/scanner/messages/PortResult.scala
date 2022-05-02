package org.antipathy.fing.scanner.messages

import org.joda.time.{DateTime, DateTimeZone}

case class PortResult(
                       ipAddress: String,
                       hostName: String,
                       hardwareAddress: String,
                       hardwareVendor: String,
                       dnsName: Option[String],
                       time: DateTime,
                       pingResults: Seq[PingValues],
                       portValues: Seq[PortValues]
                     )

case class PortValues(
                       name: String,
                       number: Int,
                       protocol: String,
                       time: DateTime = DateTime.now(DateTimeZone.UTC)
                     )
