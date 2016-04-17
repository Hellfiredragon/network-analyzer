package analyzer.config

import com.typesafe.config.ConfigFactory

import scala.language.implicitConversions
import scala.collection.JavaConversions._

sealed trait OperatingSystem {
    val value: String
}

object OperatingSystem {
    implicit def os2String(os: OperatingSystem): String = os.value
}

case object Windows extends OperatingSystem {
    val value = "windows"
}

case object Linux extends OperatingSystem {
    val value = "linux"
}

/**
  * Created by jan on 13.04.16.
  */
class AnalyzerConfig(resource: String = "application") {

    private val config = ConfigFactory.load(resource)
        .withFallback(ConfigFactory.defaultReference())
        .getConfig("network-analyzer")

    def os = if (System.getProperty("os.name").toLowerCase().contains("win")) Windows else Linux

    private val commands = config.getConfig("commands")

    def bufferSize = config.getInt("buffer-size")

    def tshark = config.getConfig("tshark").getString(os)

    def tsharkProcess = config.getConfig("process-names").getConfig("tshark").getString(os)

    def commandLine(command: String): Seq[String] = config.getConfig("command-line").getStringList(os) :+ command

    def killCommand(processId: Int) = commands.getConfig("kill").getString(os).format(processId)
}
