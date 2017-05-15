package domain;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

/**
 * Represents a container of filename items.
 * <p>
 * Created by danielGoncalves on 09/05/17.
 */
public class FilenameItemList implements Observer {

    private ObservableList<FilenameItem> list;

    public FilenameItemList() {
        this.list = FXCollections.observableArrayList();
    }

    public boolean add(FilenameItem item) {

        if (!item.isActive()) {
            return false;
        }
        // If list already contains item refresh scheduler
        int index = list.indexOf(item);
        if (index >= 0) {

            FilenameItem found = list.get(index);
            found.refresh();
            return false;
        }

        // If it's a new item add to list
        item.addObserver(this);
        item.activate();
        return this.list.add(item);
    }

    public boolean addAll(Collection<FilenameItem> collection) {

        boolean ret = true;
        for (FilenameItem filename :
                collection) {
            if (!add(filename)) ret = !ret;
        }

        return ret;
    }

    public boolean remove(FilenameItem item) {

        return this.list.remove(item);
    }

    public ObservableList<FilenameItem> getList() {
        return this.list;
    }

    @Override
    public void update(Observable o, Object arg) {

        // MUTEX : because of race condition when updating more than one observable
        synchronized (this) {
            if (o instanceof FilenameItem) {

                FilenameItem item = (FilenameItem) o;
                if (!item.isActive()) {
                    remove(item);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilenameItemList that = (FilenameItemList) o;

        return list.equals(that.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }
}
