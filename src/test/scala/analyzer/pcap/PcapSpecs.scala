package analyzer.pcap

import java.io.EOFException
import java.util.concurrent.TimeoutException

import com.sun.nio.sctp.SctpChannel
import org.pcap4j.core.Pcaps
import org.pcap4j.packet._
import org.pcap4j.packet.TcpPacket.TcpHeader
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Jan on 18.04.2016.
  */
class PcapSpecs
    extends FlatSpec
    with Matchers
    with ScalaFutures {

    "A Pcap Lib" should "do something" in {

        val handle = Pcaps.openOffline("traces/tcp-ecn-sample.pcap")

        def loop() {

            while (true) {
                try {
                    val packet = handle.getNextPacketEx
                    packet match {
                        case p: EthernetPacket => {
                            System.out.println(p.getHeader)
                            p.getPayload match {
                                case ipv4: IpV4Packet => {
                                    System.out.println(ipv4.getHeader)
                                    ipv4.getPayload match {
                                        case tcp: TcpPacket => System.out.println(tcp)
                                        case udp: UdpPacket => System.out.println(udp)
                                    }
                                }
                                case ipv6: IpV6Packet => {
                                    System.out.println(ipv6.getHeader)
                                }
                            }
                        }
                    }
                } catch {
                    case e: TimeoutException => System.out.println("Timeout")
                    case e: EOFException => return
                }
            }
        }

        loop()

        handle.close();

    }
}
