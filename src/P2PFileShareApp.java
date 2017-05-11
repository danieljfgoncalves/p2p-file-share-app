import domain.FilenameItemSet;
import domain.WatchedDirectory;
import networking.udp.UdpCommunication;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;

public class P2PFileShareApp {

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("|****************************************|");
        System.out.println("| Launched Serverless P2P File Share App |");
        System.out.println("|****************************************|");


        DatagramSocket udpSocket = null;

        try {
            udpSocket = new DatagramSocket(8888);
        } catch (BindException ex) {
            System.out.println("Bind to local port failed");
            System.exit(-1);
        }

        FilenameItemSet set = new FilenameItemSet();
        WatchedDirectory dir = new WatchedDirectory("testDir");

        UdpCommunication udp =
                new UdpCommunication(udpSocket, 20, dir, set, "user1", (short) 7777);

        udp.start();

        Thread.sleep(60 * 1000);

        System.out.println("Killing.");
        System.exit(0);
    }
}
