import application.CommunicationsController;
import domain.Directory;
import domain.FilenameItemSet;
import settings.Application;
import util.Constants;

import javax.swing.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.Constants.WARNING_PANE_TITLE;

public class P2PFileShareApp {

    public static void main(String[] args) {

        System.out.println("|****************************************|");
        System.out.println("| Launched Serverless P2P File Share App |");
        System.out.println("|****************************************|");

        // Open UDP/TCP Sockets
        DatagramSocket udpSocket = null;
        ServerSocket tcpSocket = null;
        try {
            udpSocket = new DatagramSocket(Application.settings().getUdpPort());
            tcpSocket = new ServerSocket(Application.settings().getTcpPort());
        } catch (IOException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.SEVERE, "Open sockets failed.", e);

            // PopUp Warning
            JOptionPane.showMessageDialog(
                    null,
                    "Open sockets failed.",
                    WARNING_PANE_TITLE,
                    JOptionPane.WARNING_MESSAGE);

            System.exit(Constants.SOCKET_FAILED);
        }

        // Create List of file to share & shared/download directories
        FilenameItemSet filenameSet = new FilenameItemSet();
        Directory shdDir = null;
        Directory dwlDir = null;
        try { // FIXME : Refactor to a controller ??
            shdDir = new Directory(Application.settings().getShdDir());
            dwlDir = new Directory(Application.settings().getDownloadsDir());
            shdDir.watch(); // Activate watch service
            dwlDir.watch();
        } catch (IOException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.SEVERE, "Open directories failed.", e);

            // PopUp Warning
            JOptionPane.showMessageDialog(
                    null,
                    "Open directories failed.",
                    WARNING_PANE_TITLE,
                    JOptionPane.WARNING_MESSAGE);

            System.exit(Constants.SOCKET_FAILED);
        }

        // Allocated TCP Port
        Integer tcpPort = tcpSocket.getLocalPort();

        // Open Communications Controller
        CommunicationsController communicationsController = null;
        try {
            communicationsController = new CommunicationsController(udpSocket, shdDir, filenameSet, tcpPort, tcpSocket, dwlDir);
        } catch (SocketException e) {
            Logger.getLogger(P2PFileShareApp.class.getName()).log(Level.WARNING, "Send broadcast packet failed.", e);

            // PopUp Warning
            JOptionPane.showMessageDialog(
                    null,
                    "Sending files crashed. Relaunch app or you can still download files.",
                    WARNING_PANE_TITLE,
                    JOptionPane.WARNING_MESSAGE);
        }
        // Open communications
        communicationsController.openUdpCommunications();
        communicationsController.openTcpCommunications();
        // TODO : Remaining App flow (UI Frames)


        System.out.println("sleeping");
        try {
            Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("|******************************************|");
        System.out.println("| Terminated Serverless P2P File Share App |");
        System.out.println("|******************************************|");

        System.exit(Constants.EXIT_SUCCESS); // Kills all threads
    }
}
