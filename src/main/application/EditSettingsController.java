package application;

import util.Constants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import static settings.AppSettings.*;
import static util.StringUtil.arrayToString;

/**
 * Controller to edit application settings file.
 * <p>
 * Created by danielGoncalves on 12/05/17.
 */
public class EditSettingsController {

    private static final String HEADER = "############################\n" +
            "# P2PFileShareApp Settings #\n" +
            "############################";

    public void edit(String shdDir, String dwlDir, Integer udpPort, Integer tcpPort, Integer broadcastPeriod,
                     Integer fileRefresh, String[] exts, Integer maxUploads) {

        Properties properties = new Properties();
        OutputStream config = null;

        try {
            config = new FileOutputStream(Constants.CONFIG_FILENAME);
            // set the properties value
            properties.setProperty(SHD_DIR_KEY, shdDir);
            properties.setProperty(DOWNLOADS_DIR_KEY, dwlDir);
            properties.setProperty(UDP_PORT_KEY, udpPort.toString());
            properties.setProperty(TCP_PORT_KEY, tcpPort.toString());
            properties.setProperty(BROADCAST_TIME_INTERVAL_KEY, broadcastPeriod.toString());
            properties.setProperty(FILE_REFRESH_TIME_KEY, fileRefresh.toString());
            properties.setProperty(FILE_EXTENSIONS_KEY, arrayToString(exts));
            properties.setProperty(MAX_UPLOADS_KEY, maxUploads.toString());

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
