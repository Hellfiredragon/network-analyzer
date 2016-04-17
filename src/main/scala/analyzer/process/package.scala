package analyzer

import scala.sys.process.ProcessLogger

/**
  * Created by jan on 13.04.16.
  */
package object process {

    implicit def processReaderToProcessLogger(reader: ProcessReader): ProcessLogger = reader.logger
}
