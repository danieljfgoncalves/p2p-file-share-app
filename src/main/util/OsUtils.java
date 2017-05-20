package util;

/**
 * OS Utils
 * <p>
 * Created by 2DD - Group SNOW WHITE {1151452, 1151031, 1141570, 1151088}
 */
public class OsUtils {

    private static String OS = System.getProperty("os.name").toLowerCase();

    /**
     * Checks if the OS is Windows
     */
    public static boolean isWindows() {

        return (OS.contains("win"));
    }

    /**
     * Checks if the OS is macOS
     */
    public static boolean isMac() {

        return (OS.contains("mac"));

    }

    /**
     * Checks if the OS is UNIX
     */
    public static boolean isUnix() {

        return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
    }

    /**
     * Checks if the OS is Solaris
     */
    public static boolean isSolaris() {

        return (OS.contains("sunos"));
    }

}
