package networking.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Handles UDP Connection to obtain peers list of files to share & sends its own shared files
 *
 * Created by danielGoncalves on 09/05/17.
 */
public class UdpCommunication implements Runnable {

    private static final int SIZE = 300;
    private static final int SLEEP_TIME = 3;
    private DatagramSocket sock;

    public UdpCommunication(DatagramSocket socket) {

        sock = socket;
    }

    @Override
    public void run() {

        byte[] data = new byte[SIZE];

        DatagramPacket udpPacket = new DatagramPacket(data, data.length);
        try {
            while (true) {

                udpPacket.setData(data);
                udpPacket.setLength(data.length);
                sock.receive(udpPacket);

                System.out.println("Request from: " + udpPacket.getAddress().getHostAddress() +
                        " port: " + udpPacket.getPort());

                String test = "Test File.";

                udpPacket.setAddress(udpPacket.getAddress());
                udpPacket.setData(test.getBytes());
                udpPacket.setLength(test.length());
                sock.send(udpPacket);
            }
        } catch (IOException ex) {
            System.out.println("IOException");
        }
    }
}
