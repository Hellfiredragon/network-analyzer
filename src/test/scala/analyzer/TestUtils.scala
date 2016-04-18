package analyzer

import analyzer.config.{AnalyzerConfig, Linux, Windows}
import play.api.libs.iteratee.Iteratee
import play.api.libs.concurrent.Execution.Implicits._

/**
  * Created by Jan on 17.04.2016.
  */
object TestUtils {
    def forCommand(count: Int, command: String)(implicit config: AnalyzerConfig) = config.os match {
        case Windows => config.commandLine(s"FOR /L %i IN (1, 1, $count) DO $command")
        case Linux => config.commandLine(s"for i in {1..$count}; do $command; done")
    }

    def printStream() = Iteratee.foreach(println)

    def countResult[E]() = Iteratee.fold[E, Int](0)((count, element) => count + 1)

    def errorResult() = Iteratee.fold[String, String]("")((error, line) => error + sys.props("line.separator") + line)
}
