package domain;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

/**
 * Represents a container of filename items.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class RemoteFilenameList implements Observer {

    private final ObservableList<RemoteFilename> list;

    /**
     * Creates a remote file list manager
     */
    public RemoteFilenameList() {
        this.list = FXCollections.observableArrayList();
    }

    /**
     * Adds a remote file to the list and activates/refreshes the timeout scheduler for the added file
     *
     * @param item the remote file to add
     * @return true if file is added, false if resfreshed or if file was inactive
     */
    public boolean add(RemoteFilename item) {

        if (item.isInactive()) {
            return false;
        }
        // If list already contains item refresh scheduler
        int index = list.indexOf(item);
        if (index >= 0) {

            RemoteFilename found = list.get(index);
            found.refresh();
            return false;
        }

        // If it's a new item add to list
        item.addObserver(this);
        item.activate();
        return this.list.add(item);
    }

    /**
     * Adds a collection of remote files.
     *
     * @param collection the collection of remote files
     * @return true if all were added
     */
    public void addAll(Collection<RemoteFilename> collection) {
        for (RemoteFilename filename :
                collection) {
            add(filename);
        }
    }

    /**
     * Removes a remote file from the list.
     *
     * @param item the rmeote file to remove
     * @return true if removed, false otherwise
     */
    public void remove(RemoteFilename item) {

        this.list.remove(item);
    }

    /**
     * Obtains the list
     *
     * @return the remote file list
     */
    public ObservableList<RemoteFilename> getList() {
        return this.list;
    }

    @Override
    public void update(Observable o, Object arg) {

        // MUTEX : because of race condition when updating more than one observable
        synchronized (this) {
            if (o instanceof RemoteFilename) {

                RemoteFilename item = (RemoteFilename) o;
                if (item.isInactive()) {
                    remove(item);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteFilenameList that = (RemoteFilenameList) o;

        return list.equals(that.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }
}
