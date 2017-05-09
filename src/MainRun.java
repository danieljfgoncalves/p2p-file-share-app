import networking.udp.UdpBroadcastReceiver;
import networking.udp.UdpBroadcastSender;

import java.net.*;

public class MainRun {

    public static void main(String[] args) throws UnknownHostException, SocketException {
        System.out.println("Hello World!");

        InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");

        InetSocketAddress sAddr = new InetSocketAddress(9002);

        // create an unbound socket
        DatagramSocket sock1 = new DatagramSocket(null);
        // make it possible to bind several sockets to the same port
        sock1.setReuseAddress(true);
        // might not be necessary, but for clarity
        sock1.setBroadcast(true);
        sock1.bind(sAddr);

        InetAddress myAddr = sAddr.getAddress();
        int port2 = 8887;
        //DatagramSocket sock2 = new DatagramSocket(port2);

        Thread sock1Thread = new Thread(new UdpBroadcastReceiver(sock1));

        sock1Thread.start();

        Thread sock2Thread = new Thread(new UdpBroadcastSender(sock1));

        sock2Thread.start();
    }
}
