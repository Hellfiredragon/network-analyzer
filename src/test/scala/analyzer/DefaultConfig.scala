package analyzer

import analyzer.config.AnalyzerConfig

/**
  * Created by jan on 13.04.16.
  */
trait DefaultConfig {

    implicit val config = new AnalyzerConfig()
}
