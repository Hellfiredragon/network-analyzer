package analyzer.process

import analyzer.DefaultConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Jan on 13.12.2015.
  */
class TsharkCommandBuilderSpecs
    extends FlatSpec
        with Matchers
        with ScalaFutures
        with DefaultConfig {

    "A TsharkCommandBuilder" should "build commands" in {
        val builder = new TsharkCommandBuilder()

        builder.build() should be(Seq(config.tshark))
        builder.captureFilter("tcp").build() should be(Seq(config.tshark, "-f", "\"tcp\""))
        builder.displayFilter("tcp").build() should be(Seq(config.tshark, "-Y", "\"tcp\""))
        builder.file("test").build() should be(Seq(config.tshark, "-r", "test"))
        builder.flushAlways().build() should be(Seq(config.tshark, "-l"))
        builder.pdml().build() should be(Seq(config.tshark, "-T", "pdml"))
        builder.captureInterface("interface").build() should be(Seq(config.tshark, "-i", "interface"))
        builder.listInterfaces().build() should be(Seq(config.tshark, "-D"))
        builder.capturePacketCount(100).build() should be(Seq(config.tshark, "-c", "100"))
        builder.fields("a", "b").build() should be(Seq(config.tshark, "-T", "fields", "-e", "a", "-e", "b"))
    }
}
