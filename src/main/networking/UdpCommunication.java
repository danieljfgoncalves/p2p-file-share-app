package networking;

import domain.Directory;
import domain.FilenameItem;
import domain.FilenameItemList;
import domain.FilenameSetProtocol;
import settings.Application;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static util.Constants.BROADCAST_STRING;
import static util.Constants.SEND_LIST_DELAY;

/**
 * Handles UDP Connection to obtain peers list of files to share & sends its own shared files
 * <p>
 * Created by danielGoncalves on 09/05/17.
 */
public class UdpCommunication {

    private final DatagramSocket udpSocket;
    private final Integer tcpPort;
    private final Thread receiverThread;
    private final FilenameItemList filenames;
    private final Directory sharedDirectory;
    private final Timer sendingTimer;

    public UdpCommunication(DatagramSocket socket, Directory sharedDir, FilenameItemList filenamesSet, Integer tcpPort)
            throws SocketException {

        udpSocket = socket;
        udpSocket.setBroadcast(true);
        this.tcpPort = tcpPort;
        sendingTimer = new Timer();
        receiverThread = new Thread(new UdpReceiver());
        sharedDirectory = sharedDir;
        filenames = filenamesSet;
    }

    public void start() {

        // Start Receiver
        receiverThread.start();

        // Start Sender
        sendingTimer.scheduleAtFixedRate(
                new UdpSender(), SEND_LIST_DELAY * 1000, Application.settings().getBroadcastTimeInterval() * 1000);
    }

    private void receiverServer() throws IOException {

        byte[] data = new byte[Constants.PAYLOAD_SIZE];

        /*
        A new DatagramPacket object is created, the constructor used defines only a
        buffer and the buffer size. IP address and port number will be set when the
        datagram is received (source IP address and source port number).
        */
        DatagramPacket udpPacket = new DatagramPacket(data, data.length);
        while (true) {

            udpPacket.setData(data);
            udpPacket.setLength(data.length);

            /*
            The receive() method is called to receive the datagram (client request),
            if no datagram has been received, the thread will block until one arrives.
            */
            udpSocket.receive(udpPacket);

            // TODO: Uncomment after tests
            //if (udpPacket.getAddress().equals(udpSocket.getLocalAddress())) {
            List<FilenameItem> newItems = FilenameSetProtocol.parsePacket(udpPacket.getData(), udpPacket.getAddress());
            // FIXME : erase test
            if (!newItems.isEmpty()) {
                System.out.printf("[Received] <");
                for (FilenameItem f :
                        newItems) {
                    System.out.printf(" %s;", f.getFilename());
                }
                System.out.println(" >");
                filenames.addAll(newItems);
            }
            //}
        }
    }

    private void sendBroadcast() throws IOException {

        // TODO: Implement suggestion to send to unicast addresses too.

        InetAddress broadCast = InetAddress.getByName(BROADCAST_STRING);

        File[] files = this.sharedDirectory.getFiles();

        if (files.length > 0) { // If no files to send ignore

            LinkedList<byte[]> dataList = new LinkedList<>(FilenameSetProtocol.parseFileList(files, this.tcpPort));

            DatagramPacket udpPacket =
                    new DatagramPacket(new byte[Constants.PAYLOAD_SIZE], Constants.PAYLOAD_SIZE, broadCast, udpSocket.getLocalPort());

            while (!dataList.isEmpty()) {

                byte[] data = dataList.pop();

                udpPacket.setData(data);
                udpPacket.setLength(data.length);
                // Send packet
                udpSocket.send(udpPacket);
            }

            // FIXME : erase test
            System.out.printf("[Sent] <");
            for (File f :
                    files) {
                System.out.printf(" %s;", f.getName());
            }
            System.out.println(" >");
        }
    }

    /**
     * UDP Receiver Handler
     */
    private class UdpReceiver implements Runnable {

        @Override
        public void run() {

            try {
                receiverServer(); // Launch receiver server in a new thread
            } catch (IOException e) {
                e.printStackTrace(); // FIXME : Treat exception
            }
        }
    }

    /**
     * UDP Sender Handler
     */
    private class UdpSender extends TimerTask {

        @Override
        public void run() {

            try {
                sendBroadcast(); // Send filenames to share in broadcast
            } catch (IOException e) {
                e.printStackTrace(); // FIXME : Treat exception
            }
        }
    }
}
