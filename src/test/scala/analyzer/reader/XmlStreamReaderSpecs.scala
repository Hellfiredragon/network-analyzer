package analyzer.reader

import analyzer.{DefaultConfig, TestUtils}
import analyzer.config.{Linux, Windows}
import analyzer.process._
import org.scalatest.StreamlinedXmlEquality._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Iteratee

import scala.sys.process._
import scala.xml.Elem

/**
  * Created by Jan on 11.12.2015.
  */
class XmlStreamReaderSpecs
    extends FlatSpec
        with Matchers
        with ScalaFutures
        with DefaultConfig {

    implicit override val patienceConfig = PatienceConfig(Span(5, Seconds))

    "A XmlReader" should "read xml" in {
        val reader = new XmlStreamReader("foo")

        val result = reader.xml |>>> Iteratee.fold[Elem, Seq[Elem]](Nil)((r, c) => r ++ Seq(c))

        config.os match {
            case Windows => {
                config.commandLine("echo ^<foo^>") ! reader should be(0)
                config.commandLine("echo ^<bar^>") ! reader should be(0)
                config.commandLine("echo ^<baz/^>") ! reader should be(0)
                config.commandLine("echo ^</bar^>") ! reader should be(0)
                config.commandLine("echo ^</foo^>") ! reader should be(0)
            }
            case Linux => {
                config.commandLine("echo <foo>") ! reader should be(0)
                config.commandLine("echo <bar>") ! reader should be(0)
                config.commandLine("echo <baz/>") ! reader should be(0)
                config.commandLine("echo </bar>") ! reader should be(0)
                config.commandLine("echo </foo>") ! reader should be(0)
            }
        }


        reader.close()

        whenReady(result) { s =>
            s.head === <foo>
                <bar>
                    <baz/>
                </bar>
            </foo>
        }
    }

    it should "read multiple elements" in {
        val reader = new XmlStreamReader("foo")
        val command = config.os match {
            case Windows => TestUtils.forCommand(5, "echo \"^<foo^>^<bar^>^<baz/^>^</bar^>^</foo^>\"")
            case Linux => TestUtils.forCommand(5, "echo \"<foo><bar><baz/></bar></foo>\"")
        }

        val result = reader.xml |>>> Iteratee.fold[Elem, Seq[Elem]](Nil)((r, c) => r ++ Seq(c))
        val error = reader.error |>>> Iteratee.fold("")((r, c) => r + c)

        command ! reader
        reader.close()

        whenReady(error) { err =>
            err should be("")
        }
        whenReady(result) { s =>
            val expected = <foo>
                <bar>
                    <baz/>
                </bar>
            </foo>
            val expectedList = List(expected, expected, expected, expected, expected)
            s === expectedList
        }
    }

    it should "throw errors" in {
        val file = "dfaskjlfsadjklfdsöfdsl"
        val reader = new XmlStreamReader("foo")
        val command = config.os match {
            case Windows => config.commandLine(s"dir $file")
            case Linux => config.commandLine(s"ls $file")
        }

        val result = reader.error |>>> Iteratee.fold[String, String]("")((r, c) => r + c)

        command ! reader should not be(0)
        reader.close()
    }
}
