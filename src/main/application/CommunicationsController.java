package application;

import domain.Directory;
import domain.FilenameItemSet;
import networking.TcpCommunication;
import networking.UdpCommunication;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * Controller to handle TCP/UDP communications.
 * <p>
 * Created by danielGoncalves on 12/05/17.
 */
public class CommunicationsController {

    private UdpCommunication udp;
    private TcpCommunication tcp;

    public CommunicationsController(DatagramSocket udpSocket, Directory sharedDir,
                                    FilenameItemSet filenameSet, Integer tcpPort,
                                    ServerSocket tcpSocket, Directory downloadDir) throws SocketException {

        udp = new UdpCommunication(udpSocket, sharedDir, filenameSet, tcpPort);
        tcp = new TcpCommunication(tcpSocket, sharedDir, downloadDir);
    }

    public void openUdpCommunications() {
        udp.start();
    }

    public void openTcpCommunications() {
        tcp.start();
    }

    public void downloadFile(String filename, InetAddress host, int tcpPort) throws IOException {

        tcp.download(filename, host, tcpPort);
    }
}
