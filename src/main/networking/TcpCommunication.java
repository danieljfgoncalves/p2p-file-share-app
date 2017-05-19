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

    public void stop() throws IOException {
        serverSocket.close();
    }

    private void tcpServer() throws IOException {

        System.out.println("Waiting for peers to connect (TCP)...");
        while (true) {
            Socket connectionSocket = serverSocket.accept();

            clientTaskPool.submit(new TcpConnection(connectionSocket));
        }
    }

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
        System.out.println("INT VALUE:" + fileSize);
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
     * TCP Server
     */
    private class TcpServer implements Runnable {

        @Override
        public void run() {

            try {
                tcpServer(); // Launch tcp server in a new thread
            } catch (Exception e) {

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
                income.read(stringBytes, 0, stringBytes.length);
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
                // e.printStackTrace(); FIXME
            } finally {
                try {

                    if (fis != null) fis.close();
                    if (bis != null) bis.close();
                    if (outcome != null) outcome.close();
                    if (income != null) income.close();
                    connectionSocket.close();
                } catch (IOException e) {
                    e.printStackTrace(); // FIXME
                }
            }
        }
    }
}
