package networking;

import domain.Directory;
import settings.Application;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles TCP Connections to download a file from peers list of files to share.
 * <p>
 * Created by danielGoncalves on 11/05/17.
 */
public class TcpCommunication {

    private final Directory sharedDir;
    private final Directory downloadDir;
    private final ServerSocket serverSocket;
    private final Thread serverThread;
    private final ExecutorService clientTaskPool;

    public TcpCommunication(ServerSocket tcpSocket, Directory sharedDirectory, Directory downloadDirectory) {

        sharedDir = sharedDirectory;
        downloadDir = downloadDirectory;
        serverSocket = tcpSocket;
        serverThread = new Thread(new TcpServer());
        clientTaskPool = Executors.newFixedThreadPool(Application.settings().getMaxUploads());
    }

    public void start() {

        serverThread.start(); // Start tcp server
    }

    private void tcpServer() throws IOException {

        System.out.println("Waiting for peers to connect (TCP)...");
        while (true) {
            Socket connectionSocket = serverSocket.accept();

            clientTaskPool.submit(new TcpConnection(connectionSocket));
        }
    }

    public void download(String filename, InetAddress host, int tcpPort) throws IOException {

        Socket clientSocket = new Socket(host, tcpPort);

        DataOutputStream outcome = new DataOutputStream(clientSocket.getOutputStream());
        DataInputStream income = new DataInputStream(clientSocket.getInputStream());
        // Send filename
        outcome.writeInt(filename.length());
        outcome.write(filename.getBytes(), 0, filename.length());
        // Receive file size
        int fileSize = (int) income.readLong();
        // Prepare file bytes
        byte[] fileBytes = new byte[fileSize];
        income.read(fileBytes, 0, fileBytes.length);
        // Write to file
        File downloadedFile = new File(downloadDir.getAbsoluteDirPath(), filename);
        FileOutputStream fos = new FileOutputStream(downloadedFile);
        fos.write(fileBytes);

        // Close streams
        fos.close();
        outcome.close();
        income.close();
        clientSocket.close();

        System.out.println("Downloaded the file: " + filename + " to: " + clientSocket.getInetAddress().getHostAddress());
    }

    /**
     * TCP Server
     */
    private class TcpServer implements Runnable {

        @Override
        public void run() {

            try {
                tcpServer(); // Launch tcp server in a new thread
            } catch (IOException e) {
                e.printStackTrace(); // FIXME: Treat exceptions
            }

        }
    }

    /**
     * TCP connection task
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

            // Reply to download request
            try {

                // Open income & outcome connection
                DataInputStream income = new DataInputStream(connectionSocket.getInputStream());
                DataOutputStream outcome = new DataOutputStream(connectionSocket.getOutputStream());
                // Request file's name & size to download
                int filenameSize = income.readInt();
                byte[] stringBytes = new byte[filenameSize];
                income.read(stringBytes, 0, stringBytes.length);
                String filename = new String(stringBytes);
                System.out.println("Requested the file: " + filename);
                // Get File by received filename
                File requestFile = sharedDir.getFile(filename);
                if (requestFile == null) throw new IllegalArgumentException("File doesn't exist.");
                // Send file size
                long fileSize = requestFile.length();
                outcome.writeLong(fileSize);
                // Send file
                byte[] fileBytes = new byte[(int) fileSize];
                fis = new FileInputStream(requestFile);
                bis = new BufferedInputStream(fis);
                bis.read(fileBytes, 0, fileBytes.length);
                outcome.write(fileBytes, 0, fileBytes.length); // Writes file to socket
                // Flush stream
                outcome.flush();

                System.out.println("Uploaded the file: " + filename + " to: " + connectionSocket.getInetAddress().getHostAddress());

            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                try {

                    if (fis != null) fis.close();
                    if (fis != null) bis.close();
                    connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
