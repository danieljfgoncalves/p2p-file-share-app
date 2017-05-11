package domain;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Observable;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Represents the directory that will update if any changes to directory.
 * <p>
 * Created by danielGoncalves on 09/05/17.
 */
public class WatchedDirectory extends Observable {

    private final WatchService watcher;
    private final Path directory;
    private final FileFilter fileFilter;

    public WatchedDirectory(String dirPath) throws IOException {

        this.watcher = FileSystems.getDefault().newWatchService();
        this.directory = Paths.get(dirPath);
        this.directory.register(this.watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        this.fileFilter = new MyFileFilter();
    }

    public void watch() throws InterruptedException {

        // Print to console
        System.out.println("Watch Service registered for directory: " + directory.getFileName());

        while (true) {

            WatchKey key = watcher.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();

                System.out.println(kind.name() + ": " + fileName);

                // Notify Observers
                setChanged();
                notifyObservers();
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    public File[] getFiles() throws IOException {

        File folder = this.directory.toFile();

        return folder.listFiles(this.fileFilter);
    }

    /**
     * A inner class that implements the Java FileFilter interface to exclude directories.
     */
    private class MyFileFilter implements FileFilter {
        public boolean accept(File file) {
            return file.isFile();
        }
    }
}
