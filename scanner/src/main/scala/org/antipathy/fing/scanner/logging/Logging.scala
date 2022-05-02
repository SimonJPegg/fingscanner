package org.antipathy.fing.scanner.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Base trait for logging
 */
trait Logging {

  val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)
}
