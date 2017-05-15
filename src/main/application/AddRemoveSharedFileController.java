package application;

import settings.Application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Controller to add/remove a shared file from directory.
 * <p>
 * Created by danielGoncalves on 12/05/17.
 */
public class AddRemoveSharedFileController {

    public void addShareFile(File shdFile) throws IOException {

        File newFile = new File(Application.settings().getShdDir(), shdFile.getName());

        Files.copy(shdFile.toPath(), newFile.toPath());
    }

    public void removeSharedFile(File shdFile) {

        shdFile.delete();
    }
}
