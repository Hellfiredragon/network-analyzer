package analyzer.process

import analyzer.config.AnalyzerConfig

/**
  * Created by jan on 16.04.16.
  */
class TsharkCommandBuilder(commands: Seq[String] = Nil)(implicit config: AnalyzerConfig) {

    private def appendCommands(cs: String*): TsharkCommandBuilder = new TsharkCommandBuilder(commands ++ cs)

    def captureFilter(filter: String): TsharkCommandBuilder = appendCommands("-f", "\"" + filter + "\"")

    def capturePacketCount(count: Int): TsharkCommandBuilder = appendCommands("-c", count.toString)

    def displayFilter(filter: String): TsharkCommandBuilder = appendCommands("-Y", "\"" + filter + "\"")

    def file(file: String): TsharkCommandBuilder = appendCommands("-r", file)

    def flushAlways(): TsharkCommandBuilder = appendCommands("-l")

    def pdml(): TsharkCommandBuilder = appendCommands("-T", "pdml")

    def fields(fields: String*): TsharkCommandBuilder = appendCommands(Seq("-T", "fields") ++ fields.flatMap(s => Seq("-e", s)): _*)

    def captureInterface(interface: String): TsharkCommandBuilder = appendCommands("-i", interface)

    def listInterfaces(): TsharkCommandBuilder = appendCommands("-D")

    def build(): Seq[String] = Seq(config.tshark) ++ commands
}
