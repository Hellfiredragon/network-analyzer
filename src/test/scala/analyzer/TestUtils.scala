package analyzer

import analyzer.config.{AnalyzerConfig, Linux, Windows}

/**
  * Created by Jan on 17.04.2016.
  */
object TestUtils {
    def forCommand(count: Int, command: String)(implicit config: AnalyzerConfig) = config.os match {
        case Windows => config.commandLine(s"FOR /L %i IN (1, 1, $count) DO $command")
        case Linux => config.commandLine(s"for i in {1..$count}; do $command; done")
    }


}
