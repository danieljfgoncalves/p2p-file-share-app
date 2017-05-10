package networking.udp;

import domain.FilenameItemSet;
import domain.WatchedDirectory;

import java.net.DatagramSocket;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Handles UDP Connection to obtain peers list of files to share & sends its own shared files
 *
 * Created by danielGoncalves on 09/05/17.
 */
public class UdpCommunication extends Observable {

    private static final int TIMER_DELAY = 2;
    private final DatagramSocket udpSocket;
    private final int timeInterval;
    private final int timePeriod;
    private final Thread receiverThread;
    private final FilenameItemSet filenames;
    private final WatchedDirectory sharedDirectory;
    private Timer sendingTimer;

    public UdpCommunication(DatagramSocket socket, int timeIntervalInSecs, int sendingTimePeriod,
                            WatchedDirectory sharedDir, FilenameItemSet filenamesSet) {

        udpSocket = socket;

        sendingTimer = new Timer();
        timeInterval = timeIntervalInSecs;
        timePeriod = sendingTimePeriod;

        receiverThread = new Thread(new UdpReceiver());

        sharedDirectory = sharedDir;
        filenames = filenamesSet;
    }

    public void start() {

        // Start Receiver
        receiverThread.start();

        // Start Sender
        sendingTimer.scheduleAtFixedRate(new UdpSender(), TIMER_DELAY * 1000, timePeriod * 1000);
    }

    public void kill() {

        // Terminates receiver server
        receiverThread.interrupt();
        // Terminates sending timer
        sendingTimer.cancel();
        sendingTimer.purge();
    }

    private void receiverServer() {

        // TODO: Implement receiver server
    }

    private synchronized void sendBroadcast() {

        // TODO: Implement sending a broadcast to other peers
    }

    /**
     * UDP Receiver Handler
     */
    private class UdpReceiver implements Runnable {

        @Override
        public void run() {

            receiverServer(); // Launch receiver server in a new thread
        }
    }

    /**
     * UDP Sender Handler
     */
    private class UdpSender extends TimerTask {

        @Override
        public void run() {

            sendBroadcast(); // Send filenames to share in broadcast
        }
    }
}
