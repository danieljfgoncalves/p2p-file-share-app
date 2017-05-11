package domain;

import util.Constants;

import java.net.InetAddress;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>
 * Represents a filename with related information (username, host address, etc.)
 * <p>
 * Created by danielGoncalves on 09/05/17.
 */
public class FilenameItem extends Observable {

    private String filename;
    private String username;
    private InetAddress host;
    private Short tcpPort;
    private Boolean active;
    private Timer timer; // Timer to trigger state if refresh time limit is reached.

    public FilenameItem(String filename, String username, InetAddress hostAddress, Short tcpPort) {

        this.filename = filename;
        this.username = username;
        this.host = hostAddress;
        this.tcpPort = tcpPort;

        // Instantiate observer list, active state & timer;
        this.active = true;
        // Set timer to null
        this.timer = null;

    }

    public String getFilename() {
        return this.filename;
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getHost() {
        return host;
    }

    public Short getTcpPort() {
        return tcpPort;
    }

    public boolean isActive() {
        return this.active;
    }

    /**
     * Resets the filename refresh time limit.
     */
    public void refresh() {

        this.timer = new Timer();
        this.timer.schedule
                (new ChangeStateTimerTask(), Constants.REFRESH_FILENAME_TIMELIMIT * 1000);
    }

    private void deactivate() {

        this.active = false;
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

        @Override
        public void run() {

            deactivate();
        }
    }
}
