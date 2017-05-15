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
 * Created by danielGoncalves on 09/05/17.
 */
public class FilenameItem extends Observable {

    // TODO : IF possible send file size too.

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

    public FilenameItem(String filename, String username, InetAddress hostAddress, Integer tcpPort) {

        this.filename = filename;
        this.username = username;
        this.host = hostAddress;
        this.tcpPort = tcpPort;

        filenameProperty = new SimpleStringProperty(filename);
        usernameProperty = new SimpleStringProperty(username);

        // active state;
        active = true;
        // Instantiate scheduler
        scheduler = Executors.newScheduledThreadPool(1);
        futureTask = null;
    }

    public StringProperty filenameProperty() {
        return filenameProperty;
    }

    public StringProperty usernameProperty() {
        return usernameProperty;
    }

    public String getFilename() {
        return filename;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getHost() {
        return host;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public boolean isActive() {
        return this.active;
    }

    /**
     * Resets the filename refresh time limit.
     */
    public void refresh() {

        if (futureTask != null) {
            cancel();
        }
        activate();
        System.out.println("[Refresh] " + filename);
    }

    public void activate() {

        if (futureTask == null) {
            futureTask = scheduler.schedule(
                    new ChangeStateTimerTask(this),
                    Application.settings().getFileRefreshTime(),
                    TimeUnit.SECONDS);
        }
    }

    private void cancel() {

        if (futureTask != null) {
            futureTask.cancel(true);
        }
    }

    public void shutdown() {

        cancel();
        scheduler.shutdownNow();
        System.out.println("[Shutdown] " + filename);
    }

    private void deactivate() {

        this.active = false;
        // Shutdown
        shutdown();
        // Notify observers
        setChanged();
        notifyObservers();
    }

    @Override
    public int hashCode() {
        int result = filename.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + tcpPort.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilenameItem that = (FilenameItem) o;

        if (!filename.equals(that.filename)) return false;
        if (!username.equals(that.username)) return false;
        if (!host.equals(that.host)) return false;
        return tcpPort.equals(that.tcpPort);
    }

    /**
     * Private TimerTask to run state change
     */
    private class ChangeStateTimerTask extends TimerTask {

        private final FilenameItem item;

        private ChangeStateTimerTask(FilenameItem filenameItem) {

            item = filenameItem;
        }

        @Override
        public void run() {

            System.out.println("[Deactivate]" + item.filename);
            item.deactivate();
        }
    }
}
