package application;

import presentation.EditConfigurationDialog;
import util.Constants;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller to edit application settings file.
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class EditConfigurationsController {

    private static final String HEADER = "############################\n" +
            "# P2PFileShareApp Settings #\n" +
            "############################";

    /**
     * Edits and persists the configuration file.
     *
     * @param configurations the new configurations
     * @throws IOException I/O error
     */
    public void edit(Map<String, String> configurations) throws IOException {

        File configFile = new File(Constants.CONFIG_FILENAME);
        if (!configFile.exists()) configFile.createNewFile();

        Properties properties = new Properties();
        OutputStream config = null;
        try {
            InputStream input = new FileInputStream(configFile);
            properties.load(input);
            input.close();
            config = new FileOutputStream(configFile);
            // set the properties value
            properties.putAll(configurations);
            // save properties to project root folder
            properties.store(config, HEADER);

        } catch (IOException io) {
            Logger.getLogger(EditConfigurationDialog.class.getName()).log(Level.SEVERE, "Saving the configurations failed.", io);
        } finally {
            if (config != null) {
                try {
                    config.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
