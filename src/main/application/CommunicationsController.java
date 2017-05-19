package application;

import domain.Directory;
import domain.FilenameItem;
import domain.FilenameItemList;
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
 * Created by danielGoncalves on 12/05/17.
 */
public class CommunicationsController {

    private UdpCommunication udp = null;
    private TcpCommunication tcp = null;

    public CommunicationsController(DatagramSocket udpSocket, Directory sharedDir,
                                    FilenameItemList filenameSet, Integer tcpPort,
                                    ServerSocket tcpSocket, Directory downloadDir) throws SocketException {

        udp = new UdpCommunication(udpSocket, sharedDir, filenameSet, tcpPort);
        tcp = new TcpCommunication(tcpSocket, sharedDir, downloadDir);
    }

    public void loadKnownIps() throws UnknownHostException {
        udp.loadKnownIps();
    }

    public void openUdpCommunications() {

        if (udp != null) udp.start();
    }

    public void openTcpCommunications() {
        if (tcp != null) tcp.start();
    }

    public void downloadFile(FilenameItem item, File newFile) throws IOException, IllegalArgumentException {

        if (tcp != null) tcp.download(item.getFilename(), item.getHost(), item.getTcpPort(), newFile);
    }

    public void addPeerAddresses(String[] addresses) throws UnknownHostException {

        for (String address :
                addresses) {
            InetAddress inetAddress = InetAddress.getByName(address);
            udp.addAddress(inetAddress);
        }
    }

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

    public List<String> getKnownIps() {
        return udp.getKnownIps();
    }
}
