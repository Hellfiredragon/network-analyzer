package analyzer.config

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by jan on 13.04.16.
  */
class AnalyzerConfigSpecs
    extends FlatSpec
        with Matchers
        with ScalaFutures {

    "A Config" should "return config parameter" in {
        val config = new AnalyzerConfig()
        config.bufferSize should be(100000)
        config.tshark should be (config.os match {
	        case Windows => "C:\\Program Files\\Wireshark\\tshark.exe"
	        case Linux => "/usr/bin/tshark"
        })
	    config.commandLine("blub") should be(config.os match {
		    case Windows => Seq("cmd", "/c", "blub")
		    case Linux => Seq("bash","-c", "blub")
	    })
        config.killCommand(1) should be("kill -9 1")
    }
}
