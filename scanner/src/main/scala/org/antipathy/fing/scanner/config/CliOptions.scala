package org.antipathy.fing.scanner.config

case class CliOptions(
                      subnet: String = "",
                      schedule: Int = 5,
                      poolSize: Int = 10,
                      influx: Influx = Influx(),
                      log: Log = Log(),
                      dns: DNS = DNS()
                     )

case class Influx(
                   server:String = "",
                   token:String = "",
                   org: String = "",
                   bucket: String  =""
                 )

case class Log(
                server:Option[String] = None
              )

case class DNS(
                server:Option[String] = None
              )
