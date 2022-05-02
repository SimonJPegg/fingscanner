package org.antipathy.fing.scanner.actors

import akka.actor.{ActorRef, Props}
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import org.antipathy.fing.scanner.actors.traits.LoggingActor
import org.antipathy.fing.scanner.messages.{Measurement, ScanComplete}
import org.antipathy.fing.scanner.messages.Measurement

import scala.jdk.CollectionConverters._

class AggregationActor(influxWriteActor: ActorRef) extends LoggingActor {

  override def receive: Receive = {
    case ScanComplete(results) =>

      val totalPoint = Point.measurement("Nodes")
        .time(results.head.time.getMillis,WritePrecision.MS).
        addField("total", results.length)

      val pingPoints = results.flatMap{ result =>
        result.pingResults.map{ ping =>
          Point.measurement("Latency")
            .time(ping.time.getMillis, WritePrecision.MS)
            .addTags(Map(
              "DNSName" -> result.dnsName.getOrElse("Unknown"),
              "IPAddress" -> result.ipAddress,
              "Vendor" -> result.hardwareVendor
            ).asJava)
            .addField("avg", ping.avg)
            .addField("min", ping.min)
            .addField("max", ping.max)
            .addField("loss", ping.loss)

        }
      }
      val portPoints = results.flatMap{ result =>
        result.portValues.map{ port =>
          Point.measurement("OpenPort")
            .time(port.time.getMillis, WritePrecision.MS)
            .addTags(Map(
              "DNSName" -> result.dnsName.getOrElse("Unknown"),
              "IPAddress" -> result.ipAddress,
              "Vendor" -> result.hardwareVendor
            ).asJava)
            .addField("name", port.name)
            .addField("protocol", port.protocol)
            .addField("count", 1)
        }
      }

      val totalPortPoints = results.flatMap(_.portValues).groupBy(_.name).map{
        case (name, values) =>
          Point.measurement("Ports")
            .time(values.head.time.getMillis, WritePrecision.MS)
            .addField(name, values.length)
      }
      (Seq(totalPoint) ++ pingPoints ++ portPoints ++ totalPortPoints).foreach{ point =>
        influxWriteActor ! Measurement(point)
      }
  }
}

object AggregationActor {
  def props(influxWriteActor: ActorRef): Props = Props(new AggregationActor(influxWriteActor))
}
