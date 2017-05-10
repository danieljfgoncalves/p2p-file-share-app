package domain;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Represents a container of filename items.
 * <p>
 * Created by danielGoncalves on 09/05/17.
 */
public class FilenameItemSet extends Observable implements Observer {

    private Set<FilenameItem> set;

    public FilenameItemSet() {
        this.set = new HashSet<>();
    }

    public boolean add(FilenameItem item) {

        if (!item.isActive()) {
            return false;
        }

        item.refresh();
        item.addObserver(this);

        return this.set.add(item);
    }

    public boolean remove(FilenameItem item) {

        return this.set.remove(item);
    }

    public Set<FilenameItem> getSet() {
        return this.set;
    }

    public void notifyChanges() {

        // Notify observers
        setChanged();
        notifyObservers();
    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof FilenameItem) {

            FilenameItem item = (FilenameItem) o;
            if (!item.isActive()) {
                remove(item);
                // Notify observers
                notifyChanges();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilenameItemSet that = (FilenameItemSet) o;

        return set.equals(that.set);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }
}
