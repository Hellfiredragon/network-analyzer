package analyzer.process

import analyzer.DefaultConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import scala.sys.process._

/**
  * Created by Jan on 13.12.2015.
  */
class TaskListSpecs
    extends FlatSpec
        with Matchers
        with ScalaFutures
        with DefaultConfig {

    implicit override val patienceConfig = PatienceConfig(Span(3, Seconds))

    "A Tasklist" should "list all tasks" in {
        whenReady(Tasklist.listTasks()) { list =>
            list.length should be > 0
        }
    }

    it should "find lasts started tshark" in {
        val reader = new ProcessReader()
        val command = new TsharkCommandBuilder().captureInterface("lo").build()

        Future {
            command ! reader
        }

        whenReady(Tasklist.lastTshark()) { task =>
            task should not be None
            Tasklist.killTask(task.get.pid)
        }
    }

    it should "kill tasks" in {
        val reader = new ProcessReader()
        val command = new TsharkCommandBuilder().captureInterface("lo").build()

        Future {
            command ! reader
        }

        whenReady(Tasklist.lastTshark()) { task =>
            Tasklist.killTask(task.get.pid)

            whenReady(Tasklist.lastTshark()) { task =>
                task should be(None)
            }
        }
    }
}
