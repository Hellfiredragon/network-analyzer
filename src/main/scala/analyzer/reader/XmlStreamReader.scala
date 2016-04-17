package analyzer.reader

import analyzer.config.AnalyzerConfig
import analyzer.process.ProcessReader
import play.api.libs.iteratee.{Concurrent, Iteratee}

import scala.concurrent.ExecutionContext
import scala.xml.{Elem, XML}

/**
  * Created by Jan on 11.12.2015.
  */
class XmlStreamReader(chunkSplitter: String)(implicit config: AnalyzerConfig, ec: ExecutionContext) extends ProcessReader {

    private val elemStart = ("(<" + chunkSplitter + ">)").r

    private val elemEnd = ("(<\\/" + chunkSplitter + ">)").r

    private val (chunkEnumerator, chunkChannel) = Concurrent.broadcast[Elem]

    val sb = new StringBuilder

    val finish = lines |>>> Iteratee.foreach[String] {
        case elemStart(s) => {
            sb.setLength(0)
            sb.append(s)
        }
        case elemEnd(s) => {
            sb.append(s)
            chunkChannel.push(XML.loadString(sb.toString()))
            sb.setLength(0)
        }
        case s => {
            sb.append(s)
        }
    }

    val xml = chunkEnumerator &> Concurrent.buffer(config.bufferSize)

    override def close(): Unit = {
        super.close()
        finish.onComplete {
            case _ => chunkChannel.eofAndEnd()
        }
    }
}
