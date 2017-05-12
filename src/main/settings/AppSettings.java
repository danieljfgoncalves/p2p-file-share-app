package settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the App Settings
 * <p>
 * Created by danielGoncalves on 09/05/17.
 */
public class AppSettings {

    // TODO : Refactor code that needs settings

    private final static String PROPERTIES_RESOURCE = "settings.properties";
    // PROPERTY KEYS
    private final static String USERNAME_KEY = "username";
    private final static String SHD_DIR_KEY = "shd.directory";
    private final static String DOWNLOADS_DIR_KEY = "downloads.directory";
    private final static String UDP_PORT_KEY = "udp.port";
    private final static String TCP_PORT_KEY = "tcp.port";
    private final static String BROADCAST_TIME_INTERVAL_KEY = "broadcast.time.interval";
    private final static String FILE_REFRESH_TIME_KEY = "announcement.refresh.time";
    private final static String FILE_EXTENSIONS_KEY = "file.extensions";
    private final static String MAX_UPLOADS_KEY = "max.upload.connections";
    // PROPERTY DEFAULTS
    private final static String USERNAME_DEFAULT = System.getProperty("user.name", "unknown");
    private final static String SHD_DIR_DEFAULT = "shared";
    private final static String DOWNLOADS_DIR_DEFAULT = "download";
    private final static Integer UDP_PORT_DEFAULT = 32035;
    private final static Integer TCP_PORT_DEFAULT = 0;
    private final static Integer BROADCAST_TIME_INTERVAL_DEFAULT = 30;
    private final static Integer FILE_REFRESH_TIME_DEFAULT = 45;
    private final static String FILE_EXTENSIONS_DEFAULT = "jpg,png,txt,mp3,mov,avi,doc,docx,xls,xlsx";
    private final static Integer MAX_UPLOADS_DEFAULT = 10;
    // MISC
    private final static String DEFAULT_SEPARATOR = ",";


    private final Properties applicationProperties = new Properties();

    AppSettings() {
        loadProperties();
    }

    private void loadProperties() {
        InputStream propertiesStream = null;
        try {
            propertiesStream = this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE);
            if (propertiesStream != null) {
                this.applicationProperties.load(propertiesStream);
            } else {
                throw new FileNotFoundException(
                        "property file '" + PROPERTIES_RESOURCE + "' not found in the classpath");
            }
        } catch (final IOException exio) {

            // If no file found set default
            setDefaultProperties();

            Logger.getLogger(AppSettings.class.getName()).log(Level.SEVERE, null, exio);
        } finally {
            if (propertiesStream != null) {
                try {
                    propertiesStream.close();
                } catch (final IOException ex) {
                    Logger.getLogger(AppSettings.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void setDefaultProperties() {
        this.applicationProperties.setProperty(SHD_DIR_KEY, SHD_DIR_DEFAULT);
        this.applicationProperties.setProperty(DOWNLOADS_DIR_KEY, DOWNLOADS_DIR_DEFAULT);
        this.applicationProperties.setProperty(UDP_PORT_KEY, UDP_PORT_DEFAULT.toString());
        this.applicationProperties.setProperty(TCP_PORT_KEY, TCP_PORT_DEFAULT.toString());
        this.applicationProperties.setProperty(BROADCAST_TIME_INTERVAL_KEY, BROADCAST_TIME_INTERVAL_DEFAULT.toString());
        this.applicationProperties.setProperty(FILE_REFRESH_TIME_KEY, FILE_REFRESH_TIME_DEFAULT.toString());
        this.applicationProperties.setProperty(FILE_EXTENSIONS_KEY, FILE_EXTENSIONS_DEFAULT);
        this.applicationProperties.setProperty(MAX_UPLOADS_KEY, MAX_UPLOADS_DEFAULT.toString());
    }

    public String getUsername() {

        String username = this.applicationProperties.getProperty(USERNAME_KEY, USERNAME_DEFAULT);

        return (username.length() == 0) ? USERNAME_DEFAULT : username;
    }

    public String getShdDir() {
        return this.applicationProperties.getProperty(SHD_DIR_KEY, SHD_DIR_DEFAULT);
    }

    public String getDownloadsDir() {
        return this.applicationProperties.getProperty(DOWNLOADS_DIR_KEY, DOWNLOADS_DIR_DEFAULT);
    }

    public Integer getUdpPort() {
        return new Integer(this.applicationProperties.getProperty(UDP_PORT_KEY, UDP_PORT_DEFAULT.toString()));
    }

    public Integer getTcpPort() {
        return new Integer(this.applicationProperties.getProperty(TCP_PORT_KEY, TCP_PORT_DEFAULT.toString()));
    }

    public Integer getBroadcastTimeInterval() {
        return new Integer(this.applicationProperties.getProperty(BROADCAST_TIME_INTERVAL_KEY, BROADCAST_TIME_INTERVAL_DEFAULT.toString()));
    }

    public Integer getFileRefreshTime() {
        return new Integer(this.applicationProperties.getProperty(FILE_REFRESH_TIME_KEY, FILE_REFRESH_TIME_DEFAULT.toString()));
    }

    public String[] getFileExtensions() {
        return this.applicationProperties.getProperty(FILE_EXTENSIONS_KEY, FILE_EXTENSIONS_DEFAULT).split(DEFAULT_SEPARATOR);
    }

    public Integer getMaxUploads() {
        return new Integer(this.applicationProperties.getProperty(MAX_UPLOADS_KEY, MAX_UPLOADS_DEFAULT.toString()));
    }
}
