package org.antipathy.fing.scanner.actors

import akka.actor.Props
import com.influxdb.client.{WriteApiBlocking => InfluxClient}
import org.antipathy.fing.scanner.actors.traits.LoggingActor
import org.antipathy.fing.scanner.messages.Measurement

class InfluxWriteActor(client: InfluxClient) extends LoggingActor {

  override def receive: Receive = {
    case Measurement(point) =>
      try {
        client.writePoint(point)
      } catch {
        case e: Exception => logger.error("Error writing point",e)
      }
      logger.info("Wrote point to influx")
  }
}

object InfluxWriteActor {

  def props(client: InfluxClient) = Props(new InfluxWriteActor(client))
}
