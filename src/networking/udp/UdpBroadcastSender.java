package networking.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by danielGoncalves on 09/05/17.
 */
public class UdpBroadcastSender implements Runnable {

    private static final int SIZE = 300;
    private static final int SLEEP_TIME = 30;
    private DatagramSocket sock;

    public UdpBroadcastSender(DatagramSocket socket) {

        sock = socket;
    }

    @Override
    public void run() {

        byte[] data = new byte[SIZE];
        int len;

        DatagramPacket udpPacket = new DatagramPacket(data, data.length);
        try {
            while (true) {

                String sender = "Sending Request";

                InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
                udpPacket.setAddress(broadcastAddr);
                udpPacket.setData(sender.getBytes());
                udpPacket.setLength(sender.length());
                sock.send(udpPacket);

                len = udpPacket.getLength();
                System.out.println("Requested to broadcast");

                udpPacket.setData(data);
                udpPacket.setLength(data.length);
                sock.receive(udpPacket);

                Thread.sleep(SLEEP_TIME * 1000);
            }
        } catch (IOException ex) {
            System.out.println("IOException");
        } catch (InterruptedException e) {
            System.out.println("InterruptedException");
        }
    }
}
