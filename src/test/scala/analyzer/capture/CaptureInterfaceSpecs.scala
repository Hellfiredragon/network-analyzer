package analyzer.capture

import analyzer.DefaultConfig
import analyzer.process.{ProcessReader, _}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._

import scala.sys.process._
import scala.xml.Elem

/**
  * Created by Jan on 13.12.2015.
  */
class CaptureInterfaceSpecs
    extends FlatSpec
        with Matchers
        with ScalaFutures
        with DefaultConfig {

    implicit override val patienceConfig = PatienceConfig(Span(3, Seconds))

    "A CaptureInterface" should "list all interfaces" in {
        whenReady(CaptureInterface.listInterfaces()) { list =>
            list.length should be > 0
        }
    }

    it should "capture an packet" in {
        val reader = new ProcessReader
        val command = Seq("bash", "-c", "echo Hello | netcat localhost 80")
        val (elements, error) = CaptureInterface.pdmlCapture("lo", 1)

        val result = elements |>>> Iteratee.fold[Elem, Seq[Elem]](Nil)((r, c) => r :+ c)
        val errorResult = error |>>> Iteratee.fold[String, String]("")((r, c) => r + c)

        Thread.sleep(500) // We wait for tshark to start capture
        command ! reader

        reader.close()

        whenReady(errorResult) { err =>
            err should be("")
        }
        whenReady(result) { x =>
            x.length should be(1)
        }
    }

    it should "throw an excpetion if capture count less than 1" in {
        try {
            CaptureInterface.pdmlCapture("foo", 0)
            fail("capture has not thrown an exception")
        } catch {
            case CaptureException(message) => message should be("The specified packet count is less than 1.")
        }
    }
}
