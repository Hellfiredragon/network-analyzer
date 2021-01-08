package analyzer.pcap

import java.io.EOFException
import java.util.concurrent.TimeoutException

import com.sun.nio.sctp.SctpChannel
import org.pcap4j.core.{PcapHandle, Pcaps}
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
        val handle = Pcaps openOffline "traces/tcp-ecn-sample.pcap"

        var packet: Packet = null

        while ( {
            packet = handle getNextPacket; packet != null
        }) packet match {
            case p: EthernetPacket => {
                //println(p.getHeader)
                p.getPayload match {
                    case ipv4: IpV4Packet => {
                        //println(ipv4.getHeader)
                        ipv4.getPayload match {
                            case tcp: TcpPacket => {
                                //println(tcp)
                                println(if(tcp.getPayload != null) tcp.getPayload.getClass)
                            }
                            case udp: UdpPacket => println(udp)
                        }
                    }
                    case ipv6: IpV6Packet => {
                        println(ipv6.getHeader)
                    }
                }
            }
        }


        handle.close();
    }
}
