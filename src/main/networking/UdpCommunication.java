package networking;

import domain.Directory;
import domain.FilenameItem;
import domain.FilenameItemList;
import domain.FilenameSetProtocol;
import settings.Application;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

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
    private final Set<InetAddress> addresses;

    public UdpCommunication(DatagramSocket socket, Directory sharedDir, FilenameItemList filenamesSet, Integer tcpPort)
            throws SocketException {

        udpSocket = socket;
        udpSocket.setBroadcast(true);
        this.tcpPort = tcpPort;
        sendingTimer = new Timer();
        receiverThread = new Thread(new UdpReceiver());
        sharedDirectory = sharedDir;
        filenames = filenamesSet;
        addresses = new HashSet<>();
    }

    public void loadKnownIps() throws UnknownHostException {
        addresses.addAll(Application.settings().getKnownAddreses());
    }

    public void start() {

        // Start Receiver
        receiverThread.start();

        // Start Sender
        sendingTimer.scheduleAtFixedRate(
                new UdpSender(), SEND_LIST_DELAY * 1000, Application.settings().getBroadcastTimeInterval() * 1000);
    }

    public void stop() {

        udpSocket.close();
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

            if (!isLocalAddress(udpPacket.getAddress())) {

                addresses.add(udpPacket.getAddress()); // FIXME

                List<FilenameItem> newItems = FilenameSetProtocol.parsePacket(udpPacket.getData(), udpPacket.getAddress());

                if (!newItems.isEmpty()) {
                    System.out.printf("[Received] <");
                    for (FilenameItem f :
                            newItems) {
                        System.out.printf(" %s;", f.getFilename());
                    }
                    System.out.println(" >");
                    filenames.addAll(newItems);
                }
            }
        }
    }

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

    private void sendBroadcast() throws IOException {

        addresses.add(InetAddress.getByName(BROADCAST_STRING));

        File[] files = this.sharedDirectory.getFiles();

        if (files.length > 0) { // If no files to send ignore

            LinkedList<byte[]> dataList = new LinkedList<>(FilenameSetProtocol.parseFileList(files, this.tcpPort));

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
            // FIXME : erase test
            System.out.printf("[Sent] <");
            for (File f :
                    files) {
                System.out.printf(" %s;", f.getName());
            }
            System.out.println(" >");
        }
    }

    public boolean addAddress(InetAddress address) {

        return addresses.add(address);
    }

    public String[] getKnownAddressList() {

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
            } catch (Exception e) {
                e.printStackTrace(); // FIXME : Treat exception
            }
        }
    }
}
