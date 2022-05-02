package org.antipathy.fing.scanner.config

import scopt.OParser

object CliParser {

  def apply(args: Array[String]): Option[CliOptions] = {
    val builder = OParser.builder[CliOptions]
    import builder._
    val parser = OParser.sequence(
      programName("fingsscanner"),
      head("fingsscanner", "0.1"),
      opt[String]("subnet").action((s,c) => c.copy(subnet = s)).text("The subnet to scan").required(),
      opt[String]("influx-server").action((i, c) => c.copy(influx = c.influx.copy(server = i))).text("The influxDB server address").required(),
      opt[String]("influx-token").action((t, c) => c.copy(influx = c.influx.copy(token = t))).text("The influxDB auth token").required(),
      opt[String]("influx-org").action((o, c) => c.copy(influx = c.influx.copy(org = o))).text("The influxDB organisation").required(),
      opt[String]("influx-bucket").action((b, c) => c.copy(influx = c.influx.copy(bucket = b))).text("The influxDB bucket").required(),
      opt[String]("loki-server").action((l, c) => c.copy(log = c.log.copy(server = Some(l)))).text("The address of the loki server"),
      opt[String]("dns-server").action((d, c) => c.copy(dns = c.dns.copy(server = Some(d)))).text("The name of the DNS server"),
      opt[Int]("actor-pool").action((p, c) => c.copy(poolSize = p)).text("The size of the actor pool for each actor"),
      opt[Int]("interval").action((p, c) => c.copy(poolSize = p)).text("How often to run a scan in minutes")
    )
    OParser.parse(parser, args, CliOptions())
  }
}
