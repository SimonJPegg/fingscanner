package org.antipathy.fing.scanner.messages

import org.joda.time.{DateTime, DateTimeZone}

case class ScanResult(
                       ipAddress: String,
                       hostName: String,
                       hardwareAddress: String,
                       hardwareVendor: String,
                       time: DateTime = DateTime.now(DateTimeZone.UTC)
                     )
