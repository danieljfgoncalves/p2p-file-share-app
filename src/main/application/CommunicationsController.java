package application;

import domain.Directory;
import domain.RemoteFilename;
import domain.RemoteFilenameList;
import networking.TcpCommunication;
import networking.UdpCommunication;
import settings.AppSettings;
import util.Constants;
import util.StringUtil;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Properties;

/**
 * Controller to handle TCP/UDP communications.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class CommunicationsController {

    private UdpCommunication udp = null;
    private TcpCommunication tcp = null;

    /**
     * Creates a communications controller
     *
     * @param udpSocket   the UDP datagram socket
     * @param sharedDir   the shared directory
     * @param filenames   the remote filenames
     * @param tcpPort     the TCP port
     * @param tcpSocket   the TCP server socket
     * @param downloadDir the downloads directory
     * @throws SocketException error creating/accessing socket
     */
    public CommunicationsController(DatagramSocket udpSocket, Directory sharedDir,
                                    RemoteFilenameList filenames, Integer tcpPort,
                                    ServerSocket tcpSocket, Directory downloadDir) throws SocketException {

        udp = new UdpCommunication(udpSocket, sharedDir, filenames, tcpPort);
        tcp = new TcpCommunication(tcpSocket, sharedDir, downloadDir);
    }

    /**
     * Opens UDP communications
     */
    public void openUdpCommunications() {

        if (udp != null) udp.start();
    }

    /**
     * Opens TCP communications
     */
    public void openTcpCommunications() {
        if (tcp != null) tcp.start();
    }

    /**
     * Downloads a remote file.
     *
     * @param remoteFilename the remote filename
     * @param newFile        the file object to store the download (if null creates in default folder)
     * @throws IOException              I/O error
     * @throws IllegalArgumentException file not available error
     */
    public void downloadFile(RemoteFilename remoteFilename, File newFile) throws IOException, IllegalArgumentException {

        if (tcp != null)
            tcp.download(remoteFilename.getFilename(), remoteFilename.getHost(), remoteFilename.getTcpPort(), newFile);
    }

    /**
     * Adds a list of IPv4 addresses to the known addresses
     *
     * @param addresses list of known addresses
     * @throws UnknownHostException unknown IPv4 address error
     */
    public void addPeerAddresses(String[] addresses) throws UnknownHostException {

        for (String address :
                addresses) {
            InetAddress inetAddress = InetAddress.getByName(address);
            udp.addAddress(inetAddress);
        }
    }

    /**
     * Loads a list of known IPv4 addresses
     *
     * @throws UnknownHostException unknown IPv4 address error
     */
    public void loadKnownIps() throws UnknownHostException {
        udp.loadKnownIps();
    }

    /**
     * Persists a list of known IPv4 addresses
     *
     * @throws IOException I/O error
     */
    public void saveKnownIpsList() throws IOException {

        File config = new File(Constants.CONFIG_FILENAME);

        if (!config.exists()) config.createNewFile();

        Properties properties = new Properties();
        InputStream input = new FileInputStream(config);
        properties.load(input);
        input.close();
        properties.replace(AppSettings.KNOWN_IPS_KEY, StringUtil.arrayToString(udp.getKnownAddressList()));
        FileOutputStream output = new FileOutputStream(config);
        properties.store(output, "Update known ips.");
        output.close();
    }

    /**
     * Obtains a list of known IPv4 addresses
     *
     * @return list of known IPv4 addresses
     */
    public List<String> getKnownIps() {
        return udp.getKnownIps();
    }
}
