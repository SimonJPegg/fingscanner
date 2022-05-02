package org.antipathy.fing.scanner.actors.traits

import akka.actor.Actor
import org.antipathy.fing.scanner.logging.Logging

trait LoggingActor extends Actor with Logging {

  private val className: String = this.getClass.getSimpleName

  logger.debug(s"$className started!")

  override def preStart: Unit = { logger.debug(s"$className: preStart") }

  override def postStop: Unit = { logger.debug(s"$className: postStop") }

  override def preRestart(reason: Throwable, message: Option[Any]) : Unit = {
    logger.debug(s"$className: preRestart")
    logger.debug(s"$className reason: ${reason.getMessage}")
    logger.debug(s"$className message: ${message.getOrElse("")}")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable) : Unit = {
    logger.debug(s"$className: postRestart")
    logger.debug(s"$className reason: ${reason.getMessage}")
    super.postRestart(reason)
  }
}
