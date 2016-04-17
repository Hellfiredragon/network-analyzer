package analyzer.process

import analyzer.config.AnalyzerConfig
import play.api.libs.iteratee.Concurrent

import scala.sys.process.ProcessLogger

/**
  * Created by jan on 13.04.16.
  */
class ProcessReader(implicit config: AnalyzerConfig) {

    private val (stdOut, channelOut) = Concurrent.broadcast[String]

    private val (stdErr, channelErr) = Concurrent.broadcast[String]

    private[process] val logger = ProcessLogger(channelOut.push, channelErr.push)

    val lines = stdOut &> Concurrent.buffer(config.bufferSize)

    val error = stdErr &> Concurrent.buffer(config.bufferSize)

    def close(): Unit = {
        channelOut.eofAndEnd()
        channelErr.eofAndEnd()
    }
}
