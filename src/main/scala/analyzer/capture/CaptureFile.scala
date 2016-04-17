package analyzer.capture

import analyzer.config.AnalyzerConfig
import analyzer.process.TsharkCommandBuilder
import analyzer.reader.{FieldStreamReader, XmlStreamReader}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumeratee, Enumerator}

import scala.concurrent.Future
import scala.sys.process._
import scala.xml.Elem

/**
  * Created by jan on 16.04.16.
  */
object CaptureFile {

    def pdmlCapture(name: String)(implicit config: AnalyzerConfig): (Enumerator[Elem], Enumerator[String]) = {
        val reader = new XmlStreamReader("packet")
        val command = new TsharkCommandBuilder()
            .file(name)
            .pdml()
            .build()

        Future {
            command ! reader
        } onComplete {
            case _ => reader.close()
        }

        (reader.xml, reader.error &> Enumeratee.filter(x => !x.contains("Capturing on") && !x.contains("packet captured")))
    }

    def fieldsCapture(name: String, fields: String*)(implicit config: AnalyzerConfig): (Enumerator[Seq[String]], Enumerator[String]) = {
        val reader = new FieldStreamReader("\t")
        val command = new TsharkCommandBuilder()
            .file(name)
            .fields(fields: _*)
            .build()

        Future {
            command ! reader
        } onComplete {
            case _ => reader.close()
        }

        (reader.fields, reader.error)
    }
}
