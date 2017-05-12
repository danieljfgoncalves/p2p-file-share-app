package application;

import domain.Directory;
import domain.FilenameItemSet;
import networking.TcpCommunication;
import networking.UdpCommunication;

import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * Controller to handle TCP/UDP communications.
 * <p>
 * Created by danielGoncalves on 12/05/17.
 */
public class OpenCommunicationsController {

    public void OpenUDPComunications(DatagramSocket udpSocket, Directory sharedDir, FilenameItemSet filenameSet, Integer tcpPort) throws SocketException {

        UdpCommunication udp =
                new UdpCommunication(udpSocket, sharedDir, filenameSet, tcpPort);
        udp.start();
    }

    public void OpenTCPComunications(ServerSocket tcpSocket, Directory sharedDir, Directory downloadDir) {

        TcpCommunication tcp = new TcpCommunication(tcpSocket, sharedDir, downloadDir);
        tcp.start();
    }
}
