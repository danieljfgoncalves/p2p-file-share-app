package domain;

import util.Constants;

import java.net.InetAddress;
import java.util.*;

/**
 * TODO : Implement Observable methods
 * <p>
 * Represents a filename with related information (username, host address, etc.)
 * <p>
 * Created by danielGoncalves on 09/05/17.
 */
public class FilenameItem extends Observable {

    private String filename;
    private String username;
    private InetAddress host;
    private Integer tcpPort;
    private Boolean active;
    private List<Observer> observers;
    private Timer timer; // Timer to trigger state if refresh time limit is reached.

    public FilenameItem(String filename, String username, InetAddress hostAddress, Integer tcpPort) {

        this.filename = filename;
        this.username = username;
        this.host = hostAddress;
        this.tcpPort = tcpPort;

        // Instantiate observer list, active state & timer;
        this.observers = new ArrayList<>();
        this.active = true;
        // Set timer
        refresh();

    }

    /**
     * Resets the filename refresh limit.
     */
    public void refresh() {

        this.timer = new Timer();
        this.timer.schedule
                (new ChangeStateTimerTask(), Constants.REFRESH_LIMIT_FILENAME * 1000);
    }

    private void deactive() {

        this.active = false;

        // TODO : Implement notifyObservers
    }

    /**
     * Private TimerTask to run state change
     */
    private class ChangeStateTimerTask extends TimerTask {

        @Override
        public void run() {

            deactive();
        }
    }
}
