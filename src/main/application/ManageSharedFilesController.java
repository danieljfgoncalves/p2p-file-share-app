package application;

import settings.Application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Controller to add/remove a shared file from directory.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class ManageSharedFilesController {

    /**
     * Adds a new shared file to the apps shared folder (By default /shared)
     *
     * @param shdFile the new file to share
     * @throws IOException I/O error
     */
    public void addShareFile(File shdFile) throws IOException {

        File newFile = new File(Application.settings().getShdDir(), shdFile.getName());
        Files.copy(shdFile.toPath(), newFile.toPath());
    }

    /**
     * Removes a shared file from the apps shared folder (By default /shared)
     *
     * @param shdFile the shared file to remove
     */
    public void removeSharedFile(File shdFile) {

        shdFile.delete();
    }
}
