package org.antipathy.fing.scanner.messages

import com.influxdb.client.write.Point

case class Measurement(point: Point)
