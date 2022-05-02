package org.antipathy.fing.scanner.actors.traits

import sys.process._

trait FingCommandActor[T] {
  protected def prefix = "/usr/bin/fing "
  //protected def prefix = "docker run --rm --net=host gmacario/fing"
  type SystemCommand = String

  protected def run(command: SystemCommand, conversionFunction: Array[String] => T): T = {
    val result = command.!!
    conversionFunction(result.split(System.lineSeparator()))
  }

}
