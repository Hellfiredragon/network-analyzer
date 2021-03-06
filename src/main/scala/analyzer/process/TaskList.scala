package analyzer.process

import analyzer.config.{AnalyzerConfig, Linux, Windows}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Iteratee
import scala.concurrent.Future
import scala.language.postfixOps
import scala.sys.process._

case class Task(name: String, pid: Int)

object Tasklist {

    def listTasks()(implicit config: AnalyzerConfig): Future[Seq[Task]] = {

        val reader = new ProcessReader()
        val command = config.os match {
            case Windows => config.commandLine("tasklist /nh")
            case Linux => config.commandLine("ps aux | awk '{print $2 \"\\t\" $11}'")
        }
        val pattern = config.os match {
            case Windows => "([^\\d]+)([\\d]+).*".r
            case Linux => "(\\d+)\t(.+)".r
        }

        val result = reader.lines |>>> Iteratee.fold[String, Seq[Either[String, Task]]](Nil) { (r, c) =>
            config.os match {
                case Windows => c match {
                    case pattern(name, pid) => r :+ Right(Task(name.trim, pid.toInt))
                    case c: String => r :+ Left(c)
                }
                case Linux => c match {
                    case pattern(pid, name) => r :+ Right(Task(name, pid.toInt))
                    case c: String => r :+ Left(c)
                }
            }
        }

        command ! reader

        reader.close()

        result.map(seq => {
            seq.filter {
                case Right(task) => true
                case _ => false
            } map {
                case Right(task) => task
                case _ => throw new RuntimeException("cannot be")
            }
        })
    }

    def lastTshark()(implicit config: AnalyzerConfig): Future[Option[Task]] = {

        listTasks() map {
            case x: Seq[Task] => val filtered = x.filter(x => x.name == config.tsharkProcess)
                filtered match {
                    case Seq(x1, xs@_*) => Some(filtered.last)
                    case Nil => None
                }
        }
    }

    def killTask(pid: Int)(implicit config: AnalyzerConfig): Int = config.killCommand(pid) ! ProcessLogger(fout => Unit, ferr => Unit)
}
