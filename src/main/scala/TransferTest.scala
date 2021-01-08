import akka.stream.scaladsl._
import java.net.InetSocketAddress
import java.nio.ByteOrder
import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import akka.stream.actor.ActorSubscriber
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.util.{ByteString, ByteStringBuilder}
import scala.concurrent.Future

class ServerHandler extends Actor {

    import akka.io.Tcp._

    def receive = {

        case Received(data) => println(data)
        case PeerClosed => context stop self
    }
}

class ServerActor extends Actor {

    import akka.io.Tcp._
    import context.system

    IO(akka.io.Tcp) ! Bind(self, new InetSocketAddress("localhost", 10000))

    def receive = {

        case b@Bound(localAddress) =>
        case CommandFailed(_: Bind) => context stop self
        case b@Connected(remote, local) =>
            println("server", remote, local)
            val handler = context.actorOf(Props[ServerHandler])
            val connection = sender()
            connection ! Register(handler)
        case x => println("unknown", x)
    }
}

class StringActor extends Actor {

    def receive = {

        case x => println(x)
    }
}

object Server extends App {

    implicit val system = ActorSystem("server")

    implicit val materializer = akka.stream.ActorMaterializer()

    //system.actorOf(Props[ServerActor])
    val connections: Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind("localhost", 10000)

    connections runForeach { connection =>
        println(connection)

        val sub = ActorSubscriber[String](system.actorOf(Props[StringActor]))

        val printer = Flow[ByteString]
            .via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = 4, allowTruncation = true))
            .map(_.utf8String)
            .to(Sink.fromSubscriber(sub))

        connection.flow.to(Sink.foreach(println))
        println(printer)
    }

    println("started")
}

class ClientActor extends Actor {

    import akka.io.Tcp._
    import context.system

    IO(akka.io.Tcp) ! Connect(new InetSocketAddress("localhost", 10000))

    case object Ack extends Event

    def receive = {

        case CommandFailed(_: Connect) => context stop self
        case c@Connected(remote, local) =>
            println("client", remote, local)
            val connection = sender()
            context watch connection
            connection ! Register(self)
            for (i <- 1 to 100) {
                connection ! Write(ByteString("test\r\n"), Ack)
            }
            println("written")
    }
}

object Client extends App {

    implicit val system = ActorSystem("client")

    system.actorOf(Props[ClientActor])

    println("started")
}
