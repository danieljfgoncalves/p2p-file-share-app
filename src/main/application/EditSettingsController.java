package application;

import util.Constants;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * Controller to edit application settings file.
 * <p>
 * Created by danielGoncalves on 12/05/17.
 */
public class EditSettingsController {

    private static final String HEADER = "############################\n" +
            "# P2PFileShareApp Settings #\n" +
            "############################";

    public void edit(Map<String, String> settings) throws IOException {

        File configFile = new File(Constants.CONFIG_FILENAME);
        if (!configFile.exists()) configFile.createNewFile();

        Properties properties = new Properties();
        OutputStream config = null;
        try {
            InputStream input = new FileInputStream(configFile);
            properties.load(input);
            input.close();
            System.out.println("+++++++++++++++pas+++++++++++++++");
            config = new FileOutputStream(configFile);
            // set the properties value
            properties.putAll(settings);
            // save properties to project root folder
            properties.store(config, HEADER);

        } catch (IOException io) {
            io.printStackTrace();
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
