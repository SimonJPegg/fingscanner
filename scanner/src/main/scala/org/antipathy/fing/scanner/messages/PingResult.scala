package org.antipathy.fing.scanner.messages

import org.joda.time.{DateTime, DateTimeZone}

case class PingResult(
                       ipAddress: String,
                       hostName: String,
                       hardwareAddress: String,
                       hardwareVendor: String,
                       dnsName: Option[String],
                       time: DateTime,
                       pingResults: Seq[PingValues]
                     )

case class PingValues(
                       min: Long,
                       max: Long,
                       avg: Long,
                       loss: Long,
                       time: DateTime = DateTime.now(DateTimeZone.UTC)
                     )
