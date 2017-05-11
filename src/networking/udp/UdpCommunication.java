package networking.udp;

import domain.FilenameItem;
import domain.FilenameItemSet;
import domain.FilenameSetProtocol;
import domain.WatchedDirectory;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Handles UDP Connection to obtain peers list of files to share & sends its own shared files
 * <p>
 * Created by danielGoncalves on 09/05/17.
 */
public class UdpCommunication extends Observable {

    private static final String BROADCAST_STRING = "255.255.255.255";
    private static final int TIMER_DELAY = 2;
    private final DatagramSocket udpSocket;
    private final String username;
    private final Short tcpPort;
    private final int timePeriod;
    private final Thread receiverThread;
    private final FilenameItemSet filenames;
    private final WatchedDirectory sharedDirectory;
    private final Timer sendingTimer;

    public UdpCommunication(DatagramSocket socket, int sendingTimePeriod,
                            WatchedDirectory sharedDir, FilenameItemSet filenamesSet, String username, Short tcpPort)
            throws SocketException {

        udpSocket = socket;
        udpSocket.setBroadcast(true);
        // udpSocket.setSoTimeout(TIMEOUT * 1000); // FIXME : set the socket timeout

        this.username = username;
        this.tcpPort = tcpPort;

        sendingTimer = new Timer();
        timePeriod = sendingTimePeriod;

        receiverThread = new Thread(new UdpReceiver());

        sharedDirectory = sharedDir;
        filenames = filenamesSet;
    }

    public void start() {

        // Start Receiver
        receiverThread.start();

        // Start Sender
        sendingTimer.scheduleAtFixedRate(new UdpSender(), TIMER_DELAY * 1000, timePeriod * 1000);
    }

    public void kill() {

        // Terminates receiver server
        receiverThread.interrupt();
        // Terminates sending timer
        sendingTimer.cancel();
        sendingTimer.purge();
    }

    private void receiverServer() throws IOException {

        byte[] data = new byte[Constants.PAYLOAD_SIZE];

        /*
        A new DatagramPacket object is created, the constructor used defines only a
        buffer and the buffer size. IP address and port number will be set when the
        datagram is received (source IP address and source port number).
        */
        DatagramPacket udpPacket = new DatagramPacket(data, data.length);

        System.out.println("Listening for peers files to share.");

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

                /* After receiving the request the source IP address and source port number are
                stored in the DatagramPacket object, so there is no need to change then when
                sending a reply because the same DatagramPacket is used for that purpose. Source
                IP address and source port number are printed at the server console. */
            System.out.println("Request from: " + udpPacket.getAddress().getHostAddress() +
                    " port: " + udpPacket.getPort());

            FilenameSetProtocol.parsePacket(filenames, udpPacket.getData(), udpPacket.getAddress());
            filenames.notifyChanges();

            // FIXME : erase test
            System.out.println("Received Files:\n");
            for (FilenameItem f :
                    filenames.getSet()) {
                System.out.printf("RECEIVED ELEMENT: %s | %s | %s | %s\n",
                        f.getFilename(),
                        f.getUsername(),
                        f.getHost().getHostAddress(),
                        f.getTcpPort().toString());
            }
            System.out.println("/**************************/\n");

            //}
        }
    }

    private void sendBroadcast() throws IOException {

        InetAddress broadCast = InetAddress.getByName(BROADCAST_STRING);

        File[] files = this.sharedDirectory.getFiles();

        // FIXME : erase test
        System.out.println("Sent Files:\n");
        for (File f :
                files) {
            System.out.println(f.getName());
        }
        System.out.println("/**************************/\n");

        if (files.length > 0) { // If no files to send ignore

            LinkedList<byte[]> dataList = new LinkedList<>(FilenameSetProtocol.parseFileList(files, tcpPort, username));

            DatagramPacket udpPacket =
                    new DatagramPacket(new byte[Constants.PAYLOAD_SIZE], Constants.PAYLOAD_SIZE, broadCast, udpSocket.getLocalPort());

            while (!dataList.isEmpty()) {

                byte[] data = dataList.pop();

                udpPacket.setData(data);
                udpPacket.setLength(data.length);
                // Send packet
                udpSocket.send(udpPacket);

                System.out.println("Sent file list to share with peers.");
            }

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
