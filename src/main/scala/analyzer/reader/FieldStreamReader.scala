package analyzer.reader

import analyzer.config.AnalyzerConfig
import analyzer.process.ProcessReader
import play.api.libs.iteratee.Enumeratee

import scala.concurrent.ExecutionContext

/**
  * Created by jan on 16.04.16.
  */
class FieldStreamReader(splitter: String)(implicit config: AnalyzerConfig, ec: ExecutionContext) extends ProcessReader {

    val enumeratee: Enumeratee[String, Seq[String]] = Enumeratee.map[String](s => s.split(splitter))

    val fields = lines.through(enumeratee)
}
