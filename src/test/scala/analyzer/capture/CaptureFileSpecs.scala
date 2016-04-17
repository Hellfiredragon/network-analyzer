package analyzer.capture

import analyzer.DefaultConfig
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
        val (elements, error) = CaptureFile.pdmlCapture("traces/aaa.pcap")

        val result = elements |>>> Iteratee.fold[Elem, Seq[Elem]](Nil)((r, c) => r :+ c)
        val errorResult = error |>>> Iteratee.fold[String, String]("")((r, c) => r + c)

        whenReady(errorResult) { err =>
            err should be("")
        }
        whenReady(result) { x =>
            x.length should be(691)
        }
    }

    it should "capture fields" in {
        val (elements, error) = CaptureFile.fieldsCapture("traces/aaa.pcap", "ip.src", "ip.dst", "arp.src.proto_ipv4", "arp.dst.proto_ipv4")

        val result = elements |>>> Iteratee.fold[Seq[String], Seq[Seq[String]]](Nil)((r, c) => r :+ c)
        val errorResult = error |>>> Iteratee.fold[String, String]("")((r, c) => r + c)
		error |>>> Iteratee.foreach(println)

        whenReady(errorResult) { err =>
            err should be("")

	        whenReady(result) { x =>
		        x.length should be(691)
		        x.head should be(Seq("192.168.1.2", "192.168.1.255"))
	        }
        }
    }

    def captureBigFile(): Unit = {
        val (elements, error) = CaptureFile.fieldsCapture("traces/general101d.pcapng", "ip.src", "ip.dst", "arp.src.proto_ipv4", "arp.dst.proto_ipv4")

        val result = elements |>>> Iteratee.fold[Seq[String], Seq[Seq[String]]](Nil)((r, c) => r :+ c)
        val errorResult = error |>>> Iteratee.fold[String, String]("")((r, c) => r + c)

        whenReady(errorResult) { err =>
            err should be("")
        }
        whenReady(result) { x =>
            x.length should be(37422)
            x.head should be(Seq("10.9.9.9", "10.10.10.10"))
        }
    }

    it should "capture a big file" in {
        for(i <- 1 to 5) captureBigFile()

        for(i <- 1 to 5) {
            var begin = System.nanoTime()
            captureBigFile()
            println(((System.nanoTime() - begin) / 1000 / 1000.0) + "ms")
        }
    }

    it should "doesn't exist" in {
        val file = "FDdsaffsadfasf"
        val (elements, error) = CaptureFile.pdmlCapture(file)

        val result = error |>>> Iteratee.fold[String, String]("")((r, c) => r + c)

        whenReady(result) { s =>
            s contains file should be(true)
        }
    }
}
