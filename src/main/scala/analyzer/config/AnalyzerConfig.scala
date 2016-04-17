package analyzer.config

import com.typesafe.config.ConfigFactory;

/**
  * Created by jan on 13.04.16.
  */
class AnalyzerConfig(resource: String = "application") {

    private val config = ConfigFactory.load(resource)
        .withFallback(ConfigFactory.defaultReference())
        .getConfig("network-analyzer")

    private val commands = config.getConfig("commands")

    def bufferSize = config.getInt("buffer-size")

    def tshark = config.getString("tshark")

    def killCommand(processId: Int) = commands.getString("kill").format(processId)
}
