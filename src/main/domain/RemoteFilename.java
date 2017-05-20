package domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import settings.Application;

import java.net.InetAddress;
import java.util.Observable;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Represents a filename with related information (username, host address, etc.)
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class RemoteFilename extends Observable {

    private static final int THREAD_POOL_SIZE = 10;
    // JavaFX
    private final StringProperty filenameProperty;
    private final StringProperty usernameProperty;
    private final String filename;
    private final String username;
    private final InetAddress host;
    private final Integer tcpPort;
    private final ScheduledExecutorService scheduler;
    private Boolean active;
    private ScheduledFuture<?> futureTask;

    /**
     * Creates a available remote file name.
     *
     * @param filename    the file's name
     * @param username    the user's name that hosts the file
     * @param hostAddress the user's host IPv4 address
     * @param tcpPort     the user's tcp port to connect
     */
    public RemoteFilename(String filename, String username, InetAddress hostAddress, Integer tcpPort) {

        this.filename = filename;
        this.username = username;
        this.host = hostAddress;
        this.tcpPort = tcpPort;

        filenameProperty = new SimpleStringProperty(filename);
        usernameProperty = new SimpleStringProperty(username);

        // active state;
        active = true;
        // Instantiate scheduler
        scheduler = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        futureTask = null;
    }

    /**
     * Obtain the javaFX filename property
     *
     * @return javaFX filename property
     */
    public StringProperty filenameProperty() {
        return filenameProperty;
    }

    /**
     * Obtain the javaFX username property
     *
     * @return javaFX username property
     */
    public StringProperty usernameProperty() {
        return usernameProperty;
    }

    /**
     * Obtain the file's name
     *
     * @return the file's name
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Obtain the user's name (owner of the file)
     *
     * @return the user's name (owner of the file)
     */
    public String getUsername() {
        return username;
    }

    /**
     * Obtain the host IPv4 address
     *
     * @return the host IPv4 address
     */
    public InetAddress getHost() {
        return host;
    }

    /**
     * Obtain the TCP Port to connect
     *
     * @return the TCP Port to connect
     */
    public Integer getTcpPort() {
        return tcpPort;
    }

    /**
     * Check if remote file is active
     *
     * @return true if active, false otherwise
     */
    boolean isInactive() {
        return this.active;
    }

    /**
     * Resets the filename refresh time limit
     */
    void refresh() {

        if (futureTask != null) {
            cancel();
        }
        activate();
        System.out.println("[Refresh] " + filename);
    }

    /**
     * Activates the timeout limit
     */
    void activate() {

        if (futureTask == null) {
            futureTask = scheduler.schedule(
                    new ChangeStateTimerTask(this),
                    Application.settings().getFileRefreshTime(),
                    TimeUnit.SECONDS);
            System.out.println("[Activate] " + filename);
        }
    }

    /**
     * Cancels the timeout limit
     */
    private void cancel() {

        if (futureTask != null) {
            futureTask.cancel(true);
            futureTask = null;
            System.out.println("[Cancel] " + filename);
        }
    }

    /**
     * Shutdown the remote file timeout scheduler
     */
    private void shutdown() {

        cancel();
        scheduler.shutdownNow();
        System.out.println("[Shutdown] " + filename);
    }

    /**
     * Deactivates the timeout scheduler
     */
    private void deactivate() {

        this.active = false;
        // Shutdown
        shutdown();
        // Notify observers
        setChanged();
        notifyObservers();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteFilename that = (RemoteFilename) o;

        return filename.equals(that.filename) && username.equals(that.username) && host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int result = filename.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + host.hashCode();
        return result;
    }

    /**
     * Private TimerTask to run state change
     */
    private class ChangeStateTimerTask extends TimerTask {

        private final RemoteFilename item;

        /**
         * Creates timer task for the remote file
         *
         * @param remoteFilename remote file name
         */
        private ChangeStateTimerTask(RemoteFilename remoteFilename) {

            item = remoteFilename;
        }

        @Override
        public void run() {

            System.out.println("[Deactivate]" + item.filename);
            item.deactivate();
        }
    }
}
