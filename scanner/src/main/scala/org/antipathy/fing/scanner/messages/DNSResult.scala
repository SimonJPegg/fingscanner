package org.antipathy.fing.scanner.messages

import org.joda.time.DateTime

case class DNSResult(
                       ipAddress: String,
                       hostName: String,
                       hardwareAddress: String,
                       hardwareVendor: String,
                       time: DateTime,
                       dnsName: Option[String]
                     )
