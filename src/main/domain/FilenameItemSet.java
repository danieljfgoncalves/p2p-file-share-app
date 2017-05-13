package domain;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Observable;
import java.util.Observer;

/**
 * Represents a container of filename items.
 * <p>
 * Created by danielGoncalves on 09/05/17.
 */
public class FilenameItemSet implements Observer {

    private ObservableList<FilenameItem> list;

    public FilenameItemSet() {
        this.list = FXCollections.observableArrayList();
    }

    public boolean add(FilenameItem item) {

        if (!item.isActive()) {
            return false;
        }
        if (this.list.contains(item)) {

            this.remove(item);
        }
        item.refresh();
        item.addObserver(this);

        return this.list.add(item);
    }

    public boolean remove(FilenameItem item) {

        return this.list.remove(item);
    }

    public ObservableList<FilenameItem> getList() {
        return this.list;
    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof FilenameItem) {

            FilenameItem item = (FilenameItem) o;
            if (!item.isActive()) {
                remove(item);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilenameItemSet that = (FilenameItemSet) o;

        return list.equals(that.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }
}
