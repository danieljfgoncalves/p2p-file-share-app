import domain.Directory;
import domain.FilenameItemSet;
import networking.TcpCommunication;
import networking.UdpCommunication;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

public class P2PFileShareApp {

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("|****************************************|");
        System.out.println("| Launched Serverless P2P File Share App |");
        System.out.println("|****************************************|");


        DatagramSocket udpSocket = null;
        try {
            udpSocket = new DatagramSocket(8887);
        } catch (BindException ex) {
            System.out.println("Bind to local port failed");
            System.exit(-1);
        }

        ServerSocket tcpSocket = new ServerSocket(0);

        FilenameItemSet set = new FilenameItemSet();
        Directory shdDir = new Directory("testDir");
        Directory dwlDir = new Directory("testDir2");

        UdpCommunication udp =
                new UdpCommunication(udpSocket, 20, shdDir, set, "user1", 7777);
        udp.start();

        TcpCommunication tcp = new TcpCommunication(tcpSocket, shdDir, dwlDir);
        tcp.start();

        Integer tcpPort = tcpSocket.getLocalPort();

        tcp.download("download.txt", InetAddress.getByName("192.168.1.2"), tcpPort);

        //Thread.sleep(60 * 1000);

        System.exit(0); // Kill all threads

        System.out.println("|******************************************|");
        System.out.println("| Terminated Serverless P2P File Share App |");
        System.out.println("|******************************************|");
    }
}
