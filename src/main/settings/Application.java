package settings;

/**
 * A "global" static class with the application settings.
 * <p>
 * Created by danielGoncalves on 12/05/17.
 */
public class Application {
    private static final AppSettings SETTINGS = new AppSettings();

    private Application() {
    }

    /**
     * Obtain the application settings
     *
     * @return the app settings
     */
    public static AppSettings settings() {
        return SETTINGS;
    }
}
