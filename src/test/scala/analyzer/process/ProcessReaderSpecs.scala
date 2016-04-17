package analyzer.process

import java.io.IOException

import analyzer.{DefaultConfig, TestUtils}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Iteratee

import scala.concurrent.Future
import scala.sys.process._

/**
  * Created by jan on 13.04.16.
  */
class ProcessReaderSpecs
    extends FlatSpec
        with Matchers
        with ScalaFutures
        with DefaultConfig {

    "A ProcessReader" should "read lines" in {
        val reader = new ProcessReader()
        val command = config.commandLine("echo test")

        val result = reader.lines |>>> Iteratee.fold[String, String]("")((r, c) => r + c)

        command ! reader

        reader.close()

        whenReady(result) { s =>
            s should be("test")
        }
    }

    it should "read multiple lines" in {
        val reader = new ProcessReader()
        val command = TestUtils.forCommand(10, "echo test")

        val result = reader.lines |>>> Iteratee.fold[String, String]("")((r, c) => r + ";" + c)

        command ! reader

        reader.close()

        whenReady(result) { s =>
            s should be(";test;test;test;test;test;test;test;test;test;test")
        }
    }

    it should "fail with IOException" in {
        val reader = new ProcessReader()
        val command = "i-am-not-here-...-never-ever-:D test"

        val result = Future {
            command !! reader
        }

        reader.close()

        whenReady(result.failed) { ex =>
            ex shouldBe an[IOException]
        }
    }

    it should "fail with ProcessException" in {
        val reader = new ProcessReader()
        val command = config.commandLine("ls i-am-not-here-...-never-ever-:D")

        val result = Future {
            command !! reader
        }

        reader.close()

        whenReady(result.failed) { ex =>
            ex shouldBe an[RuntimeException]
        }
    }
}
