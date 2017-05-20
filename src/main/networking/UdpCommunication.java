package networking;

import domain.Directory;
import domain.RemoteFilename;
import domain.RemoteFilenameList;
import settings.Application;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.Constants.BROADCAST_STRING;
import static util.Constants.SEND_LIST_DELAY;

/**
 * Handles UDP Connection to obtain peers list of files to share & sends its own shared files
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class UdpCommunication {

    private final DatagramSocket udpSocket;
    private final Integer tcpPort;
    private final Thread receiverThread;
    private final RemoteFilenameList filenames;
    private final Directory sharedDirectory;
    private final Timer sendingTimer;
    private final Set<InetAddress> addresses;

    /**
     * Creates an UDP Connection
     *
     * @param socket    the datagram socket
     * @param sharedDir the shared directory
     * @param filenames the filename list
     * @param tcpPort   the peer's TCP port
     * @throws SocketException Creating/Associating a socket error
     */
    public UdpCommunication(DatagramSocket socket, Directory sharedDir, RemoteFilenameList filenames, Integer tcpPort)
            throws SocketException {

        udpSocket = socket;
        udpSocket.setBroadcast(true);
        this.tcpPort = tcpPort;
        sendingTimer = new Timer();
        receiverThread = new Thread(new UdpReceiver());
        sharedDirectory = sharedDir;
        this.filenames = filenames;
        addresses = new HashSet<>();
    }

    /**
     * Start the UDP Server
     */
    public void start() {

        // Start Receiver
        receiverThread.start();

        // Start Sender
        sendingTimer.scheduleAtFixedRate(
                new UdpSender(), SEND_LIST_DELAY * 1000, Application.settings().getBroadcastTimeInterval() * 1000);
    }

    /**
     * Creates a UDP Server to listen for Datagram Packets
     *
     * @throws IOException I/O error
     */
    private void receiverServer() throws IOException {

        byte[] data = new byte[Constants.PAYLOAD_SIZE];

        /*
        A new DatagramPacket object is created, the constructor used defines only a
        buffer and the buffer size. IP address and port number will be set when the
        datagram is received (source IP address and source port number).
        */
        DatagramPacket udpPacket = new DatagramPacket(data, data.length);
        //noinspection InfiniteLoopStatement
        while (true) {

            udpPacket.setData(data);
            udpPacket.setLength(data.length);

            /*
            The receive() method is called to receive the datagram (client request),
            if no datagram has been received, the thread will block until one arrives.
            */
            udpSocket.receive(udpPacket);

            if (!isLocalAddress(udpPacket.getAddress())) {

                addresses.add(udpPacket.getAddress());

                List<RemoteFilename> newItems = RemoteFilenameListProtocol.parsePacket(udpPacket.getData(), udpPacket.getAddress());

                if (!newItems.isEmpty()) {
                    System.out.printf("[Received] <");
                    for (RemoteFilename f :
                            newItems) {
                        System.out.printf(" %s;", f.getFilename());
                    }
                    System.out.println(" >");
                    filenames.addAll(newItems);
                }
            }
        }
    }

    /**
     * Sends the remote filenames list through broadcast and to any known IPv4 address by the app.
     *
     * @throws IOException I/O error
     */
    private void sendRemoteFiles() throws IOException {

        addresses.add(InetAddress.getByName(BROADCAST_STRING));

        File[] files = this.sharedDirectory.getFiles();

        if (files.length > 0) { // If no files to send ignore

            LinkedList<byte[]> dataList = new LinkedList<>(RemoteFilenameListProtocol.parseFileList(files, this.tcpPort));

            List<DatagramPacket> packets = new ArrayList<>();
            for (InetAddress address :
                    addresses) {
                packets.add(new DatagramPacket(new byte[Constants.PAYLOAD_SIZE], Constants.PAYLOAD_SIZE, address, udpSocket.getLocalPort()));
            }

            while (!dataList.isEmpty()) {

                byte[] data = dataList.pop();

                for (DatagramPacket udpPacket :
                        packets) {
                    udpPacket.setData(data);
                    udpPacket.setLength(data.length);
                    // Send packet
                    try {
                        udpSocket.send(udpPacket);
                    } catch (IOException e) {
                        System.out.println("Couldn't send packet.");
                    }
                }
            }

            System.out.printf("[Sent] <");
            for (File f :
                    files) {
                System.out.printf(" %s;", f.getName());
            }
            System.out.println(" >");
        }
    }

    /**
     * Checks if a address is a local address
     *
     * @param address the address to check
     * @return true if it is local, false otherwise
     * @throws SocketException Create/Associate Socket exception
     */
    private boolean isLocalAddress(InetAddress address) throws SocketException {

        Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();
        while (list.hasMoreElements()) {

            NetworkInterface anInterface = list.nextElement();
            Enumeration<InetAddress> addresses = anInterface.getInetAddresses();

            while (addresses.hasMoreElements()) {

                InetAddress inetAddress = addresses.nextElement();
                if (address.equals(inetAddress)) return true;
            }
        }
        return false;
    }

    /**
     * Loads the known IPv4 addresses
     *
     * @throws UnknownHostException Unrecognized address error
     */
    public void loadKnownIps() throws UnknownHostException {
        addresses.addAll(Application.settings().getKnownAddresses());
    }

    /**
     * Adds an address to the known addresses list
     *
     * @param address the address to add
     * @return true if added, false otherwise
     */
    public void addAddress(InetAddress address) {

        addresses.add(address);
    }

    /**
     * Obtain known IPv4 addresses
     *
     * @return a string array of known addresses
     */
    public String[] getKnownIps() {

        List<String> tmp = new ArrayList<>();
        for (InetAddress addr :
                addresses) {
            tmp.add(addr.getHostAddress());
        }

        String[] list = new String[tmp.size()];

        return tmp.toArray(list);
    }

    /**
     * UDP Receiver Handler
     */
    private class UdpReceiver implements Runnable {

        @Override
        public void run() {

            try {
                receiverServer(); // Launch receiver server in a new thread
            } catch (Exception e) {
                Logger.getLogger(Directory.class.getName()).log(Level.SEVERE, "UDP Server failed.", e);
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
                sendRemoteFiles(); // Send filenames to share in broadcast
            } catch (Exception e) {
                Logger.getLogger(Directory.class.getName()).log(Level.SEVERE, "UDP Broadcast Sender failed.", e);
            }
        }
    }
}
