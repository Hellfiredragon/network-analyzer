package analyzer.capture

import analyzer.{TestUtils, DefaultConfig}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import scala.xml.Elem

/**
  * Created by Jan on 13.12.2015.
  */
class CaptureFileSpecs
    extends FlatSpec
        with Matchers
        with ScalaFutures
        with DefaultConfig {

    implicit override val patienceConfig = PatienceConfig(Span(30, Seconds))

    "A CaptureFile" should "capture pdml" in {
        val (elementStream, errorStream) = CaptureFile.pdmlCapture("traces/aaa.pcap")

        val result = elementStream |>>> TestUtils.countResult()
        val errorResult = errorStream |>>> TestUtils.errorResult()

        whenReady(errorResult) { err =>
            err should be("")
        }
        whenReady(result) { x =>
            x should be(691)
        }
    }

    it should "capture fields" in {
        val (elementStream, errorStream) = CaptureFile.fieldsCapture("traces/aaa.pcap", "ip.src", "ip.dst", "arp.src.proto_ipv4", "arp.dst.proto_ipv4")

        val result = elementStream |>>> TestUtils.countResult()
        val errorResult = errorStream |>>> TestUtils.errorResult()

        whenReady(errorResult) { err =>
            err should be("")

            whenReady(result) { x =>
                x should be(691)
            }
        }
    }

    it should "capture a big file" in {
        val (elementStream, errorStream) = CaptureFile.fieldsCapture("traces/general101d.pcapng", "ip.src", "ip.dst", "arp.src.proto_ipv4", "arp.dst.proto_ipv4")

        val result = elementStream |>>> TestUtils.countResult()
        val errorResult = errorStream |>>> TestUtils.errorResult()

        whenReady(errorResult) { err =>
            err should be("")
        }
        whenReady(result) { x =>
            x should be(37422)
        }
    }

    it should "doesn't exist" in {
        val file = "FDdsaffsadfasf"
        val (elementStream, errorStream) = CaptureFile.pdmlCapture(file)

        val result = errorStream |>>> Iteratee.fold[String, String]("")((r, c) => r + c)

        whenReady(result) { s =>
            s contains file should be(true)
        }
    }
}
