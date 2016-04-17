package analyzer.capture

import analyzer.config.AnalyzerConfig
import analyzer.process._
import analyzer.reader.XmlStreamReader
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumeratee, Enumerator, Iteratee}

import scala.concurrent.Future
import scala.sys.process._
import scala.xml.Elem

/**
  * Created by Jan on 13.12.2015.
  */
case class CaptureInterface(path: String, name: String)

object CaptureInterface {

    val pattern = "\\d+[.] (.*) [(](.*)[)]".r

    def listInterfaces()(implicit config: AnalyzerConfig): Future[Seq[Either[String, CaptureInterface]]] = {
        val reader = new ProcessReader()
        val command = new TsharkCommandBuilder().listInterfaces().build()

        val result = reader.lines |>>> Iteratee.fold[String, Seq[Either[String, CaptureInterface]]](Nil)(
            (r, c) => c match {
                case pattern(a, b) => r :+ Right(CaptureInterface(a, b))
                case c: String => r :+ Left(c)
            }
        )

        command ! reader

        reader.close()

        result
    }

    def pdmlCapture(name: String, count: Int)(implicit config: AnalyzerConfig): (Enumerator[Elem], Enumerator[String]) = {
        if (count < 1) throw CaptureException("The specified packet count is less than 1.")

        val reader = new XmlStreamReader("packet")
        val builder = new TsharkCommandBuilder()
            .flushAlways()
            .captureInterface(name)
            .pdml()
        val command = if (count > 0) builder.capturePacketCount(count).build() else builder.build()

        Future {
            command ! reader
        } onComplete {
            case _ => reader.close()
        }

        (reader.xml, reader.error &> Enumeratee.filter(x => !x.contains("Capturing on") && !x.contains("packet captured")))
    }
}