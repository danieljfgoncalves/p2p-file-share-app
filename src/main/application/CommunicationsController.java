package application;

import domain.Directory;
import domain.FilenameItem;
import domain.FilenameItemList;
import networking.TcpCommunication;
import networking.UdpCommunication;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;

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

    public void openUdpCommunications() {

        if (udp != null) udp.start();
    }

    public void openTcpCommunications() {
        if (tcp != null) tcp.start();
    }

    public void downloadFile(FilenameItem item, File newFile) throws IOException, IllegalArgumentException {

        if (tcp != null) tcp.download(item.getFilename(), item.getHost(), item.getTcpPort(), newFile);
    }
}
