package org.antipathy.fing.scanner.messages

case class ScanComplete(
                        results: Seq[PortResult]
                       )
