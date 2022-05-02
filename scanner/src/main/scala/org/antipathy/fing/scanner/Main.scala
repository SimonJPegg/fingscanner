package org.antipathy.fing.scanner

import akka.actor.{ActorSystem, DeadLetter}
import akka.routing.RoundRobinPool
import com.influxdb.client.InfluxDBClientFactory
import org.antipathy.fing.scanner.actors._
import org.antipathy.fing.scanner.config.CliParser
import org.antipathy.fing.scanner.logging.Logging
import org.antipathy.fing.scanner.messages.BeginScan

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main extends Logging {

  def main(args: Array[String]): Unit = {
    CliParser(args) match {
      case Some(config) =>
        logger.info("Creating Influx client")
        val influxDBClient = InfluxDBClientFactory
          .create(config.influx.server, config.influx.token.toCharArray, config.influx.org, config.influx.bucket)

        logger.info("creating actors")
        val system = ActorSystem("AskSystem")
        val deadLetterActor = system.actorOf(DeadLetterActor.props(), "deadletter")
        val influxWriteActor = system.actorOf(InfluxWriteActor.props(influxDBClient.getWriteApiBlocking).withRouter(RoundRobinPool(config.poolSize)))
        val aggregationActor = system.actorOf(AggregationActor.props(influxWriteActor), "aggregator")
        val collectorActor = system.actorOf(CollectionActor.props(aggregationActor), "collector")
        val portActors = system.actorOf(PortActor.props(collectorActor).withRouter(RoundRobinPool(config.poolSize)), "portmapper")
        val pingActors = system.actorOf(PingActor.props(portActors).withRouter(RoundRobinPool(config.poolSize)), "pingmapper")
        val dnsActors = system.actorOf(DNSLookupActor.props(pingActors, config.dns.server).withRouter(RoundRobinPool(config.poolSize)), "dnsmapper")
        val scanActor = system.actorOf(ScanActor.props(dnsActors, collectorActor), "scanner")

        logger.info("scheduling events")
        system.eventStream.subscribe(deadLetterActor, classOf[DeadLetter])
        system.scheduler.scheduleAtFixedRate(1.second, config.schedule.minutes, scanActor, BeginScan(config.subnet))

      case None =>
        logger.error("No config supplied")
    }
  }
}
