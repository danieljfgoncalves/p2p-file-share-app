package networking;

import domain.Directory;
import settings.Application;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles TCP Connections to download a file from peers list of files to share.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class TcpCommunication {

    private final Directory sharedDir;
    private final Directory downloadDir;
    private final ServerSocket serverSocket;
    private final Thread serverThread;
    private final ExecutorService clientTaskPool;

    /**
     * Creates a TCP Communication.
     *
     * @param tcpSocket         the TCP server socket
     * @param sharedDirectory   the shared directory
     * @param downloadDirectory the download directory
     */
    public TcpCommunication(ServerSocket tcpSocket, Directory sharedDirectory, Directory downloadDirectory) {

        sharedDir = sharedDirectory;
        downloadDir = downloadDirectory;
        serverSocket = tcpSocket;
        serverThread = new Thread(new TcpServer());
        clientTaskPool = Executors.newFixedThreadPool(Application.settings().getMaxUploads());
    }

    /**
     * Starts the TCP server
     */
    public void start() {

        serverThread.start(); // Start tcp server
    }

    /**
     * Creates a TCP Server.
     *
     * @throws IOException I/O error
     */
    private void tcpServer() throws IOException {

        System.out.println("Waiting for peers to connect (TCP)...");
        //noinspection InfiniteLoopStatement
        while (true) {
            Socket connectionSocket = serverSocket.accept();

            clientTaskPool.submit(new TcpConnection(connectionSocket));
        }
    }

    /**
     * Requests a download to the peer's server.
     *
     * @param filename the resquested file's name
     * @param host     the peer's host IPv4 Address
     * @param tcpPort  the peer's TCP Port
     * @param toFile   the File object representing the location of the download file (if null get location by default)
     * @throws IOException              I/O error
     * @throws IllegalArgumentException Unavailable file error
     */
    public void download(String filename, InetAddress host, int tcpPort, File toFile) throws IOException, IllegalArgumentException {

        File downloadedFile = (toFile == null) ? new File(downloadDir.getAbsoluteDirPath(), filename) : toFile;

        Socket clientSocket = new Socket(host, tcpPort);

        DataOutputStream outcome = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream income = new DataInputStream(clientSocket.getInputStream());
        // Send filename
        outcome.writeInt(filename.length());
        outcome.write(filename.getBytes(), 0, filename.length());
        // Receive file size
        Long lon = income.readLong();
        int fileSize = lon.intValue();
        try {
            if (fileSize < 0) throw new IllegalArgumentException("File is not available anymore.");
            // Prepare file bytes
            byte[] fileBytes = new byte[fileSize];
            int readBytes = 0;
            while (readBytes < fileSize) {
                int offset = readBytes;
                int tmp = income.read(fileBytes, offset, (fileBytes.length - offset));
                if (tmp < 0) break;
                readBytes += tmp;
            }
            System.out.println("READ BYTES: " + readBytes);
            // Write to file
            Files.write(downloadedFile.toPath(), fileBytes);
            System.out.println("Downloaded the file: " + filename + " to: " + clientSocket.getInetAddress().getHostAddress());
        } finally {

            // Close streams
            outcome.close();
            income.close();
            clientSocket.close();
        }
    }

    /**
     * Thread running the TCP Server
     */
    private class TcpServer implements Runnable {

        @Override
        public void run() {

            try {
                tcpServer(); // Launch tcp server in a new thread
            } catch (Exception e) {

                Logger.getLogger(Directory.class.getName()).log(Level.SEVERE, "TCP server failed.", e);
            }

        }
    }

    /**
     * TCP client connection task
     */
    private class TcpConnection implements Runnable {

        private final Socket connectionSocket;

        private TcpConnection(Socket connectionSocket) {
            this.connectionSocket = connectionSocket;
        }

        @Override
        public void run() {

            FileInputStream fis = null;
            BufferedInputStream bis = null;
            DataInputStream income = null;
            DataOutputStream outcome = null;

            // Reply to download request
            try {
                // Open income & outcome connection
                income = new DataInputStream(connectionSocket.getInputStream());
                outcome = new DataOutputStream(connectionSocket.getOutputStream());
                // Request file's name & size to download
                int filenameSize = income.readInt();
                byte[] stringBytes = new byte[filenameSize];
                income.readFully(stringBytes, 0, stringBytes.length);
                String filename = new String(stringBytes);
                System.out.println("Requested the file: " + filename);
                // Get File by received filename
                File requestFile = sharedDir.getFile(filename);
                // Send file size
                long fileSize = (requestFile == null) ? -1 : requestFile.length();
                outcome.writeLong(fileSize);
                if (requestFile == null) throw new IllegalArgumentException("File not available anymore.");
                // Send file
                byte[] fileBytes = new byte[(int) fileSize];
                fis = new FileInputStream(requestFile);
                bis = new BufferedInputStream(fis);
                int readBytes = 0;
                while (readBytes < fileSize) {

                    int offset = readBytes;
                    int tmp = bis.read(fileBytes, offset, fileBytes.length);
                    if (tmp < 0) break;
                    readBytes += tmp;
                }
                outcome.write(fileBytes, 0, fileBytes.length); // Writes file to socket
                // Flush stream
                outcome.flush();
                System.out.println("Uploaded the file: " + filename + " to: " + connectionSocket.getInetAddress().getHostAddress());

            } catch (IOException | IllegalArgumentException e) {
                Logger.getLogger(Directory.class.getName()).log(Level.WARNING, "TCP Connection failed.", e);
            } finally {
                try {
                    if (fis != null) fis.close();
                    if (bis != null) bis.close();
                    if (outcome != null) outcome.close();
                    if (income != null) income.close();
                    connectionSocket.close();
                } catch (IOException e) {
                    Logger.getLogger(Directory.class.getName()).log(Level.WARNING, "Closing streams failed.", e);
                }
            }
        }
    }
}
